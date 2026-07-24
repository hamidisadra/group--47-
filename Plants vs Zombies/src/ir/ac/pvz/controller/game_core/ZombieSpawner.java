package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.ZombieBaseStats;
import ir.ac.pvz.model.support.ZombieDataRepository;
import ir.ac.pvz.model.support.ZombieDefinition;
import ir.ac.pvz.model.support.ZombieDefinitionRepository;
import ir.ac.pvz.model.support.ArmorDataRepository;
import ir.ac.pvz.model.support.ArmorDefinitionRepository;
import ir.ac.pvz.model.zombies.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

public final class ZombieSpawner {
    private final Board board;
    private final RandomGenerator random;
    private final StageConfig stageConfig;
    private List<String> allowedZombieTypes;
    private final ZombieTypeRegistry typeRegistry;
    private final ZombieDefinitionRepository zombieRepository;
    public ZombieSpawner(Board board) {
        this(board, StageConfig.unconfigured(seasonOf(board)));
    }

    public ZombieSpawner(Board board, List<String> allowedZombieTypes) {
        this(board, new StageConfig(seasonOf(board),
                0, 0, 0.25f, 2f, allowedZombieTypes));
    }
    public ZombieSpawner(Board board, StageConfig stageConfig) {
        this(board, stageConfig, defaultRandom(stageConfig));
    }

    /**
     * The scored game fixes the seed so every player faces the same zombies
     * on the same day. Without a seed the spawner stays fully random.
     */
    private static RandomGenerator defaultRandom(StageConfig stageConfig) {
        if (stageConfig != null && stageConfig.getRandomSeed() != null) {
            return new Random(stageConfig.getRandomSeed());
        }
        return new Random();
    }

