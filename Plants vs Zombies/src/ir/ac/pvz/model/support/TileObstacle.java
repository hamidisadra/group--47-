package ir.ac.pvz.model.support;


import ir.ac.pvz.model.core.GameObject;

public abstract class TileObstacle extends GameObject {

    public boolean blocksStraightProjectiles;
    public boolean canPlantOn;

    protected TileObstacle(int health, boolean blocksStraightProjectiles, boolean canPlantOn) {
        super(0f, 0, health);
        this.blocksStraightProjectiles = blocksStraightProjectiles;
        this.canPlantOn = canPlantOn;
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        if (!isAlive) {
            destroy();
        }
    }

    public void destroy() {
        die();
    }
}
