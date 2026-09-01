package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.zombies.ProspectorZombie;
import java.util.List;

final class PlantTargetFinder {
    private PlantTargetFinder() {
    }

    static Plant findPlantTarget(Board board, Zombie zombie) {
        if (zombie instanceof ProspectorZombie
            && ((ProspectorZombie) zombie).reversedByDynamite) {
            return findNearestPlantToRight(board, zombie);
        }

        return findNearestPlantAhead(board, zombie);
    }

    static Plant findNearestPlantToRight(Board board, Zombie zombie) {
        int minimumX = Math.max(0, (int) Math.floor(zombie.currentPosition.x));

        for (int x = minimumX; x < board.columns; x++) {
            Plant plant = topLivingPlant(board, x, zombie.lane);

            if (plant != null) {
                return plant;
            }
        }

        return null;
    }

    static Plant findNearestPlantAhead(Board board, Zombie zombie) {
        if (zombie == null) {
            return null;
        }

        int maximumX = Math.min(board.columns - 1,
            (int) Math.floor(zombie.currentPosition.x));

        for (int x = maximumX; x >= 0; x--) {
            Plant plant = topLivingPlant(board, x, zombie.lane);

            if (plant != null) {
                return plant;
            }
        }

        return null;
    }

    private static Plant topLivingPlant(Board board, int column, int lane) {
        Tile tile = board.getTile(new GridPosition(column, lane));

        if (tile == null) {
            return null;
        }

        List<Plant> plants = tile.getPlants();

        for (int index = plants.size() - 1; index >= 0; index--) {
            Plant plant = plants.get(index);

            if (plant.isAlive && !plant.isCatTransformed) {
                return plant;
            }
        }

        return null;
    }
}
