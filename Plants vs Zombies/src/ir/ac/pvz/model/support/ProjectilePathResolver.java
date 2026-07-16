package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.ProjectileType;
import com.pvz.model.zombies.ArcadeZombie;
import com.pvz.model.zombies.BarrelRollerZombie;
import com.pvz.model.zombies.ImpZombie;
import com.pvz.model.zombies.JesterZombie;

import java.util.List;

public class ProjectilePathResolver {

    public boolean deliver(Projectile projectile, Zombie target, Board board) {
        if (projectile == null || target == null || board == null) {
            return false;
        }
        if (isBlockedBeforeTarget(projectile, target, board)) {
            return false;
        }
        ProjectileType originalType = projectile.type;
        target.receiveProjectile(projectile);
        if (target instanceof JesterZombie && projectile.isReflected) {
            deliverReflected(projectile, target, originalType, board);
        }
        return true;
    }

    private boolean isBlockedBeforeTarget(Projectile projectile, Zombie target,
                                          Board board) {
        if (projectile.trajectory == ProjectileTrajectory.ARC
                || projectile.ignoresObstacles || projectile.sourcePlant == null) {
            return false;
        }
        int start = projectile.sourcePlant.location.x + 1;
        int end = (int) Math.floor(target.currentPosition.x);
        for (int x = start; x <= end; x++) {
            Tile tile = board.getTile(new GridPosition(x, target.lane));
            if (tile != null && blockProjectile(projectile, tile, board)) {
                return true;
            }
        }
        return false;
    }

    private boolean blockProjectile(Projectile projectile, Tile tile,
                                    Board board) {
        if (hitPushedObject(projectile, tile, board)
                || hitOctopusBlock(projectile, tile)) {
            return true;
        }
        if (tile.obstacle instanceof FrozenBlock && tile.obstacle.isAlive) {
            hitFrozenBlock(projectile, tile, board);
            return true;
        }
        if (tile.obstacle instanceof Tombstone && tile.obstacle.isAlive
                && projectile.trajectory == ProjectileTrajectory.STRAIGHT) {
            projectile.hitObstacle(tile.obstacle);
            if (!tile.obstacle.isAlive) {
                board.clearDestroyedObstacle(tile);
            }
            return true;
        }
        return false;
    }

    private boolean hitOctopusBlock(Projectile projectile, Tile tile) {
        for (Plant plant : tile.getPlants()) {
            if (plant.isOctopusBlocked && plant.blockingOctopus != null) {
                plant.blockingOctopus.takeDamage(projectile.damageAmount);
                projectile.die();
                return true;
            }
        }
        return false;
    }

    private boolean hitPushedObject(Projectile projectile, Tile tile,
                                    Board board) {
        for (Zombie zombie : tile.getZombies()) {
            if (zombie instanceof ArcadeZombie
                    && hitArcadeMachine(projectile, (ArcadeZombie) zombie)) {
                return true;
            }
            if (zombie instanceof BarrelRollerZombie
                    && hitBarrel(projectile,
                    ((BarrelRollerZombie) zombie).barrel, board)) {
                return true;
            }
        }
        for (Barrel barrel : board.getLooseBarrels()) {
            int x = (int) Math.floor(barrel.position.x);
            if (x == tile.position.x && barrel.position.y == tile.position.y
                    && hitBarrel(projectile, barrel, board)) {
                return true;
            }
        }
        return false;
    }

    private boolean hitArcadeMachine(Projectile projectile,
                                     ArcadeZombie zombie) {
        ArcadeMachine machine = zombie.arcadeMachine;
        if (machine == null || machine.health <= 0) {
            return false;
        }
        machine.health = Math.max(0, machine.health - projectile.damageAmount);
        projectile.die();
        return true;
    }

    private boolean hitBarrel(Projectile projectile, Barrel barrel,
                              Board board) {
        if (barrel == null || barrel.health <= 0) {
            return false;
        }
        barrel.health = Math.max(0, barrel.health - projectile.damageAmount);
        projectile.die();
        if (barrel.health == 0) {
            spawnBarrelImps(barrel, board);
        }
        return true;
    }

    private void spawnBarrelImps(Barrel barrel, Board board) {
        List<ImpZombie> imps = barrel.breakAndSpawnImps();
        for (ImpZombie imp : imps) {
            board.placeZombie(imp, imp.currentPosition);
        }
        board.removeLooseBarrel(barrel);
    }

    private void hitFrozenBlock(Projectile projectile, Tile tile,
                                Board board) {
        if (projectile.type == ProjectileType.FIRE) {
            tile.obstacle.destroy();
        } else {
            projectile.hitObstacle(tile.obstacle);
        }
        if (!tile.obstacle.isAlive) {
            board.clearDestroyedObstacle(tile);
        }
    }

    private void deliverReflected(Projectile projectile, Zombie jester,
                                  ProjectileType originalType, Board board) {
        int start = (int) Math.floor(jester.currentPosition.x) - 1;
        for (int x = start; x >= 0; x--) {
            Tile tile = board.getTile(new GridPosition(x, jester.lane));
            if (tile == null) {
                continue;
            }
            if (blockProjectile(projectile, tile, board)) {
                return;
            }
            Plant plant = firstVulnerablePlant(tile);
            if (plant != null) {
                hitReflectedPlant(projectile, originalType, plant, board);
                return;
            }
        }
    }

    private Plant firstVulnerablePlant(Tile tile) {
        for (int index = tile.getPlants().size() - 1; index >= 0; index--) {
            Plant plant = tile.getPlants().get(index);
            if (plant.isAlive && !plant.isCatTransformed) {
                return plant;
            }
        }
        return null;
    }

    private void hitReflectedPlant(Projectile projectile,
                                   ProjectileType originalType,
                                   Plant plant, Board board) {
        plant.takeDamage(projectile.damageAmount);
        projectile.die();
        if (originalType == ProjectileType.ICE && plant.isAlive) {
            applyHunterIceHit(plant, board);
        }
    }

    private void applyHunterIceHit(Plant plant, Board board) {
        plant.receiveHunterIceHit(3);
        if (!plant.isPermanentlyFrozen()) {
            return;
        }
        Tile tile = board.getTile(plant.location);
        if (tile == null || tile.obstacle instanceof FrozenBlock) {
            return;
        }
        tile.obstacle = new FrozenBlock(plant,
                BalanceDefaults.HUNTER_ICE_HEALTH);
        tile.type = com.pvz.model.enums.TileType.FROZEN_TILE;
        tile.canPlant = false;
    }
}
