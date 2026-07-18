package ir.ac.pvz.model.stage;

import java.util.ArrayList;
import java.util.List;
import ir.ac.pvz.model.others.Wave;

public abstract class Stage {
    protected int number;
    protected int difficulty;
    protected int waveCount;
    protected List<Wave> waves;
    protected int currentWaveIndex;
    protected boolean started;

    public Stage(int number, int difficulty, int waveCount) {
        this.number = number;
        this.difficulty = difficulty;
        this.waveCount = waveCount;
        this.waves = new ArrayList<>();
        this.currentWaveIndex = 0;
        this.started = false;

        for (int i = 1; i <= waveCount; i++) {
            int cost = 100 + i * 50 * difficulty;
            waves.add(new Wave(i, cost, 0, i == waveCount, new ArrayList<>()));
        }
    }

    public int getNumber() {
        return number;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getWaveCount() {
        return waveCount;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public boolean isStarted() {
        return started;
    }

    public Wave getCurrentWave() {
        if (currentWaveIndex >= waves.size()) {
            return null;
        }
        return waves.get(currentWaveIndex);
    }

    public void startZombieWaves() {
        if (started) {
            System.out.println("Zombie waves have already started.");
            return;
        }
        started = true;
        announceWave(getCurrentWave());
    }

    private void announceWave(Wave wave) {
        if (wave == null) {
            return;
        }
        if (wave.isFinalWave) {
            System.out.println("The final wave has come.");
        } else {
            System.out.println("Wave " + wave.waveNumber + " started.");
        }
    }

    public boolean finishStage() {
        return currentWaveIndex >= waves.size();
    }

    public abstract void startStage();
}