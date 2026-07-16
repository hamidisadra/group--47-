package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ZombieDataRepository;

public class ImpZombie extends Zombie {

    public ImpZombie() {
        super(0.22f, 190, 100, 100);
        applyFastEating(speed, attackDamage);
    }

    @Override
    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        super.applyBaseData(movementSpeed, baseHealth, eatDamagePerSecond,
                cost, weight, plantFoodEligible);
        applyFastEating(movementSpeed, eatDamagePerSecond);
    }

    private void applyFastEating(float movementSpeed, int baseEatDps) {
        double basicSpeed = ZombieDataRepository.getInstance().getNumber(
                "BasicZombie", "Speed", 0.185d);
        if (basicSpeed <= 0d) {
            return;
        }
        attackDamage = Math.max(baseEatDps,
                Math.round((float) (baseEatDps * movementSpeed / basicSpeed)));
        damageToPlant = Math.max(1, Math.round(attackDamage / 10f));
    }
}
