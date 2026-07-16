package com.pvz.model.zombies;

import com.pvz.model.enums.ProjectileType;
import com.pvz.model.support.Projectile;
import com.pvz.model.support.ZombieDataRepository;

public class ImpDragon extends ImpZombie {

    public boolean immuneToFire;
    public boolean flyOverPlants;
    public boolean diesToAnyOtherProjectile;

    public ImpDragon() {
        super();
        this.waveCost = 150;
        this.initialWaveCost = 150;
        this.immuneToFire = true;
        this.flyOverPlants = false;
        this.diesToAnyOtherProjectile = false;
    }

    @Override
    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        float impSpeed = (float) ZombieDataRepository.getInstance().getNumber(
                "ImpZombie", "Speed", 0.22d);
        super.applyBaseData(impSpeed, baseHealth, eatDamagePerSecond,
                cost, weight, plantFoodEligible);
    }

    @Override
    public void receiveProjectile(Projectile projectile) {
        if (projectile != null && projectile.type == ProjectileType.FIRE) {
            return;
        }
        super.receiveProjectile(projectile);
    }
}
