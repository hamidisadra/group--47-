package ir.ac.pvz.model.support;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ZombieEffectType;

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
        boolean onWater = tile != null && tile.isWater;
        if (onWater && !underwater) {
            submerge(zombie);
        } else if (!onWater && underwater) {
            surface(zombie);
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
            surface(zombie);
        }
        return false;
    }

    private void submerge(Zombie zombie) {
        underwater = true;
        zombie.effects.removeIf(effect ->
                effect.type == ZombieEffectType.UNDERWATER);
        zombie.effects.add(new ZombieEffect(ZombieEffectType.UNDERWATER,
                Float.POSITIVE_INFINITY));
    }

    private void surface(Zombie zombie) {
        underwater = false;
        zombie.effects.removeIf(effect ->
                effect.type == ZombieEffectType.UNDERWATER);
    }
}
