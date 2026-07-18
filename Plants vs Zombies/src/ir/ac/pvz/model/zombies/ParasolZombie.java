package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.support.Projectile;
import ir.ac.pvz.model.core.Zombie;

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
