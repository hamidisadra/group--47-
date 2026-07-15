package ir.ac.pvz.model.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IZombie extends MiniGame {
    private int sunAmount;
    private List<Brain> brains;
    private List<SunProducerZombie> sunProducerZombies;
    private Map<String, Integer> zombieCosts;

    public IZombie(int stageNumber) {
        super("I, Zombie", stageNumber);
        this.sunAmount = 150;
        this.brains = new ArrayList<>();
        this.sunProducerZombies = new ArrayList<>();
        this.zombieCosts = new HashMap<>();

        for (int row = 1; row <= 5; row++) {
            brains.add(new Brain(row));
        }

        zombieCosts.put("basic", 50);
        zombieCosts.put("conehead", 75);
        zombieCosts.put("buckethead", 125);
        zombieCosts.put("sun-producer", 100);
        zombieCosts.put("gargantuar", 200);
    }

    public int getSunAmount() {
        return sunAmount;
    }

    public List<Brain> getBrains() {
        return brains;
    }

    public boolean placeZombie(String type, int row) {
        Integer cost = zombieCosts.get(type);
        if (cost == null || cost > sunAmount) {
            return false;
        }
        sunAmount -= cost;
        if (type.equals("sun-producer")) {
            sunProducerZombies.add(new SunProducerZombie(row));
        }
        return true;
    }

    public void collectSunFromProducers() {
        for (SunProducerZombie zombie : sunProducerZombies) {
            sunAmount += zombie.produceSun();
        }
    }

    public void eatBrain(int row) {
        for (Brain brain : brains) {
            if (brain.getRow() == row) {
                brain.eat();
            }
        }
    }

    public boolean checkWinCondition() {
        for (Brain brain : brains) {
            if (!brain.isEaten()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkLoseCondition() {
        return false;
    }
}
