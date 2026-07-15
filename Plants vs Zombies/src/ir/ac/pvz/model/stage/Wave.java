package ir.ac.pvz.model.stage;

import java.util.ArrayList;
import java.util.List;

public class Wave {
    private int number;
    private int waveCost;
    private boolean isFinalWave;
    private List<String> zombieTypes;

    public Wave(int number, int waveCost, boolean isFinalWave) {
        this.number = number;
        this.waveCost = waveCost;
        this.isFinalWave = isFinalWave;
        this.zombieTypes = new ArrayList<>();
    }

    public int getNumber() {
        return number;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public boolean isFinalWave() {
        return isFinalWave;
    }

    public List<String> getZombieTypes() {
        return zombieTypes;
    }

    public void startWave() {
        if (isFinalWave) {
            System.out.println("The final wave has come.");
        } else {
            System.out.println("Wave " + number + " started.");
        }
    }

    public void spawnZombie(String type, int lane) {
        zombieTypes.add(type);
        System.out.println("Zombie " + type + " spawned at wave " + number + " in lane " + lane + " which costed " + waveCost + ".");
    }

    public boolean canStartNextWave() {
        return true;
    }
}
