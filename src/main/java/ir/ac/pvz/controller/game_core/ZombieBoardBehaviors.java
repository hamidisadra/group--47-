package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.ZombieDefinition;
import ir.ac.pvz.model.zombies.FishermanZombie;
import ir.ac.pvz.model.zombies.KingZombie;
import ir.ac.pvz.model.zombies.PianistZombie;
import ir.ac.pvz.model.zombies.TombRaiserZombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class ZombieBoardBehaviors {
    private static final double DEFAULT_TOMBS_TO_SPAWN = 2d;
    private static final float KNIGHTING_RANGE = 4f;

    private ZombieBoardBehaviors() {
    }

    static boolean raiseTombstones(TombRaiserZombie zombie, Board board,
                                   Random random, ZombieDefinition definition) {
        List<Tile> candidates = emptyTombstoneTiles(board);

        if (candidates.isEmpty()) {
            return false;
        }

        Collections.shuffle(candidates, random);

        double configured = DEFAULT_TOMBS_TO_SPAWN;

        if (definition != null) {
            configured = definition.getNumber(
                    "NumberOfTombsToSpawn", configured);
        }

        int requested = (int) Math.round(configured);
        int count = Math.min(Math.max(1, requested), candidates.size());

        for (int index = 0; index < count; index++) {
            Tile tile = candidates.get(index);
            zombie.createTombstone(board, tile.position);
            tile.type = TileType.TOMBSTONE;
        }

        return count > 0;
    }

    private static List<Tile> emptyTombstoneTiles(Board board) {
        List<Tile> tiles = new ArrayList<>();

        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));

                if (!tile.isWater && !tile.hasObstacle()
                        && tile.getPlants().isEmpty()
                        && tile.getZombies().isEmpty()) {
                    tiles.add(tile);
                }
            }
        }

        return tiles;
    }

    static boolean hookPlant(FishermanZombie zombie, Board board) {
        Plant target = nearestPlantToZombie(zombie, board);

        if (target == null) {
            return false;
        }

        int targetX = target.location.x + 1;

        if (targetX >= (int) Math.floor(zombie.currentPosition.x)) {
            zombie.throwAdjacentHookedPlant(target);
            return true;
        }

        GridPosition destination = new GridPosition(targetX, target.location.y);
        Tile destinationTile = board.getTile(destination);

        if (destinationTile == null || !destinationTile.getPlants().isEmpty()) {
            return false;
        }

        return board.movePlant(target, destination);
    }

    static Plant nearestPlantToZombie(Zombie zombie, Board board) {
        Plant nearest = null;

        for (Plant plant : board.getPlantsInLane(zombie.lane)) {
            if (!plant.isAlive || plant.isCatTransformed) {
                continue;
            }

            if (nearest == null || plant.location.x > nearest.location.x) {
                nearest = plant;
            }
        }

        return nearest;
    }

    static boolean promoteZombie(KingZombie king, Board board) {
        Zombie target = null;
        double bestDistance = Double.MAX_VALUE;

        for (Zombie zombie : board.getAllAliveZombies()) {
            if (!isPlainZombie(zombie) || zombie == king) {
                continue;
            }

            double horizontal = king.currentPosition.x
                    - zombie.currentPosition.x;
            double vertical = Math.abs(zombie.lane - king.lane);

            if (horizontal >= 0f && horizontal < KNIGHTING_RANGE
                    && vertical <= 1f
                    && horizontal + vertical < bestDistance) {
                target = zombie;
                bestDistance = horizontal + vertical;
            }
        }

        if (target == null) {
            return false;
        }

        return king.promoteNearbyBasicZombie(target);
    }

    private static boolean isPlainZombie(Zombie zombie) {
        return zombie.getType().replace("-", "")
                .replace("_", "").replace(" ", "")
                .equalsIgnoreCase("BasicZombie");
    }

    static boolean moveAdjacentZombies(PianistZombie pianist, Board board,
                                       Random random) {
        boolean moved = false;

        for (Zombie zombie : new ArrayList<>(board.getAllAliveZombies())) {
            if (zombie == pianist || zombie instanceof KingZombie
                    || zombie instanceof FishermanZombie) {
                continue;
            }

            List<Integer> lanes = adjacentLanes(zombie.lane, board.rows);

            if (lanes.isEmpty()) {
                continue;
            }

            int target = lanes.get(random.nextInt(lanes.size()));
            pianist.moveZombieToAdjacentLane(zombie, target);
            board.placeZombie(zombie, new ContinuousPosition(
                    zombie.currentPosition.x, target));
            moved = true;
        }

        return moved;
    }

    private static List<Integer> adjacentLanes(int lane, int rows) {
        List<Integer> lanes = new ArrayList<>();

        if (lane > 0) {
            lanes.add(lane - 1);
        }

        if (lane + 1 < rows) {
            lanes.add(lane + 1);
        }

        return lanes;
    }
}
