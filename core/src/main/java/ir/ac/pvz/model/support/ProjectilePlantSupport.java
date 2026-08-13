package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.ProjectileType;
public final class ProjectilePlantSupport {
    private ProjectilePlantSupport() {
    }
    public static void bounceBowlingBulb(ProjectileResolver resolver,
                                         Plant plant, Zombie firstTarget,
                                         int damage, Board board) {
        int[] lanes = {firstTarget.lane - 1, firstTarget.lane + 1};
        for (int lane : lanes) {
            Zombie target = board.getNearestZombieAhead(plant.location.x, lane);
            if (target != null) {
                resolver.hitZombie(plant, target, damage,
                        ProjectileType.PIERCING, board);
            }
        }
    }
    public static boolean isWithinAttackRange(Plant plant, Zombie zombie) {
        float range = Float.POSITIVE_INFINITY;
        String type = plant.getNormalizedType();
        if (type.equals("seashroom")) {
            range = BalanceDefaults.SHORT_PROJECTILE_RANGE;
            if (plant.level >= 2) {
                range++;
            }
        }
        else if (type.equals("puffshroom")) {
            range = BalanceDefaults.SHORT_PROJECTILE_RANGE;
            if (plant.level >= 4) {
                range++;
            }
        }
        else if (type.equals("fumeshroom")) {
            range = BalanceDefaults.MEDIUM_PROJECTILE_RANGE;
            if (plant.level >= 2) {
                range++;
            }
        }
        else if (type.equals("magnetshroom")) {
            range = BalanceDefaults.SHORT_PROJECTILE_RANGE;
            if (plant.level >= 2) {
                range++;
            }
        }
        return zombie.currentPosition.x - plant.location.x <= range + 0.0001f;
    }
    public static void meltFrozenBlockAt(Zombie zombie, Board board) {
        GridPosition position = new GridPosition(
                (int) Math.floor(zombie.currentPosition.x), zombie.lane);
        Tile tile = board.getTile(position);
        if (tile != null && tile.obstacle instanceof FrozenBlock) {
            tile.obstacle.destroy();
            tile.obstacle = null;
            tile.type = ir.ac.pvz.model.enums.TileType.FROSTBITE_GROUND;
            tile.canPlant = true;
        }
    }
}
