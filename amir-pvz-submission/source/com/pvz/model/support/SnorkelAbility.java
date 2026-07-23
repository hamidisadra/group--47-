package com.pvz.model.support;

import com.pvz.game.GameSession;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.ZombieEffectType;

public final class SnorkelAbility extends ZombieAbility {
    private boolean underwater;
    public SnorkelAbility() {
        super("snorkel", "Moves underwater and surfaces to eat.", 0f);
        underwater = false;
    }
    @Override
    public void onTick(Zombie zombie, GameSession session, float elapsed) {
        if (session == null) {
            return;
        }
        Tile tile = session.getBoard().getTile(new GridPosition(
                (int) Math.floor(zombie.currentPosition.x), zombie.lane));
        if (tile != null && tile.isWater && !underwater) {
            underwater = true;
            zombie.effects.add(new ZombieEffect(ZombieEffectType.UNDERWATER,
                    Float.POSITIVE_INFINITY));
        }
    }
    @Override
    public boolean blocksProjectile(Zombie zombie, Projectile projectile) {
        return underwater && projectile != null
                && projectile.trajectory != ProjectileTrajectory.ARC;
    }
    @Override
    public boolean blocksDamage(Zombie zombie, int amount) {
        return underwater && (zombie.incomingProjectile == null
                || zombie.incomingProjectile.trajectory
                != ProjectileTrajectory.ARC);
    }
    @Override
    public boolean onPlantContact(Zombie zombie, Plant plant,
                                  GameSession session) {
        if (underwater) {
            underwater = false;
            zombie.effects.removeIf(effect ->
                    effect.type == ZombieEffectType.UNDERWATER);
        }
        return false;
    }
}
