package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileType;
import com.pvz.model.support.Dynamite;
import com.pvz.model.support.Projectile;

public class ProspectorZombie extends Zombie {

    public Dynamite dynamite;
    public boolean reversedByDynamite;

    public ProspectorZombie() {
        super(0.16f, 190, 100, 200);
        this.dynamite = new Dynamite();
        this.reversedByDynamite = false;
    }

    @Override
    public void update(int tickCount) {
        super.update(tickCount);
        if (!dynamite.isExtinguished && dynamite.remainingSeconds > 0f) {
            dynamite.remainingSeconds -= tickCount / 10f;
            if (dynamite.remainingSeconds <= 0f) {
                dynamite.explodeAndReverseDirection(this);
                reversedByDynamite = true;
            }
        }
    }

    @Override
    public void move(float deltaX) {
        if (!reversedByDynamite) {
            super.move(deltaX);
            return;
        }
        if (!isAlive) {
            return;
        }
        currentPosition.x += deltaX * speed;
        positionX = currentPosition.x;
    }

    @Override
    public void receiveProjectile(Projectile projectile) {
        if (projectile != null && projectile.type == ProjectileType.ICE) {
            dynamite.extinguishByIce();
        }
        super.receiveProjectile(projectile);
    }
}
