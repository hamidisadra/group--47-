package ir.ac.pvz.model.support;

public class ZombieBaseStats {

    public float speed;
    public int health;
    public int eatDamagePerSecond;
    public int waveCost;
    public int accessoryHealth;

    public ZombieBaseStats(float speed, int health, int eatDamagePerSecond,
                           int waveCost, int accessoryHealth) {
        this.speed = speed;
        this.health = health;
        this.eatDamagePerSecond = eatDamagePerSecond;
        this.waveCost = waveCost;
        this.accessoryHealth = accessoryHealth;
    }

    public boolean isComplete() {
        return speed >= 0f && health > 0 && eatDamagePerSecond >= 0
                && waveCost >= 0 && accessoryHealth > 0;
    }
}
