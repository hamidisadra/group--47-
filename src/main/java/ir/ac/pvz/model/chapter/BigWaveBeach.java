package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;

import java.util.Random;

public class BigWaveBeach extends Chapter {
    private static final int MIN_WATER_COLUMNS = 2;

    private int currentWaterLevel;
    private int maxWaterCol;

    public BigWaveBeach() {
        super("Big Wave Beach");
        this.currentWaterLevel = MIN_WATER_COLUMNS;
        this.maxWaterCol = 4;
    }

    public int getCurrentWaterLevel() {
        return currentWaterLevel;
    }

    public int getMaxWaterColumns() {
        return maxWaterCol;
    }

    @Override
    public void applyChapterEffects(Board board) {
        applyWaterLevel(board);
    }

    public void shiftWaterLevel(Board board, Random random) {
        int target = currentWaterLevel;
        if (random == null || random.nextBoolean()) {
            target++;
        }

        else {
            target--;
        }

        currentWaterLevel = Math.max(MIN_WATER_COLUMNS,
                Math.min(maxWaterCol, target));
        applyWaterLevel(board);
    }

    private void applyWaterLevel(Board board) {
        int firstWaterColumn = board.getColumns() - currentWaterLevel;

        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                updateColumn(board, new GridPosition(x, y), x >= firstWaterColumn);
            }
        }

        floodPlants(board);
    }

    private void updateColumn(Board board, GridPosition position, boolean flooded) {
        Tile tile = board.getTile(position);
        if (tile == null || tile.type == TileType.LOW_TIDE) {
            return;
        }

        if (flooded && tile.type != TileType.WATER) {
            board.configureTile(position, TileType.WATER);
        }

        else if (!flooded && tile.type == TileType.WATER) {
            board.configureTile(position, TileType.BEACH_GROUND);
        }
    }

    public void floodPlants(Board board) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = board.getColumns() - currentWaterLevel;
                    x < board.getColumns(); x++) {
                removeDrownedPlant(board, new GridPosition(x, y));
            }
        }
    }

    private void removeDrownedPlant(Board board, GridPosition position) {
        Tile tile = board.getTile(position);
        if (tile == null || tile.type != TileType.WATER) {
            return;
        }

        Plant plant = tile.getPlant();
        if (plant != null && !tile.hasLilyPad() && !plant.canPlantOn(tile)) {
            System.out.println("Plant " + plant.type + " at "
                    + tile.getPosition().toUserString() + " is destroyed.");
            tile.getPlants().remove(plant);
        }
    }
}
