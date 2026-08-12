package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.Projectile;

public class Gargantuar extends Zombie {
    private boolean impThrown;
    private int maximumHealth;
    private boolean maximumHealthCaptured;

    public Gargantuar() {
        super("Gargantuar");
        this.isBoss = false;
        this.impThrown = false;
    }

    @Override
    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        super.applyBaseData(movementSpeed, baseHealth,
                eatDamagePerSecond, cost, weight, plantFoodEligible);
        maximumHealth = baseHealth;
        maximumHealthCaptured = false;
    }

    @Override
    public void receiveProjectile(Projectile projectile) {
        captureMaximumHealth();
        super.receiveProjectile(projectile);
    }

    @Override
    public void takeDamage(int amount) {
        captureMaximumHealth();
        super.takeDamage(amount);
    }

    @Override
    public void update(int tickCount) {
        captureMaximumHealth();
        super.update(tickCount);
    }

    @Override
    public void attackPlant(Plant plant) {
        if (plant != null) {
            plant.receiveInstantKill();
        }
    }

    @Override
    public void specialBehavior() {
        captureMaximumHealth();
        if (!impThrown && maximumHealth > 0
                && currentHealth * 2 <= maximumHealth) {
            impThrown = true;
        }
    }

    private void captureMaximumHealth() {
        if (!maximumHealthCaptured && health > 0) {
            maximumHealth = health;
            maximumHealthCaptured = true;
        }
    }

    public boolean hasThrownImp() {
        return impThrown;
    }
}