    public ZombieSpawner(Board board, StageConfig stageConfig,
                         RandomGenerator random) {
        this(board, stageConfig, random, ZombieDataRepository.getInstance(),
                ArmorDataRepository.getInstance());
    }
    public ZombieSpawner(
            Board board, StageConfig stageConfig, RandomGenerator random,
            ZombieDefinitionRepository zombieRepository,
            ArmorDefinitionRepository armorRepository) {
        this.board = board;
        if (stageConfig == null) {
            this.stageConfig = StageConfig.unconfigured(seasonOf(board));
        }
        else {
            this.stageConfig = stageConfig;
        }
        if (random == null) {
            throw new IllegalArgumentException("Random generator cannot be null.");
        }
        if (zombieRepository == null || armorRepository == null) {
            throw new IllegalArgumentException(
                    "Zombie repositories cannot be null.");
        }
        this.random = random;
        this.zombieRepository = zombieRepository;
        this.typeRegistry = new ZombieTypeRegistry(
                this.stageConfig, zombieRepository, armorRepository);
        this.allowedZombieTypes = filterAllowedZombieTypes(
                this.stageConfig.allowedZombieTypes);
    }
    public List<Zombie> spawnRandomZombiesUntilCost(int waveCost) {
        requireExactCost(waveCost);
        if (waveCost <= 0) {
            return new ArrayList<>();
        }
        List<Zombie> zombies = new ArrayList<>();
        int remaining = waveCost;
        boolean[] reachable = calculateReachableCosts(waveCost);
        while (remaining > 0) {
            List<String> candidates = getFillableCandidates(remaining, reachable);
            if (candidates.isEmpty()) {
                throw new IllegalStateException("No exact zombie combination for wave cost "
                        + waveCost + ".");
            }
            String type = chooseWeightedType(candidates);
            Zombie zombie = createZombie(type);
            int lane = chooseRandomLane(board);
            ContinuousPosition position = new ContinuousPosition(board.columns - 1, lane);
            placeZombie(zombie, position);
            zombies.add(zombie);
            remaining -= zombie.waveCost;
        }
        return zombies;
    }
    public Zombie spawnZombie(String type, ContinuousPosition position) {
        Zombie zombie = createZombie(type);
        if (zombie != null && position != null) {
            placeZombie(zombie, position);
        }
        return zombie;
    }
    public int chooseRandomLane(Board targetBoard) {
        if (targetBoard == null || targetBoard.rows <= 0) {
            return 0;
        }
        return random.nextInt(targetBoard.rows);
    }
    public int normalizeWaveCost(int requestedCost) {
        requireExactCost(requestedCost);
        return requestedCost;
    }
    public boolean canBuildExactCost(int requestedCost) {
        if (requestedCost <= 0 || allowedZombieTypes.isEmpty()) {
            return false;
        }
        boolean[] reachable = calculateReachableCosts(requestedCost);
        return reachable[requestedCost];
    }
    public void requireExactCost(int requestedCost) {
        if (!canBuildExactCost(requestedCost)) {
            throw new IllegalStateException("No exact zombie combination for wave cost "
                    + requestedCost + ".");
        }
    }
    public void setAllowedZombieTypes(List<String> types) {
        this.allowedZombieTypes = filterAllowedZombieTypes(types);
    }
    private List<String> filterAllowedZombieTypes(List<String> types) {
        List<String> filtered = new ArrayList<>();
        if (types == null) {
            return filtered;
        }
        for (String type : types) {
            if (isSupportedType(type)) {
                filtered.add(type);
            }
        }
        return filtered;
    }
    public boolean isSupportedType(String type) {
        return typeRegistry.contains(type);
    }
    public boolean hasRequiredBaseData(String type) {
        if (!isSupportedType(type)) {
            return false;
        }
        return true;
    }
    public List<String> getAllowedZombieTypes() {
        return Collections.unmodifiableList(allowedZombieTypes);
    }
    public Zombie createZombie(String type) {
        Zombie zombie = typeRegistry.create(type);
        if (zombie == null) {
            return null;
        }
        if (zombie != null) {
            zombie.setIdentity(canonicalType(type), canonicalType(type));
        }
        return zombie;
    }
    private boolean[] calculateReachableCosts(int limit) {
        boolean[] reachable = new boolean[Math.max(0, limit) + 1];
        reachable[0] = true;
        for (int cost = 1; cost <= limit; cost++) {
            for (String type : allowedZombieTypes) {
                Zombie zombie = createZombie(type);
                if (zombie != null && zombie.waveCost > 0
                        && zombie.waveCost <= cost && selectionWeight(type) > 0
                        && reachable[cost - zombie.waveCost]) {
                    reachable[cost] = true;
                    break;
                }
            }
        }
        return reachable;
    }
    private List<String> getFillableCandidates(int remaining, boolean[] reachable) {
        List<String> candidates = new ArrayList<>();
        for (String type : allowedZombieTypes) {
            Zombie zombie = createZombie(type);
            if (zombie != null && zombie.waveCost > 0
                    && selectionWeight(type) > 0 && zombie.waveCost <= remaining
                    && reachable[remaining - zombie.waveCost]) {
                candidates.add(type);
            }
        }
        return candidates;
    }
    private String chooseWeightedType(List<String> candidates) {
        int totalWeight = candidates.stream().mapToInt(this::selectionWeight).sum();
        int roll = random.nextInt(totalWeight);
        for (String type : candidates) {
            roll -= selectionWeight(type);
            if (roll < 0) {
                return type;
            }
        }
        return candidates.get(candidates.size() - 1);
    }
    private int getMaximumAllowedCost() {
        int maximum = 0;
        for (String type : allowedZombieTypes) {
            Zombie zombie = createZombie(type);
            if (zombie != null && selectionWeight(type) > 0) {
                maximum = Math.max(maximum, zombie.waveCost);
            }
        }
        return maximum;
    }
    private int selectionWeight(String type) {
        ZombieDefinition definition = zombieRepository.getByZombieType(type);
        int weight = 0;
        if (definition != null) {
            weight = definition.weight;
        }
        if (weight > 0) {
            return weight;
        }
        return 1;
    }
    private static ir.ac.pvz.model.enums.SeasonType seasonOf(Board board) {
        if (board == null) {
            return null;
        }
        return board.seasonType;
    }
    private void placeZombie(Zombie zombie, ContinuousPosition position) {
        zombie.currentPosition = position;
        zombie.positionX = position.x;
        zombie.positionY = position.y;
        zombie.lane = position.y;
        synchronizeAccessoryPosition(zombie, position);
        GridPosition tilePosition = new GridPosition((int) position.x, position.y);
        if (board != null && board.isInside(tilePosition)) {
            board.getTile(tilePosition).addZombie(zombie);
        }
    }
    private void synchronizeAccessoryPosition(Zombie zombie,
                                              ContinuousPosition position) {
        if (zombie instanceof ArcadeZombie) {
            ((ArcadeZombie) zombie).arcadeMachine.position =
                    new ContinuousPosition(position.x - 0.2f, position.y);
        }
        if (zombie instanceof BarrelRollerZombie) {
            ((BarrelRollerZombie) zombie).barrel.position =
                    new ContinuousPosition(position.x - 0.2f, position.y);
        }
    }
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
    private String canonicalType(String value) {
        if (value == null || value.isBlank()) {
            return "Zombie";
        }
        return value.trim();
    }
}
