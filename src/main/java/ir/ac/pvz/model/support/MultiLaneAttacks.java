package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.ProjectileType;

final class MultiLaneAttacks {
    private final ProjectileResolver resolver;

    MultiLaneAttacks(ProjectileResolver resolver) {
        this.resolver = resolver;
    }

    boolean attackThreeLanes(Plant plant, Board board) {
        boolean attacked = false;

        for (int row = plant.location.y - 1; row <= plant.location.y + 1; row++) {
            Zombie target = board.getNearestZombieAhead(plant.location.x, row);

            if (target != null) {
                resolver.hitZombie(plant, target, plant.attackPower,
                        ProjectileType.PEA, board);
                attacked = true;
            }
        }

        return attacked;
    }

    boolean attackDiagonalLanes(Plant plant, Board board) {
        boolean attacked = false;
        int[] rows = {plant.location.y - 1, plant.location.y + 1};

        for (int row : rows) {
            Zombie ahead = board.getNearestZombieAhead(plant.location.x, row);
            Zombie behind = board.getNearestZombieBehind(plant.location.x, row);

            attacked |= hitIfPresent(plant, ahead, board, 3);
            attacked |= hitIfPresent(plant, behind, board, 3);
        }

        return attacked;
    }

    boolean attackSplitPea(Plant plant, Board board) {
        Zombie ahead = resolver.nearestAhead(plant, board);
        Zombie behind = board.getNearestZombieBehind(
                plant.location.x, plant.location.y);

        boolean attacked = hitIfPresent(plant, ahead, board, 1);

        return hitIfPresent(plant, behind, board, 2) || attacked;
    }

    boolean attackStarfruit(Plant plant, Board board) {
        int column = plant.location.x;
        int row = plant.location.y;

        Zombie forward = board.getNearestZombieAhead(column, row);
        Zombie upperForward = board.getNearestZombieAhead(column, row - 1);
        Zombie lowerForward = board.getNearestZombieAhead(column, row + 1);
        Zombie upperBack = board.getNearestZombieBehind(column, row - 1);
        Zombie lowerBack = board.getNearestZombieBehind(column, row + 1);

        boolean attacked = false;

        attacked |= hitIfPresent(plant, forward, board, 1);
        attacked |= hitIfPresent(plant, upperForward, board, 1);
        attacked |= hitIfPresent(plant, lowerForward, board, 1);
        attacked |= hitIfPresent(plant, upperBack, board, 1);
        attacked |= hitIfPresent(plant, lowerBack, board, 1);

        return attacked;
    }

    boolean hitIfPresent(Plant plant, Zombie target, Board board, int shots) {
        if (target == null) {
            return false;
        }

        for (int shot = 0; shot < shots && !target.isDead(); shot++) {
            resolver.hitZombie(plant, target, plant.attackPower,
                    resolver.projectileTypeFor(plant), board);
        }

        return true;
    }
}
