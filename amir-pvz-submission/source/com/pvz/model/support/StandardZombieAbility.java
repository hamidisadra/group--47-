package com.pvz.model.support;

import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.ProjectileType;

public final class StandardZombieAbility extends ZombieAbility {
    public enum Kind {
        FIRE_IMMUNITY,
        FREEZE_IMMUNITY,
        LOBBER_SHIELD
    }
    private final Kind kind;
    public StandardZombieAbility(Kind kind) {
        super(kind.name().toLowerCase(), kind.name(), 0f);
        this.kind = kind;
    }
    @Override
    public boolean blocksProjectile(Zombie zombie, Projectile projectile) {
        if (projectile == null) {
            return false;
        }
        if (kind == Kind.FIRE_IMMUNITY) {
            return projectile.type == ProjectileType.FIRE;
        }
        if (kind == Kind.LOBBER_SHIELD) {
            return projectile.type == ProjectileType.LOBBED
                    || projectile.trajectory == ProjectileTrajectory.ARC;
        }
        return false;
    }
    @Override
    public boolean blocksFreeze(Zombie zombie) {
        return kind == Kind.FREEZE_IMMUNITY;
    }
}
