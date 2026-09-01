package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
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
    private final Map<Class<? extends Zombie>, ContactBehavior> contactBehaviors;
    private final Map<Class<? extends Zombie>, TickBehavior> immediateBehaviors;
    private final Map<Class<? extends Zombie>, TimedBehavior> timedBehaviors;
    private final Map<Class<? extends Zombie>, IntervalProvider> intervalProviders;
    private final Random random;
    private final ZombieDefinitionRepository zombieRepository;
    public ZombieBehaviorController() {
        this(new Random(), ZombieDataRepository.getInstance());
    }

    public ZombieBehaviorController(
            Random random, ZombieDefinitionRepository zombieRepository) {
        if (random == null || zombieRepository == null) {
            throw new IllegalArgumentException(
                    "Zombie behavior dependencies are required.");
        }
        this.abilityElapsedSeconds = new IdentityHashMap<>();
        this.reversedProspectors = new IdentityHashMap<>();
        this.hunterTargets = new IdentityHashMap<>();
        this.contactBehaviors = new java.util.LinkedHashMap<>();
        this.immediateBehaviors = new java.util.LinkedHashMap<>();
        this.timedBehaviors = new java.util.LinkedHashMap<>();
        this.intervalProviders = new java.util.LinkedHashMap<>();
        this.random = random;
        this.zombieRepository = zombieRepository;
        registerBehaviors();
    }

    public void update(Zombie zombie, GameSession session) {
        if (zombie == null || zombie.isDead() || session == null) {
            return;
        }
        for (ZombieAbility ability : new ArrayList<>(zombie.abilities)) {
            ability.onTick(zombie, session,
                    session.getClock().getTickDurationSeconds());
        }
        startTurquoiseStealing(zombie, session);
        updateImmediateBehavior(zombie, session);
        updateTimedBehavior(zombie, session);
    }

    public boolean handlePlantContact(Zombie zombie, Plant plant,
                                      GameSession session) {
        if (zombie == null || plant == null || session == null) {
            return false;
        }
        for (ZombieAbility ability : new ArrayList<>(zombie.abilities)) {
            if (ability.onPlantContact(zombie, plant, session)) {
                return true;
            }
        }
        ContactBehavior behavior = contactBehaviors.get(zombie.getClass());
        return behavior != null && behavior.handle(zombie, plant, session);
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

    private void startTurquoiseStealing(Zombie zombie, GameSession session) {
        if (!(zombie instanceof TurquoiseZombie)) {
            return;
        }
        TurquoiseZombie turquoise = (TurquoiseZombie) zombie;
        if (!turquoise.isStealingSun() && !turquoise.hasFinishedStealing()
                && hasPlantWithinFourTiles(turquoise, session.getBoard())) {
            turquoise.startStealing();
        }
    }

    private void updateImmediateBehavior(Zombie zombie, GameSession session) {
        TickBehavior behavior = immediateBehaviors.get(zombie.getClass());
        if (behavior != null) {
            behavior.update(zombie, session);
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
        IntervalProvider provider = intervalProviders.get(zombie.getClass());
        if (provider == null) {
            return session.getStageConfig().getZombieAbilityCooldown(
                    zombie.getType());
        }
        return provider.get(zombie, session);
    }

    private boolean executeTimedBehavior(Zombie zombie, GameSession session) {
        TimedBehavior behavior = timedBehaviors.get(zombie.getClass());
        return behavior != null && behavior.execute(zombie, session);
    }

    private void registerBehaviors() {
        registerContactBehaviors();
        registerImmediateBehaviors();
        registerIntervalProviders();
        registerTimedBehaviors();
    }

    private void registerContactBehaviors() {
        contactBehaviors.put(SquashZombie.class, (zombie, plant, session) -> {
            plant.takeDamage(plant.currentHp);
            zombie.forceDie();
            return true;
        });
        contactBehaviors.put(ArcadeZombie.class, (zombie, plant, session) -> {
            ((ArcadeZombie) zombie).collideWith(plant);
            return true;
        });
        contactBehaviors.put(PianistZombie.class, (zombie, plant, session) -> {
            ((PianistZombie) zombie).onReachPlant(plant);
            return true;
        });
    }

    private void registerImmediateBehaviors() {
        immediateBehaviors.put(Gargantuar.class, (zombie, session) ->
                updateGargantuar((Gargantuar) zombie, session));
        immediateBehaviors.put(ProspectorZombie.class, (zombie, session) ->
                updateProspector((ProspectorZombie) zombie, session));
        immediateBehaviors.put(Troglobite.class, (zombie, session) ->
                updateTroglobite((Troglobite) zombie, session.getBoard()));
    }

    private void registerIntervalProviders() {
        intervalProviders.put(PeashooterZombie.class, (zombie, session) ->
                ((PeashooterZombie) zombie).shootCooldownSeconds);
        intervalProviders.put(TurquoiseZombie.class,
                (zombie, session) -> 1f);
        intervalProviders.put(RaZombie.class, (zombie, session) -> 1f);
        intervalProviders.put(OctopusZombie.class, (zombie, session) ->
                ((OctopusZombie) zombie).throwOctopusCooldownSeconds);
        intervalProviders.put(HunterZombie.class, (zombie, session) ->
                session.getStageConfig().getZombieAbilityCooldown(
                        zombie.getType()));
    }

    private void registerTimedBehaviors() {
        timedBehaviors.put(PeashooterZombie.class, (zombie, session) ->
                shootPea((PeashooterZombie) zombie, session));
        timedBehaviors.put(TurquoiseZombie.class, (zombie, session) ->
                updateTurquoise((TurquoiseZombie) zombie, session));
        timedBehaviors.put(RaZombie.class, (zombie, session) ->
                updateRa((RaZombie) zombie, session));
        timedBehaviors.put(TombRaiserZombie.class, (zombie, session) ->
                raiseTombstones((TombRaiserZombie) zombie,
                        session.getBoard()));
        timedBehaviors.put(FishermanZombie.class, (zombie, session) ->
                hookPlant((FishermanZombie) zombie, session.getBoard()));
        timedBehaviors.put(KingZombie.class, (zombie, session) ->
                promoteZombie((KingZombie) zombie, session.getBoard()));
        timedBehaviors.put(PianistZombie.class, (zombie, session) ->
                moveAdjacentZombies((PianistZombie) zombie,
                        session.getBoard()));
        timedBehaviors.put(HunterZombie.class, (zombie, session) ->
                updateHunter((HunterZombie) zombie, session));
        timedBehaviors.put(OctopusZombie.class, (zombie, session) -> {
            OctopusZombie octopus = (OctopusZombie) zombie;
            if (!throwOctopus(octopus, session)) {
                return false;
            }
            octopus.scheduleNextThrow();
            return true;
        });
    }

    private void updateGargantuar(Gargantuar zombie, GameSession session) {
        boolean thrown = zombie.hasThrownImp();
        zombie.specialBehavior();
        if (!thrown && zombie.hasThrownImp()) {
            session.spawnConfiguredZombie("ImpZombie",
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
        FrozenBlock block = (FrozenBlock) source.obstacle;
        block.relocateContent(source, destination);
        destination.obstacle = block;
        destination.type = ir.ac.pvz.model.enums.TileType.FROZEN_TILE;
        destination.canPlant = false;
        source.obstacle = null;
        source.restoreNativeGround();
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
        return ZombieBoardBehaviors.raiseTombstones(zombie, board, random,
                zombieRepository.getByZombieType(zombie.getType()));
    }

    private boolean hookPlant(FishermanZombie zombie, Board board) {
        return ZombieBoardBehaviors.hookPlant(zombie, board);
    }

    private boolean promoteZombie(KingZombie king, Board board) {
        return ZombieBoardBehaviors.promoteZombie(king, board);
    }

    private boolean moveAdjacentZombies(PianistZombie pianist, Board board) {
        return ZombieBoardBehaviors.moveAdjacentZombies(pianist, board, random);
    }

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
        System.out.println("The jalapeno zombie exploded and burned row "
                + (zombie.lane + 1) + ".");
        for (int x = 0; x < session.getBoard().columns; x++) {
            Tile tile = session.getBoard().getTile(
                    new GridPosition(x, zombie.lane));
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
            tile.obstacle = new FrozenBlock(target, session.getStageConfig().getZombieAbilityValue(
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
    @FunctionalInterface
    private interface ContactBehavior {
        boolean handle(Zombie zombie, Plant plant, GameSession session);
    }
    @FunctionalInterface
    private interface TickBehavior {
        void update(Zombie zombie, GameSession session);
    }
    @FunctionalInterface
    private interface TimedBehavior {
        boolean execute(Zombie zombie, GameSession session);
    }
    @FunctionalInterface
    private interface IntervalProvider {
        float get(Zombie zombie, GameSession session);
    }
}