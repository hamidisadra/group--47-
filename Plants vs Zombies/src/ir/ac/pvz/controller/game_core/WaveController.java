package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Zombie;
import java.util.ArrayList;
import java.util.List;

public class WaveController {
    public int currentWaveNumber;
    public int baseWaveCost;
    public float waveGrowthRate;
    public float finalWaveMultiplier;
    private final List<Wave> waves;
    private final ZombieSpawner zombieSpawner;
    private final int totalWaves;
    private final List<Integer> explicitWaveCosts;
    public WaveController(int baseWaveCost, float waveGrowthRate,
                          ZombieSpawner zombieSpawner) {
        this(baseWaveCost, waveGrowthRate, 2f, 0,
                new ArrayList<>(), zombieSpawner);
    }
    public WaveController(int baseWaveCost, float waveGrowthRate,
                          float finalWaveMultiplier, int totalWaves,
                          ZombieSpawner zombieSpawner) {
        this(baseWaveCost, waveGrowthRate, finalWaveMultiplier, totalWaves,
                new ArrayList<>(), zombieSpawner);
    }
    public WaveController(int baseWaveCost, float waveGrowthRate,
                          float finalWaveMultiplier, int totalWaves,
                          List<Integer> explicitWaveCosts,
                          ZombieSpawner zombieSpawner) {
        this.currentWaveNumber = 0;
        this.baseWaveCost = baseWaveCost;
        this.waveGrowthRate = waveGrowthRate;
        this.finalWaveMultiplier = finalWaveMultiplier;
        this.totalWaves = Math.max(0, totalWaves);
        if (explicitWaveCosts == null) {
            this.explicitWaveCosts = new ArrayList<>();
        } else {
            this.explicitWaveCosts = new ArrayList<>(explicitWaveCosts);
        }
        this.waves = new ArrayList<>();
        this.zombieSpawner = zombieSpawner;
    }
    public int calculateWaveCost(int number) {
        if (number <= 0) {
            return 0;
        }
        if (!explicitWaveCosts.isEmpty() && number <= explicitWaveCosts.size()) {
            int configured = explicitWaveCosts.get(number - 1);
            if (number == 1) {
                return Math.max(1000, configured);
            }
            int previousCost = calculateWaveCost(number - 1);
            if (totalWaves > 0 && number == totalWaves) {
                return Math.max(configured, previousCost * 2);
            }
            return Math.max(configured, previousCost + 500);
        }
        int firstCost = Math.max(1000, baseWaveCost);
        if (totalWaves > 0 && number == totalWaves) {
            int previousCost = firstCost;
            if (number > 1) {
                previousCost = calculateWaveCost(number - 1);
            }
            return Math.round(previousCost * Math.max(2f,
                    finalWaveMultiplier));
        }
        if (number == 1) {
            return firstCost;
        }
        int previousCost = calculateWaveCost(number - 1);
        int percentageCost = Math.round(previousCost
                * (1f + Math.max(0.25f, waveGrowthRate)));
        return Math.max(percentageCost, previousCost + 500);
    }
    public void startNextWaveIfReady() {
        if (totalWaves == 0) {
            return;
        }
        Wave previous = getCurrentWave();
        if (previous != null && (previous.isFinalWave
                || previous.getHealthLostRatio()
                < previous.previousHealthLossThreshold)) {
            return;
        }
        int number = currentWaveNumber + 1;
        if (totalWaves > 0 && number >= totalWaves) {
            startFinalWave();
            return;
        }
        startWave(number, calculateWaveCost(number), false);
    }
    public void startFinalWave() {
        if (totalWaves == 0) {
            return;
        }
        Wave previous = getCurrentWave();
        if (previous != null && previous.getHealthLostRatio()
                < previous.previousHealthLossThreshold) {
            return;
        }
        if (previous != null && previous.isFinalWave) {
            return;
        }
        int number = currentWaveNumber + 1;
        startWave(number, calculateWaveCost(number), true);
    }
    public boolean isGameWon() {
        Wave last = getCurrentWave();
        return last != null && last.isFinalWave && last.isCleared();
    }
    public String startWaveMessage(Wave wave) {
        return "Wave " + wave.waveNumber + " started.";
    }
    public String finalWaveMessage() {
        return "The final wave has come.";
    }
    public List<Wave> getWaves() {
        return waves;
    }
    public Wave getCurrentWave() {
        if (waves.isEmpty()) {
            return null;
        }
        return waves.get(waves.size() - 1);
    }
    public int getTotalWaves() {
        return totalWaves;
    }
    private void startWave(int number, int cost, boolean finalWave) {
        if (zombieSpawner != null) {
            zombieSpawner.requireExactCost(cost);
        }
        if (finalWave) {
            System.out.println(finalWaveMessage());
        }
        else {
            System.out.println("Wave " + number + " started.");
        }
        List<Zombie> zombies = spawn(cost);
        Wave wave = new Wave(number, cost, 0, finalWave, zombies);
        waves.add(wave);
        currentWaveNumber = number;
        printSpawnMessages(wave);
    }
    private List<Zombie> spawn(int cost) {
        if (zombieSpawner == null) {
            return new ArrayList<>();
        }
        return zombieSpawner.spawnRandomZombiesUntilCost(cost);
    }
    private void printSpawnMessages(Wave wave) {
        for (Zombie zombie : wave.zombies) {
            System.out.println("Zombie " + zombie.getType()
                    + " spawned at wave " + wave.waveNumber + " in lane "
                    + (zombie.lane + 1) + " which costed "
                    + zombie.waveCost + ".");
        }
    }
}
