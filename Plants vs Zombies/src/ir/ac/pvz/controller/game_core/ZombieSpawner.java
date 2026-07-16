package com.pvz.game;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.Board;
import com.pvz.model.support.ContinuousPosition;
import com.pvz.model.support.GridPosition;
import com.pvz.model.support.ZombieBaseStats;
import com.pvz.model.support.ZombieDataRepository;
import com.pvz.model.zombies.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ZombieSpawner {

    private final Board board;
    private final Random random;
    private final StageConfig stageConfig;
    private List<String> allowedZombieTypes;

    public ZombieSpawner(Board board) {
        this(board, StageConfig.unconfigured(
                board == null ? null : board.seasonType));
    }

    public ZombieSpawner(Board board, List<String> allowedZombieTypes) {
        this(board, new StageConfig(board == null ? null : board.seasonType,
                0, 0, 0.25f, 2f, allowedZombieTypes));
    }

    public ZombieSpawner(Board board, StageConfig stageConfig) {
        this.board = board;
        this.stageConfig = stageConfig == null
                ? StageConfig.unconfigured(board == null ? null : board.seasonType)
                : stageConfig;
        this.random = new Random();
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
        String normalized = normalize(type);
        switch (normalized) {
            case "basiczombie": case "coneheadzombie":
            case "bucketheadzombie": case "knightzombie":
            case "blockheadzombie": case "gargantuar":
            case "impzombie": case "footballzombie":
            case "arcadezombie": case "parasolzombie":
            case "turquoisezombie": case "prospectorzombie":
            case "pianistzombie": case "newspaperzombie":
            case "barrelrollerzombie": case "razombie":
            case "explorerzombie": case "tombraiserzombie":
            case "dodoriderzombie": case "hunterzombie":
            case "troglobite": case "fishermanzombie":
            case "snorkelzombie": case "octopuszombie":
            case "jesterzombie": case "wizardzombie":
            case "kingzombie": case "impdragon":
                return true;
            default:
                return false;
        }
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
        String normalized = normalize(type);
        Zombie zombie;
        switch (normalized) {
            case "basiczombie": zombie = new BasicZombie(); break;
            case "coneheadzombie": zombie = new ConeheadZombie(); break;
            case "bucketheadzombie": zombie = new BucketheadZombie(); break;
            case "knightzombie": zombie = new KnightZombie(); break;
            case "blockheadzombie": zombie = new BlockheadZombie(); break;
            case "gargantuar": zombie = new Gargantuar(); break;
            case "impzombie": zombie = new ImpZombie(); break;
            case "footballzombie": zombie = new FootballZombie(); break;
            case "arcadezombie": zombie = new ArcadeZombie(); break;
            case "parasolzombie": zombie = new ParasolZombie(); break;
            case "turquoisezombie": zombie = new TurquoiseZombie(); break;
            case "prospectorzombie": zombie = new ProspectorZombie(); break;
            case "pianistzombie": zombie = new PianistZombie(); break;
            case "newspaperzombie": zombie = new NewspaperZombie(); break;
            case "barrelrollerzombie": zombie = createBarrelRollerZombie(); break;
            case "razombie": zombie = new RaZombie(); break;
            case "explorerzombie": zombie = new ExplorerZombie(); break;
            case "tombraiserzombie": zombie = new TombRaiserZombie(); break;
            case "dodoriderzombie": zombie = new DodoRiderZombie(); break;
            case "hunterzombie": zombie = new HunterZombie(); break;
            case "troglobite": zombie = new Troglobite(); break;
            case "fishermanzombie": zombie = new FishermanZombie(); break;
            case "snorkelzombie": zombie = new SnorkelZombie(); break;
            case "octopuszombie": zombie = new OctopusZombie(); break;
            case "jesterzombie": zombie = new JesterZombie(); break;
            case "wizardzombie": zombie = new WizardZombie(); break;
            case "kingzombie": zombie = new KingZombie(); break;
            case "impdragon": zombie = new ImpDragon(); break;
            default: return null;
        }
        if (zombie != null && !normalized.equals("barrelrollerzombie")) {
            ZombieDataRepository.getInstance().applyTo(zombie, type);
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
        int weight = ZombieDataRepository.getInstance().getWeight(type);
        return weight > 0 ? weight : 1;
    }

    private Zombie createBarrelRollerZombie() {
        ZombieBaseStats stats = stageConfig.getZombieBaseStats(
                "BarrelRollerZombie");
        return new BarrelRollerZombie(stats);
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
}
