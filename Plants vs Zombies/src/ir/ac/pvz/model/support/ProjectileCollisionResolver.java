package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;

public class ProjectileCollisionResolver {

    public void resolveMovement(Projectile projectile, Board board) {
        projectile.move();
        if (projectile.currentPosition.x < 0
                || projectile.currentPosition.x >= board.columns) {
            projectile.die();
        }
    }

    public void resolveCollision(Projectile projectile, Board board) {
        GridPosition position = new GridPosition(
                (int) projectile.currentPosition.x,
                projectile.currentPosition.y);
        Tile tile = board.getTile(position);
        if (tile == null || resolveFrozenObstacle(projectile, tile, board)
                || resolveBlockingObstacle(projectile, tile)) {
            return;
        }
        for (Zombie zombie : tile.getZombies()) {
            if (projectile.canHit(zombie)) {
                applyDamage(projectile, zombie);
                applySpecialEffect(projectile, zombie);
                break;
            }
        }
    }

    public void applyDamage(Projectile projectile, GameObject target) {
        if (projectile.damageMode == DamageMode.INSTANT_KILL
                && target instanceof Zombie) {
            ((Zombie) target).receiveInstantKill(projectile.trajectory);
        } else if (projectile.damageMode == DamageMode.INSTANT_KILL) {
            target.die();
        } else {
            projectile.hit(target);
        }
    }

    public void applySpecialEffect(Projectile projectile, Zombie target) {
        if (projectile.type == ProjectileType.ICE) {
            target.chill(0.5f, 3f);
        }
    }

    public boolean isBlockedByTombstone(Projectile projectile,
                                        Tombstone tombstone) {
        return tombstone != null && tombstone.isAlive
                && projectile.trajectory == ProjectileTrajectory.STRAIGHT
                && !projectile.ignoresObstacles;
    }

    public boolean canLobberIgnoreObstacle(Projectile projectile) {
        return projectile.trajectory == ProjectileTrajectory.ARC
                || projectile.ignoresObstacles;
    }

    public boolean canPoisonIgnoreArmor(Projectile projectile) {
        return projectile.type == ProjectileType.POISON
                || projectile.damageMode == DamageMode.IGNORE_ARMOR;
    }

    private boolean resolveFrozenObstacle(Projectile projectile, Tile tile,
                                          Board board) {
        if (!(tile.obstacle instanceof FrozenBlock)
                || canLobberIgnoreObstacle(projectile)) {
            return false;
        }
        if (projectile.type == ProjectileType.FIRE) {
            tile.obstacle.destroy();
        } else {
            projectile.hitObstacle(tile.obstacle);
        }
        if (!tile.obstacle.isAlive) {
            board.clearDestroyedObstacle(tile);
        }
        projectile.die();
        return true;
    }

    private boolean resolveBlockingObstacle(Projectile projectile,
                                            Tile tile) {
        Tombstone tombstone = tile.obstacle instanceof Tombstone
                ? (Tombstone) tile.obstacle : null;
        if (!isBlockedByTombstone(projectile, tombstone)) {
            return false;
        }
        projectile.hitObstacle(tile.obstacle);
        if (!tile.obstacle.isAlive) {
            tombstone.turnIntoNormalGround(tile);
        }
        return true;
    }
}
