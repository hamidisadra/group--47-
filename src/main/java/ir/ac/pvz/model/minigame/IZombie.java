package ir.ac.pvz.model.minigame;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IZombie extends MiniGame {
    public static final String SUN_PRODUCER = "sun-producer";
    public static final int LANES = 5;

    private static final String[][] LEVEL_ROSTERS = {
        { "basic", "conehead", "buckethead", "football", "imp" },
        { "basic", "newspaper", "knight", "arcade", "parasol" },
        { "basic", "blockhead", "gargantuar", "jester", "wizard" }
    };

    private static final int STARTING_SUN = 150;

    private final int level;
    private int sunAmount;
    private final List<Brain> brains;
    private final List<SunProducerZombie> sunProducerZombies;
    private final Map<String, Integer> zombieCosts;

    public IZombie(int stageNumber) {
        super("I, Zombie", stageNumber);
        this.level = Math.max(1, Math.min(LEVEL_ROSTERS.length, stageNumber));
        this.sunAmount = STARTING_SUN;
        this.brains = new ArrayList<>();
        this.sunProducerZombies = new ArrayList<>();
        this.zombieCosts = new LinkedHashMap<>();

        for (int row = 1; row <= LANES; row++) {
            brains.add(new Brain(row));
            sunProducerZombies.add(new SunProducerZombie(row));
        }

        for (String type : LEVEL_ROSTERS[level - 1]) {
            zombieCosts.put(type, costFor(type));
        }
    }

    private static int costFor(String type) {
        switch (type) {
            case "basic":
                return 50;

            case "conehead":
            case "newspaper":
            case "blockhead":
                return 75;

            case "imp":
            case "parasol":
                return 100;

            case "buckethead":
            case "knight":
            case "jester":
                return 125;

            case "football":
            case "arcade":
            case "wizard":
                return 150;

            case "gargantuar":
                return 250;

            default:
                return 100;
        }
    }

    public int getLevel() {
        return level;
    }

    public List<String> getAvailableZombies() {
        return new ArrayList<>(zombieCosts.keySet());
    }

    public boolean isPlaceable(String type) {
        return zombieCosts.containsKey(type);
    }

    public int getCost(String type) {
        Integer cost = zombieCosts.get(type);
        return cost == null ? 0 : cost;
    }

    public int getSunAmount() {
        return sunAmount;
    }

    public List<Brain> getBrains() {
        return brains;
    }

    public List<SunProducerZombie> getSunProducerZombies() {
        return sunProducerZombies;
    }

    public boolean placeZombie(String type, int row) {
        if (!isPlaceable(type)) {
            return false;
        }

        int cost = getCost(type);
        if (cost > sunAmount) {
            return false;
        }

        sunAmount -= cost;
        return true;
    }

    public void collectSunFromProducers() {
        for (SunProducerZombie zombie : sunProducerZombies) {
            if (zombie.isAlive()) {
                sunAmount += zombie.produceSun();
            }
        }
    }

    public void onSunProducerKilled(int row) {
        for (SunProducerZombie zombie : sunProducerZombies) {
            if (zombie.getRow() == row) {
                zombie.kill();
            }
        }
    }

    public int getCheapestCost() {
        int cheapest = Integer.MAX_VALUE;

        for (Integer cost : zombieCosts.values()) {
            cheapest = Math.min(cheapest, cost);
        }

        return cheapest == Integer.MAX_VALUE ? 0 : cheapest;
    }

    public boolean canAffordAnyZombie() {
        return !zombieCosts.isEmpty() && sunAmount >= getCheapestCost();
    }

    public void eatBrain(int row) {
        for (Brain brain : brains) {
            if (brain.getRow() == row) {
                brain.eat();
            }
        }
    }

    public int getRemainingBrains() {
        int remaining = 0;

        for (Brain brain : brains) {
            if (!brain.isEaten()) {
                remaining++;
            }
        }

        return remaining;
    }

    public boolean checkWinCondition() {
        return getRemainingBrains() == 0;
    }

    public boolean checkLoseCondition(boolean anyZombieOnLawn) {
        return !anyZombieOnLawn && !canAffordAnyZombie();
    }

    public boolean checkLoseCondition() {
        return checkLoseCondition(false);
    }
}
