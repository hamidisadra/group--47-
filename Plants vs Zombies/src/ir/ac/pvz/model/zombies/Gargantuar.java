package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.ZombieDataRepository;

public class Gargantuar extends Zombie {
    private boolean impThrown;
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
    }
    @Override
    public void attackPlant(Plant plant) {
        if (plant != null) {
            plant.receiveInstantKill();
        }
    }
    @Override
    public void specialBehavior() {
        if (!impThrown && currentHealth <= health / 2) {
            impThrown = true;
        }
    }
    public boolean hasThrownImp() {
        return impThrown;
    }
}
