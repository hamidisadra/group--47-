package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.ZombieEffectType;
import com.pvz.model.support.Projectile;
import com.pvz.model.support.ZombieEffect;

public class SnorkelZombie extends Zombie {

    public boolean isUnderwater;
    public boolean canBeHitOnlyByLobberWhenUnderwater;

    private boolean receivingLobberDamage;

    public SnorkelZombie() {
        super(0.185f, 350, 100, 200);
        this.isUnderwater = false;
        this.canBeHitOnlyByLobberWhenUnderwater = true;
        this.receivingLobberDamage = false;
    }

    public void enterWater() {
        if (isUnderwater) {
            return;
        }
        isUnderwater = true;
        effects.add(new ZombieEffect(ZombieEffectType.UNDERWATER,
                Float.POSITIVE_INFINITY));
    }

    @Override
    public void receiveProjectile(Projectile projectile) {
        if (isUnderwater && projectile != null
                && projectile.trajectory != ProjectileTrajectory.ARC) {
            return;
        }
        receivingLobberDamage = projectile != null
                && projectile.trajectory == ProjectileTrajectory.ARC;
        try {
            super.receiveProjectile(projectile);
        } finally {
            receivingLobberDamage = false;
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (isUnderwater && !receivingLobberDamage) {
            return;
        }
        super.takeDamage(amount);
    }

    @Override
    public void receiveInstantKill(ProjectileTrajectory trajectory) {
        if (!isUnderwater || trajectory == ProjectileTrajectory.ARC) {
            super.receiveInstantKill(trajectory);
        }
    }

    public void surfaceToEatPlant() {
        isUnderwater = false;
        effects.removeIf(effect -> effect.type == ZombieEffectType.UNDERWATER);
    }
}
