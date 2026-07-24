package com.pvz.game;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.GameStatus;
import com.pvz.model.enums.LootType;
import com.pvz.model.enums.PlantCategory;
import com.pvz.model.plants.*;
import com.pvz.model.support.*;
import com.pvz.model.zombies.*;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameTickProcessor {
    private final GameSession session;
    private final Board board;
    private final SunManager sunManager;
    private final PlantFoodInventory plantFoodInventory;
    private final WaveController waveController;
    private final TickClock clock;
    private final LootDropService lootDropService;
    private final ProjectileResolver projectileResolver;
    private final ZombieBehaviorController zombieBehaviorController;
    private final GameStatistics statistics;
    private final Map<String, Float> cooldowns;
    private final Map<Plant, Float> plantActionElapsed;
    private final List<BouncingGrape> bouncingGrapes;
    private final Set<Zombie> processedDeaths;
    private final Set<SpecialSpawnEvent> processedSpecialSpawns;
    public GameTickProcessor(GameSession session, Map<String, Float> cooldowns,
                             ProjectileResolver projectileResolver,
                             ZombieBehaviorController behaviorController,
                             LootDropService lootDropService) {
        this.session = session;
        this.board = session.getBoard();
        this.sunManager = session.getSunManager();
        this.plantFoodInventory = session.getPlantFoodInventory();
        this.waveController = session.getWaveController();
        this.clock = session.getClock();
        this.statistics = session.getStatistics();
        this.cooldowns = cooldowns;
        this.projectileResolver = projectileResolver;
        this.zombieBehaviorController = behaviorController;
        this.lootDropService = lootDropService;
        this.plantActionElapsed = new IdentityHashMap<>();
        this.bouncingGrapes = new ArrayList<>();
        this.processedDeaths = java.util.Collections.newSetFromMap(
                new IdentityHashMap<>());
        this.processedSpecialSpawns = java.util.Collections.newSetFromMap(
                new IdentityHashMap<>());
    }
    public void updateOneTick() {
        updateCooldowns();
        updateSpecialSpawns();
        updatePlants();
        updateBouncingGrapes();
        sunManager.update(clock.currentTick, clock.getElapsedSeconds());
        updateFrozenBlocks();
        updateZombies();
        removeDestroyedObjects();
        updateWaveState();
    }
    public void registerPlant(Plant plant) {
        float initialElapsed = 0f;
        if (plant.getNormalizedType().equals("chomper")) {
            initialElapsed = plant.actionInterval;
        }
        plantActionElapsed.put(plant, initialElapsed);
    }
    public void forgetPlant(Plant plant) {
        plantActionElapsed.remove(plant);
    }
    public void launchGrapeshot(Plant source) {
        int bounces = BalanceDefaults.GRAPESHOT_BASE_BOUNCES;
        if (source.level >= 3) {
            bounces++;
        }
        for (int count = 0; count < BalanceDefaults.GRAPESHOT_GRAPE_COUNT; count++) {
            bouncingGrapes.add(new BouncingGrape(source,
                    BalanceDefaults.GRAPESHOT_SECONDARY_DAMAGE, bounces));
        }
    }
    public void removeDestroyedObjects() {
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                removeDestroyedObjects(board.getTile(
                        new GridPosition(column, row)));
            }
        }
    }
    private void updateSpecialSpawns() {
        for (SpecialSpawnEvent event
                : session.getStageConfig().specialSpawnEvents) {
            if (event.tick <= clock.currentTick
                    && processedSpecialSpawns.add(event)) {
                session.spawnZombieFromSpecialTile(event.zombieType,
                        event.position);
            }
        }
    }
    private void updateBouncingGrapes() {
        float elapsed = clock.getTickDurationSeconds();
        bouncingGrapes.removeIf(grape -> !grape.update(
                elapsed, board, projectileResolver));
    }
    private void updatePlants() {
        for (Plant plant : board.getAllPlants()) {
            boolean pendingSun = plant instanceof SunProducerPlant
                    && sunManager.hasPendingPlantSun(plant);
            if (plant instanceof SunProducerPlant) {
                ((SunProducerPlant) plant).setProductionPaused(pendingSun);
            }
            plant.update(1);
            if (!plant.canAct()) {
                continue;
            }
            WallPlant.resolvePassivePlant(plant, session, projectileResolver);
            if (plant instanceof SunProducerPlant && !pendingSun) {
                sunManager.producePlantSun(plant);
            }
            if (plant instanceof ExplosivePlant
                    && ((ExplosivePlant) plant).isGraveEatingFinished()) {
                ExplosivePlant.resolveInstantPlant(plant, session,
                        projectileResolver);
                continue;
            }
            processPlantAttack(plant);
        }
    }
    private void processPlantAttack(Plant plant) {
        if (!plant.canAct() || plant.actionInterval <= 0f
                || plant.category == PlantCategory.SUN_PRODUCER
                || plant.category == PlantCategory.WALL
                || plant.category == PlantCategory.MODIFIER) {
            return;
        }
        float elapsed = plantActionElapsed.getOrDefault(plant, 0f)
                + clock.getTickDurationSeconds();
        if (elapsed + 0.0001f < plant.actionInterval) {
            plantActionElapsed.put(plant, elapsed);
            return;
        }
        if (projectileResolver.resolvePlantAttack(plant, session)) {
            elapsed = 0f;
        }
        plantActionElapsed.put(plant, elapsed);
    }
    private void updateZombies() {
        for (Zombie zombie : getAllZombies()) {
            if (zombie.isDead()) {
                continue;
            }
            zombie.update(1);
            if (zombie.isFrozen() || zombie.isStunned()) {
                continue;
            }
            zombieBehaviorController.update(zombie, session);
            if (zombie.isHypnotized) {
                updateHypnotizedZombie(zombie);
            }
            else {
                updateHostileZombie(zombie);
            }
            handleLawnMower(zombie);
        }
    }
    private void updateHostileZombie(Zombie zombie) {
        if (resolveHostileCollision(zombie)) {
            return;
        }
        Plant target = session.findPlantTarget(zombie);
        if (!hasReachedPlant(zombie, target)) {
            moveZombie(zombie);
            return;
        }
        if (!zombie.canAttackThisTick()) {
            return;
        }
        if (!zombieBehaviorController.handlePlantContact(zombie, target, session)) {
            WallPlant.resolveZombiePlantContact(zombie, target, session);
        }
    }
    private void updateHypnotizedZombie(Zombie zombie) {
        if (attackPushedObject(zombie)) {
            return;
        }
        Zombie target = findNearestEnemyAhead(zombie);
        if (target != null && sameTileX(target, zombie)) {
            if (!zombie.canAttackThisTick()) {
                return;
            }
            target.takeDamage(zombie.damageToPlant);
            return;
        }
        moveZombie(zombie);
    }
    private boolean resolveHostileCollision(Zombie zombie) {
        if (zombie instanceof FootballZombie) {
            Zombie target = findHypnotizedAt(
                    zombie.currentPosition.x, zombie.lane, zombie);
            return ((FootballZombie) zombie)
                    .collideWithHypnotizedZombie(target);
        }
        if (zombie instanceof ArcadeZombie) {
            return resolveArcadeMachineCollision((ArcadeZombie) zombie);
        }
        return false;
    }
    private boolean resolveArcadeMachineCollision(ArcadeZombie arcade) {
        ArcadeMachine machine = arcade.arcadeMachine;
        if (machine == null || machine.health <= 0 || machine.position == null) {
            return false;
        }
        Zombie target = findHypnotizedAt(machine.position.x,
                machine.position.y, arcade);
        if (target != null) {
            machine.instantKill(target);
            return true;
        }
        Tile tile = board.getTile(new GridPosition(
                (int) Math.floor(machine.position.x), machine.position.y));
        Plant plant = null;
        if (tile != null) {
            plant = tile.getPlant();
        }
        if (plant != null && plant.isAlive) {
            machine.instantKill(plant);
            return true;
        }
        return false;
    }
    private Zombie findHypnotizedAt(float x, int lane, Zombie excluded) {
        int tileX = (int) Math.floor(x);
        for (Zombie candidate : board.getZombiesInLane(lane)) {
            if (candidate != excluded && candidate.isHypnotized
                    && !candidate.isDead()
                    && (int) Math.floor(candidate.currentPosition.x) == tileX) {
                return candidate;
            }
        }
        return null;
    }
    private boolean attackPushedObject(Zombie attacker) {
        if (!attacker.canAttackThisTick()) {
            return false;
        }
        for (Zombie zombie : board.getZombiesInLane(attacker.lane)) {
            if (zombie instanceof ArcadeZombie
                    && isObjectReachable(attacker, ((ArcadeZombie) zombie)
                    .arcadeMachine.position)) {
                ArcadeMachine machine = ((ArcadeZombie) zombie).arcadeMachine;
                machine.health = Math.max(0, machine.health - attacker.damageToPlant);
                return true;
            }
            if (zombie instanceof BarrelRollerZombie
                    && isObjectReachable(attacker, ((BarrelRollerZombie) zombie)
                    .barrel.position)) {
                damageBarrel(((BarrelRollerZombie) zombie).barrel, attacker.damageToPlant);
                return true;
            }
        }
        for (Barrel barrel : board.getLooseBarrels()) {
            if (isObjectReachable(attacker, barrel.position)) {
                damageBarrel(barrel, attacker.damageToPlant);
                return true;
            }
        }
        return false;
    }
    private boolean isObjectReachable(Zombie attacker, ContinuousPosition position) {
        return position != null && position.y == attacker.lane
                && position.x >= attacker.currentPosition.x
                && position.x - attacker.currentPosition.x < 1f;
    }
    private void damageBarrel(Barrel barrel, int damage) {
        if (barrel == null || barrel.health <= 0) {
            return;
        }
        barrel.health = Math.max(0, barrel.health - Math.max(1, damage));
        if (barrel.health > 0) {
            return;
        }
        for (ImpZombie imp : barrel.breakAndSpawnImps()) {
            board.placeZombie(imp, imp.currentPosition);
        }
        board.removeLooseBarrel(barrel);
    }
    private Zombie findNearestEnemyAhead(Zombie attacker) {
        Zombie nearest = null;
        for (Zombie candidate : board.getZombiesInLane(attacker.lane)) {
            if (candidate == attacker || candidate.isDead()
                    || candidate.isHypnotized
                    || candidate.currentPosition.x < attacker.currentPosition.x) {
                continue;
            }
            if (nearest == null || candidate.currentPosition.x
                    < nearest.currentPosition.x) {
                nearest = candidate;
            }
        }
        return nearest;
    }
    private boolean sameTileX(Zombie first, Zombie second) {
        return (int) Math.floor(first.currentPosition.x)
                == (int) Math.floor(second.currentPosition.x);
    }
    private boolean hasReachedPlant(Zombie zombie, Plant plant) {
        return plant != null
                && (int) Math.floor(zombie.currentPosition.x) == plant.location.x;
    }
    private void moveZombie(Zombie zombie) {
        zombie.move(clock.getTickDurationSeconds());
        relocateZombie(zombie);
    }
    private void handleLawnMower(Zombie zombie) {
        if (zombie.isDead() || zombie.currentPosition.x >= 0f) {
            return;
        }
        LawnMower mower = board.getLawnMower(zombie.lane);
        if (mower == null) {
            return;
        }
        int before = mower.destroyedZombies.size();
        mower.handleZombieAtEnd(zombie, session);
        statistics.recordLawnMowerKills(
                Math.max(0, mower.destroyedZombies.size() - before));
        if (!zombie.isDead() && session.status == GameStatus.RUNNING) {
            board.placeZombie(zombie, new ContinuousPosition(0f, zombie.lane));
        }
    }
    private void relocateZombie(Zombie zombie) {
        board.removeZombieEverywhere(zombie);
        int x = (int) Math.floor(zombie.currentPosition.x);
        GridPosition position = new GridPosition(x, zombie.lane);
        if (!board.isInside(position)) {
            return;
        }
        Tile tile = board.getTile(position);
        tile.addZombie(zombie);
        ExplosivePlant.resolveTrapContact(tile, zombie, session,
                projectileResolver);
        applySlipperyTile(zombie, tile, x);
    }
    private void applySlipperyTile(Zombie zombie, Tile tile, int x) {
        if (tile.slipDeltaRow == 0) {
            return;
        }
        int targetLane = zombie.lane + tile.slipDeltaRow;
        if (targetLane < 0 || targetLane >= board.rows) {
            return;
        }
        tile.moveZombieBySlip(zombie);
        board.removeZombieEverywhere(zombie);
        board.getTile(new GridPosition(x, zombie.lane)).addZombie(zombie);
    }
    private void removeDestroyedObjects(Tile tile) {
        for (Plant plant : new ArrayList<>(tile.getPlants())) {
            resolveExplodeONutArmor(plant);
            if (!plant.isAlive) {
                WallPlant.resolvePlantDeath(plant, session);
                resolveModifierPlantDeath(plant);
                if (plant.wasDestroyedByDamage()) {
                    statistics.recordPlantLost(plant);
                }
                tile.getPlants().remove(plant);
                forgetPlant(plant);
            }
        }
        for (Zombie zombie : new ArrayList<>(tile.getZombies())) {
            if (zombie.isDead()) {
                processZombieDeath(zombie);
                tile.removeZombie(zombie);
            }
        }
    }
    private void resolveExplodeONutArmor(Plant plant) {
        if (!(plant instanceof ExplodeONut)) {
            return;
        }
        ExplodeONut explodeONut = (ExplodeONut) plant;
        if (explodeONut.consumeArmorExplosionPending()) {
            ExplosivePlant.explodeArea(explodeONut, explodeONut.location,
                    explodeONut.attackPower, 1, board);
        }
    }
    private void resolveModifierPlantDeath(Plant plant) {
        if (plant.getNormalizedType().equals("torchwood")
                && plant.level >= 3 && plant.wasDestroyedByDamage()) {
            explodeTorchwoodLane(plant,
                    BalanceDefaults.TORCHWOOD_LEVEL_THREE_DEATH_DAMAGE);
        }
    }
    private void explodeTorchwoodLane(Plant plant, int damage) {
        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            zombie.lastDamageSource = plant;
            zombie.takeDamage(damage);
            zombie.melt();
        }
    }
    private void processZombieDeath(Zombie zombie) {
        if (!processedDeaths.add(zombie)) {
            return;
        }
        plantFoodInventory.addFromGlowingZombie(zombie);
        releaseZombieResources(zombie);
        statistics.recordZombieKilled(zombie, clock.currentTick);
        zombieBehaviorController.remove(zombie);
        LootType loot = lootDropService.rollLoot(zombie);
        session.registerLootDrop(loot, new GridPosition(
                Math.max(0, (int) Math.floor(zombie.currentPosition.x)),
                zombie.lane));
    }
    private void releaseZombieResources(Zombie zombie) {
        if (zombie instanceof RaZombie) {
            sunManager.addSuns(((RaZombie) zombie).releaseStolenSuns());
        }
        if (zombie instanceof TurquoiseZombie) {
            int released = ((TurquoiseZombie) zombie).releaseStolenSuns();
            sunManager.dropGroundSun(new GridPosition(
                            (int) Math.floor(zombie.currentPosition.x), zombie.lane),
                    released);
        }
        if (zombie instanceof BarrelRollerZombie) {
            Barrel barrel = ((BarrelRollerZombie) zombie).barrel;
            if (barrel != null && barrel.health > 0) {
                board.addLooseBarrel(barrel);
            }
        }
    }
    private void updateFrozenBlocks() {
        FrozenBlockTickSupport.update(board, clock);
    }
    private void updateCooldowns() {
        float elapsed = clock.getTickDurationSeconds();
        cooldowns.replaceAll((key, value) -> Math.max(0f, value - elapsed));
    }
    private void updateWaveState() {
        List<Wave> waves = waveController.getWaves();
        if (!waves.isEmpty()) {
            Wave last = waves.get(waves.size() - 1);
            if (!last.isFinalWave && last.getHealthLostRatio() >= 0.75f) {
                waveController.startNextWaveIfReady();
            }
        }
        if (waveController.isGameWon()
                && board.getAllAliveZombies().isEmpty()) {
            session.win();
        }
    }
    private List<Zombie> getAllZombies() {
        List<Zombie> zombies = new ArrayList<>();
        for (int row = 0; row < board.rows; row++) {
            zombies.addAll(board.getZombiesInLane(row));
        }
        return zombies;
    }
}
