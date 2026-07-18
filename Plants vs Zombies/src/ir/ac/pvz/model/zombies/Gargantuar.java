package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.support.ZombieDataRepository;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public class Gargantuar extends Zombie {

    private boolean impThrown;

    public Gargantuar() {
        super(0.185f, 3600, 0, 1500);
        this.isBoss = true;
        this.impThrown = false;
    }

    @Override
    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        float basicSpeed = (float) ZombieDataRepository.getInstance().getNumber(
                "BasicZombie", "Speed", 0.185d);
        super.applyBaseData(Math.min(movementSpeed, basicSpeed), baseHealth,
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
        if (!impThrown && currentHealth <= 1800) {
            impThrown = true;
        }
    }

    public boolean hasThrownImp() {
        return impThrown;
    }
}
