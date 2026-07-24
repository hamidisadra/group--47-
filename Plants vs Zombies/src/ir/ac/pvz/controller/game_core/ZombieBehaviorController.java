package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.others.SunManager;
import ir.ac.pvz.model.support.*;
import ir.ac.pvz.model.zombies.*;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ZombieBehaviorController {

    private final Map<Zombie, Float> abilityElapsedSeconds;
    private final Map<ProspectorZombie, Boolean> reversedProspectors;
    private final Map<HunterZombie, Plant> hunterTargets;
    private final Random random;

    public ZombieBehaviorController() {
        this.abilityElapsedSeconds = new IdentityHashMap<>();
        this.reversedProspectors = new IdentityHashMap<>();
        this.hunterTargets = new IdentityHashMap<>();
        this.random = new Random();
    }

    public void update(Zombie zombie, GameSession session) {
        if (zombie == null || zombie.isDead() || session == null) {
            return;
        }
        updateImmediateBehavior(zombie, session);
        updateTimedBehavior(zombie, session);
    }

    public boolean handlePlantContact(Zombie zombie, Plant plant,
                                      GameSession session) {
        if (zombie == null || plant == null || session == null) {
            return false;
        }
        if (zombie instanceof DodoRiderZombie
                && ((DodoRiderZombie) zombie).canFlyOver(plant)) {
            flyOverPlant(zombie, plant, session.getBoard());
            return true;
        }
        if (zombie instanceof WizardZombie) {
            ((WizardZombie) zombie).onReachPlant(plant);
            return true;
        }
        if (zombie instanceof SquashZombie) {
            plant.takeDamage(plant.currentHp);
            zombie.forceDie();
            return true;
        }
        if (zombie instanceof SnorkelZombie) {
            ((SnorkelZombie) zombie).surfaceToEatPlant();
        }
        if (zombie instanceof ArcadeZombie) {
            ((ArcadeZombie) zombie).collideWith(plant);
            return true;
        }
        if (zombie instanceof PianistZombie) {
            ((PianistZombie) zombie).onReachPlant(plant);
            return true;
        }
        return false;
    }

    public void remove(Zombie zombie) {
        abilityElapsedSeconds.remove(zombie);
        if (zombie instanceof ProspectorZombie) {
            reversedProspectors.remove((ProspectorZombie) zombie);
        }
        if (zombie instanceof HunterZombie) {
            hunterTargets.remove((HunterZombie) zombie);
        }
    }

    private void updateImmediateBehavior(Zombie zombie, GameSession session) {
        if (zombie instanceof Gargantuar) {
            updateGargantuar((Gargantuar) zombie, session);
        }
        else if (zombie instanceof ProspectorZombie) {
            updateProspector((ProspectorZombie) zombie, session);
        }
        else if (zombie instanceof SnorkelZombie) {
            updateSnorkel((SnorkelZombie) zombie, session);
        }
        else if (zombie instanceof Troglobite) {
            updateTroglobite((Troglobite) zombie, session.getBoard());
        }
    }

    private void updateTimedBehavior(Zombie zombie, GameSession session) {
        float interval = getInterval(zombie, session);
        if (interval <= 0f) {
            return;
        }
        float elapsed = abilityElapsedSeconds.getOrDefault(zombie, 0f)
                + session.getClock().getTickDurationSeconds();
        abilityElapsedSeconds.put(zombie, elapsed);
        if (elapsed + 0.0001f < interval) {
            return;
        }
        if (executeTimedBehavior(zombie, session)) {
            abilityElapsedSeconds.put(zombie, 0f);
        }
    }

    private float getInterval(Zombie zombie, GameSession session) {
        if (zombie instanceof TurquoiseZombie || zombie instanceof RaZombie) {
            return 1f;
        }
        if (zombie instanceof OctopusZombie) {
            return ((OctopusZombie) zombie).throwOctopusCooldownSeconds;
        }
        if (zombie instanceof WizardZombie) {
            return ((WizardZombie) zombie).spellCooldownSeconds;
        }
        if (zombie instanceof PeashooterZombie) {
            return ((PeashooterZombie) zombie).shootCooldownSeconds;
        }
        if (zombie instanceof HunterZombie) {
            float cooldown = session.getStageConfig().getZombieAbilityCooldown(
                    zombie.getClass().getSimpleName());
            return cooldown > 0f ? cooldown
                    : session.getClock().getTickDurationSeconds();
        }
        return session.getStageConfig().getZombieAbilityCooldown(
                zombie.getClass().getSimpleName());
    }

    private boolean executeTimedBehavior(Zombie zombie, GameSession session) {
        if (zombie instanceof TurquoiseZombie) {
            return updateTurquoise((TurquoiseZombie) zombie, session);
        }
        if (zombie instanceof RaZombie) {
            return updateRa((RaZombie) zombie, session);
        }
        if (zombie instanceof TombRaiserZombie) {
            return raiseTombstones((TombRaiserZombie) zombie, session.getBoard());
        }
        if (zombie instanceof FishermanZombie) {
            return hookPlant((FishermanZombie) zombie, session.getBoard());
        }
        if (zombie instanceof KingZombie) {
            return promoteZombie((KingZombie) zombie, session.getBoard());
        }
        if (zombie instanceof PianistZombie) {
            return moveAdjacentZombies((PianistZombie) zombie, session.getBoard());
        }
        if (zombie instanceof PeashooterZombie) {
            return shootPea((PeashooterZombie) zombie, session);
        }
        if (zombie instanceof HunterZombie) {
            return updateHunter((HunterZombie) zombie, session);
        }
        if (zombie instanceof OctopusZombie) {
            OctopusZombie octopus = (OctopusZombie) zombie;
            if (throwOctopus(octopus, session)) {
                octopus.scheduleNextThrow();
                return true;
            }
            return false;
        }
        if (zombie instanceof WizardZombie) {
            WizardZombie wizard = (WizardZombie) zombie;
            if (wizard.transformRandomPlantToCat(session.getBoard())) {
                wizard.scheduleNextSpell();
                return true;
            }
            return false;
        }
        return false;
    }

    private void updateGargantuar(Gargantuar zombie, GameSession session) {
        boolean thrown = zombie.hasThrownImp();
        zombie.specialBehavior();
        if (!thrown && zombie.hasThrownImp()) {
            ImpZombie imp = new ImpZombie();
            session.getBoard().placeZombie(imp,
                    new ContinuousPosition(2f, zombie.lane));
        }
    }

    private void updateProspector(ProspectorZombie zombie, GameSession session) {
        if (!zombie.reversedByDynamite
                || reversedProspectors.containsKey(zombie)) {
            return;
        }
        reversedProspectors.put(zombie, true);
        session.getBoard().placeZombie(zombie,
                new ContinuousPosition(0f, zombie.lane));
    }


    private void updateTroglobite(Troglobite zombie, Board board) {
        int frontX = (int) Math.floor(zombie.currentPosition.x) - 1;
        Tile source = board.getTile(new GridPosition(frontX, zombie.lane));
        Tile destination = board.getTile(new GridPosition(frontX - 1, zombie.lane));
        if (source == null || destination == null
                || !(source.obstacle instanceof FrozenBlock)
                || destination.hasObstacle()) {
            return;
        }
        for (Plant plant : new ArrayList<>(destination.getPlants())) {
            plant.receiveInstantKill();
        }
        for (Zombie target : new ArrayList<>(destination.getZombies())) {
            if (target.isHypnotized) {
                target.forceDie();
            }
        }
        destination.obstacle = source.obstacle;
        destination.type = ir.ac.pvz.model.enums.TileType.FROZEN_TILE;
        destination.canPlant = false;
        source.obstacle = null;
        source.type = ir.ac.pvz.model.enums.TileType.FROSTBITE_GROUND;
        source.canPlant = true;
    }

    private void updateSnorkel(SnorkelZombie zombie, GameSession session) {
        Tile tile = getZombieTile(zombie, session.getBoard());
        if (tile != null && tile.isWater) {
            zombie.enterWater();
        }
    }

    private boolean updateTurquoise(TurquoiseZombie zombie,
                                    GameSession session) {
        if (zombie.hasFinishedStealing()) {
            return false;
        }
        if (!zombie.isStealingSun()
                && !hasPlantWithinFourTiles(zombie, session.getBoard())) {
            return false;
        }
        zombie.stealSunForOneSecond(session);
        return true;
    }

    private boolean updateRa(RaZombie zombie, GameSession session) {
        int capacity = zombie.getRemainingSunCapacity();
        if (capacity <= 0) {
            return false;
        }
        GridPosition position = new GridPosition(
                (int) Math.floor(zombie.currentPosition.x), zombie.lane);
        int stolen = session.getSunManager().pullGroundSunsToward(
                position, capacity);
        zombie.stealSun(stolen);
        return stolen > 0 || hasGroundSun(session.getSunManager());
    }

    private boolean hasGroundSun(SunManager sunManager) {
        for (Sun sun : sunManager.getActiveSuns()) {
            if (sun.isAlive && !sun.isFalling) {
                return true;
            }
        }
        return false;
    }

    private boolean raiseTombstones(TombRaiserZombie zombie, Board board) {
        List<Tile> candidates = emptyTombstoneTiles(board);
        if (candidates.isEmpty()) {
            return false;
        }
        java.util.Collections.shuffle(candidates, random);
        int count = Math.min(2, candidates.size());
        for (int index = 0; index < count; index++) {
            Tile tile = candidates.get(index);
            zombie.createTombstone(board, tile.position);
            tile.type = ir.ac.pvz.model.enums.TileType.TOMBSTONE;
        }
        return count > 0;
    }

    private List<Tile> emptyTombstoneTiles(Board board) {
        List<Tile> tiles = new ArrayList<>();
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));
                if (!tile.isWater && !tile.hasObstacle()
                        && tile.getPlants().isEmpty()) {
                    tiles.add(tile);
                }
            }
        }
        return tiles;
    }

    private boolean hookPlant(FishermanZombie zombie, Board board) {
        Plant target = nearestPlantToZombie(zombie, board);
        if (target == null) {
            return false;
        }
        int targetX = target.location.x + 1;
        if (targetX >= (int) Math.floor(zombie.currentPosition.x)) {
            zombie.throwAdjacentHookedPlant(target);
            return true;
        }
        GridPosition destination = new GridPosition(targetX, target.location.y);
        Tile destinationTile = board.getTile(destination);
        if (destinationTile == null || !destinationTile.getPlants().isEmpty()) {
            return false;
        }
        return board.movePlant(target, destination);
    }

    private Plant nearestPlantToZombie(Zombie zombie, Board board) {
        Plant nearest = null;
        for (Plant plant : board.getPlantsInLane(zombie.lane)) {
            if (!plant.isAlive || plant.isCatTransformed) {
                continue;
            }
            if (nearest == null || plant.location.x > nearest.location.x) {
                nearest = plant;
            }
        }
        return nearest;
    }

    private boolean promoteZombie(KingZombie king, Board board) {
        BasicZombie target = null;
        double bestDistance = Double.MAX_VALUE;
        for (Zombie zombie : board.getAllAliveZombies()) {
            if (!(zombie instanceof BasicZombie)
                    || zombie instanceof KnightZombie || zombie == king) {
                continue;
            }
            double horizontal = king.currentPosition.x
                    - zombie.currentPosition.x;
            double vertical = Math.abs(zombie.lane - king.lane);
            if (horizontal >= 0f && horizontal < 4f && vertical <= 1f
                    && horizontal + vertical < bestDistance) {
                target = (BasicZombie) zombie;
                bestDistance = horizontal + vertical;
            }
        }
        if (target == null) {
            return false;
        }
        ContinuousPosition position = new ContinuousPosition(
                target.currentPosition.x, target.lane);
        board.removeZombieEverywhere(target);
        target.isAlive = false;
        target.currentHealth = 0;
        KnightZombie knight = king.promoteNearbyBasicZombie();
        board.placeZombie(knight, position);
        return true;
    }

    private boolean moveAdjacentZombies(PianistZombie pianist, Board board) {
        boolean moved = false;
        for (Zombie zombie : new ArrayList<>(board.getAllAliveZombies())) {
            if (zombie == pianist) {
                continue;
            }
            List<Integer> lanes = adjacentLanes(zombie.lane, board.rows);
            if (!lanes.isEmpty()) {
                int target = lanes.get(random.nextInt(lanes.size()));
                pianist.moveZombieToAdjacentLane(zombie, target);
                board.placeZombie(zombie, new ContinuousPosition(
                        zombie.currentPosition.x, target));
                moved = true;
            }
        }
        return moved;
    }

    private List<Integer> adjacentLanes(int lane, int rows) {
        List<Integer> lanes = new ArrayList<>();
        if (lane > 0) {
            lanes.add(lane - 1);
        }
        if (lane + 1 < rows) {
            lanes.add(lane + 1);
        }
        return lanes;
    }

    /**
     * Fires a pea forward and lets it damage the plant it reaches.
     */
    private boolean shootPea(PeashooterZombie zombie, GameSession session) {
        Plant target = session.findNearestPlantAhead(zombie);
        if (target == null || target.location.x > zombie.currentPosition.x) {
            return false;
        }
        Projectile pea = zombie.shootAtNearestPlant(session);
        if (pea == null) {
            return false;
        }
        pea.hit(target);
        return true;
    }

    public void updateJalapeno(JalapenoZombie zombie, GameSession session) {
        if (session == null || zombie.exploded || zombie.isDead()) {
            return;
        }

        zombie.addTime(session.getClock().getTickDurationSeconds());
        if (!zombie.isReadyToExplode()) {
            return;
        }

        zombie.exploded = true;
        System.out.println("The jalapeno zombie exploded and burned row " + (zombie.lane + 1) + ".");

        for (int x = 0; x < session.getBoard().columns; x++) {
            Tile tile = session.getBoard().getTile(new GridPosition(x, zombie.lane));

            if (tile == null) {
                continue;
            }

            for (Plant plant : new java.util.ArrayList<>(tile.getPlants())) {
                plant.takeDamage(plant.currentHp);
            }
        }
        zombie.forceDie();
    }

    private boolean updateHunter(HunterZombie hunter, GameSession session) {
        Plant target = session.findNearestPlantAhead(hunter);
        if (target == null || target.isPermanentlyFrozen()
                || target.location.x > hunter.currentPosition.x) {
            hunterTargets.remove(hunter);
            return false;
        }
        Projectile projectile = hunter.throwIceAtNearestPlant(session);
        if (projectile == null) {
            hunterTargets.remove(hunter);
            return false;
        }
        hunterTargets.put(hunter, target);
        projectile.hit(target);
        if (!target.isPermanentlyFrozen()) {
            return true;
        }
        Tile tile = session.getBoard().getTile(target.location);
        if (tile != null && !(tile.obstacle instanceof FrozenBlock)) {
            tile.obstacle = new FrozenBlock(target,
                    session.getStageConfig().getZombieAbilityValue(
                            "hunterIceHealth"));
            tile.type = ir.ac.pvz.model.enums.TileType.FROZEN_TILE;
            tile.canPlant = false;
        }
        hunterTargets.remove(hunter);
        return true;
    }

    private boolean throwOctopus(OctopusZombie zombie, GameSession session) {
        Plant target = session.findNearestPlantAhead(zombie);
        if (target == null || target.isOctopusBlocked) {
            return false;
        }
        int health = session.getStageConfig()
                .getZombieAbilityValue("octopusBlockHealth");
        return zombie.throwOctopusAtPlant(target, health) != null;
    }

    private void flyOverPlant(Zombie zombie, Plant plant, Board board) {
        float destinationX = Math.max(-0.01f, plant.location.x - 1f);
        board.placeZombie(zombie, new ContinuousPosition(destinationX, zombie.lane));
    }

    private Tile getZombieTile(Zombie zombie, Board board) {
        return board.getTile(new GridPosition(
                (int) Math.floor(zombie.currentPosition.x), zombie.lane));
    }

    private boolean hasPlantWithinFourTiles(Zombie zombie, Board board) {
        for (Plant plant : board.getAllPlants()) {
            double deltaX = plant.location.x - zombie.currentPosition.x;
            double deltaY = plant.location.y - zombie.lane;
            if (plant.isAlive && Math.sqrt(deltaX * deltaX + deltaY * deltaY) <= 4d) {
                return true;
            }
        }
        return false;
    }
}