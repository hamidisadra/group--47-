package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;

public class BigWaveBeach extends Chapter {
    private int currentWaterLevel;
    private int maxWaterCol;

    public BigWaveBeach() {
        super("Big Wave Beach");
        this.currentWaterLevel = 0;
        this.maxWaterCol = 3;
    }

    public int getCurrentWaterLevel() {
        return currentWaterLevel;
    }

    @Override
    public void applyChapterEffects(Board board) {
        updateWaterLevel(board);
    }

    public void updateWaterLevel(Board board) {
        if (currentWaterLevel >= maxWaterCol) {
            return;
        }
        currentWaterLevel++;
        for (int y = 0; y < board.getRows(); y++) {
            int x = board.getColumns() - currentWaterLevel;
            GridPosition position = new GridPosition(x, y);
            Tile tile = board.getTile(position);
            if (tile != null && tile.type != TileType.WATER) {
                board.configureTile(position, TileType.WATER);
            }
        }
        floodPlants(board);
    }

    public void floodPlants(Board board) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = board.getColumns() - currentWaterLevel; x < board.getColumns(); x++) {
                Tile tile = board.getTile(new GridPosition(x, y));
                if (tile == null || tile.type != TileType.WATER) {
                    continue;
                }
                Plant plant = tile.getPlant();
                if (plant != null && !tile.hasLilyPad() && !plant.canPlantOn(tile)) {
                    System.out.println("Plant " + plant.type + " at "
                            + tile.getPosition().toUserString() + " is destroyed.");
                    tile.getPlants().remove(plant);
                }
            }
        }
    }
}