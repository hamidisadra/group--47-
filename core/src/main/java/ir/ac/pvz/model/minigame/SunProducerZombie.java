package ir.ac.pvz.model.minigame;

public class SunProducerZombie {
    private int row;
    private int health;
    private double sunRate;
    private int elapsedSeconds;

    public SunProducerZombie(int row) {
        this.row = row;
        this.health = 200;
        this.sunRate = 1.0;
    }

    public int getRow() {
        return row;
    }

    public int getHealth() {
        return health;
    }

    public double getSunRate() {
        return sunRate;
    }

    public int produceSun() {
        elapsedSeconds++;
        updateRate();
        return (int) Math.round(sunRate);
    }

    public void updateRate() {
        sunRate = Math.min(10, 1.0 + elapsedSeconds * 0.05);
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }
}
