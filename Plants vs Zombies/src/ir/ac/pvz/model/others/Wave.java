package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Zombie;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    public int waveNumber;
    public int waveCost;
    public int delayTicks;
    public boolean isFinalWave;
    public List<Zombie> zombies;
    public float previousWaveHealthLostRatio;
    public float previousHealthLossThreshold;

    private final int initialTotalHealth;

    public Wave(int waveNumber, int waveCost, int delayTicks,
                boolean isFinalWave, List<Zombie> zombies) {
        this.waveNumber = waveNumber;
        this.waveCost = waveCost;
        this.delayTicks = delayTicks;
        this.isFinalWave = isFinalWave;
        this.zombies = zombies == null ? new ArrayList<>() : zombies;
        this.previousWaveHealthLostRatio = 0f;
        this.previousHealthLossThreshold = 0.75f;
        this.initialTotalHealth = totalCurrentHealth();
    }

    public boolean canStartAfter(Wave previous) {
        if (previous == null) {
            return true;
        }
        previous.previousWaveHealthLostRatio = previous.getHealthLostRatio();
        return previous.previousWaveHealthLostRatio
                >= previous.previousHealthLossThreshold;
    }

    public boolean isCleared() {
        return zombies.stream().noneMatch(zombie -> zombie != null && !zombie.isDead());
    }

    public float getHealthLostRatio() {
        if (initialTotalHealth <= 0) {
            return 1f;
        }
        return Math.min(1f, 1f - totalCurrentHealth() / (float) initialTotalHealth);
    }

    private int totalCurrentHealth() {
        int total = 0;
        for (Zombie zombie : zombies) {
            if (zombie != null) {
                total += Math.max(0, zombie.currentHealth);
                total += zombie.getRemainingArmorHealth();
            }
        }
        return total;
    }
}
