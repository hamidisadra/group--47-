package ir.ac.pvz.model.event;

public class IceBlock {
    private int health;

    public IceBlock(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }

    public void melt() {
        health = 0;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }
}
