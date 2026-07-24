package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.interfaces.IWall;

public final class EatingAttackStrategy implements ZombieAttackStrategy {
    @Override
    public void attack(Zombie zombie, Plant plant) {
        if (zombie == null || plant == null || !plant.isAlive) {
            return;
        }
        plant.takeDamage(zombie.damageToPlant);
        if (plant instanceof IWall) {
            ((IWall) plant).block(zombie);
        }
    }
}
