package ir.ac.pvz.model.minigame;

public class SunProducerZombie {
    public static final int HEALTH = 1290;

    private static final double BASE_RATE_PER_SECOND = 0.5;
    private static final double RATE_GROWTH_PER_SECOND = 0.05;
    private static final double MAX_RATE_PER_SECOND = 5.0;

    private final int row;
    private int health;
    private double sunRate;
    private double produced;
    private double elapsedSeconds;

    public SunProducerZombie(int row) {
        this.row = row;
        this.health = HEALTH;
        this.sunRate = BASE_RATE_PER_SECOND;
    }

    public int getRow() {
        return row;
    }

    public int getHealth() {
        return health;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public double getSunRate() {
        return sunRate;
    }

    public int produceSun() {
        return produceSun(1d);
    }

    public int produceSun(double seconds) {
        if (!isAlive() || seconds <= 0d) {
            return 0;
        }

        elapsedSeconds += seconds;
        updateRate();
        produced += sunRate * seconds;

        int whole = (int) produced;
        produced -= whole;

        return whole;
    }

    public void updateRate() {
        sunRate = Math.min(MAX_RATE_PER_SECOND,
                BASE_RATE_PER_SECOND + elapsedSeconds * RATE_GROWTH_PER_SECOND);
    }

    public void takeDamage(int amount) {
        health -= amount;

        if (health < 0) {
            health = 0;
        }
    }

    public void kill() {
        health = 0;
    }
}
