package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.ProjectileType;
import com.pvz.model.support.Projectile;

public class ParasolZombie extends Zombie {

    public ParasolZombie() {
        super(0.25f, 350, 100, 200);
    }

    @Override
    public void receiveProjectile(Projectile projectile) {
        if (projectile != null && (projectile.trajectory == ProjectileTrajectory.ARC
                || projectile.type == ProjectileType.LOBBED)) {
            return;
        }
        super.receiveProjectile(projectile);
    }
}
