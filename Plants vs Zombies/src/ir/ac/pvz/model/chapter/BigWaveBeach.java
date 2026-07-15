package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.board.GameBoard;
import ir.ac.pvz.model.board.Tile;
import ir.ac.pvz.model.board.WaterTile;

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
    public void applyChapterEffects(GameBoard board) {
        updateWaterLevel(board);
    }

    public void updateWaterLevel(GameBoard board) {
        if (currentWaterLevel >= maxWaterCol) {
            return;
        }
        currentWaterLevel++;
        for (int y = 1; y <= board.getRows(); y++) {
            int x = board.getCols() - currentWaterLevel + 1;
            if (!(board.getTile(x, y) instanceof WaterTile)) {
                board.setTile(x, y, new WaterTile(board.getTile(x, y).getPosition()));
            }
        }
        floodPlants(board);
    }

    public void floodPlants(GameBoard board) {
        for (int y = 1; y <= board.getRows(); y++) {
            for (int x = board.getCols() - currentWaterLevel + 1; x <= board.getCols(); x++) {
                Tile tile = board.getTile(x, y);
                if (tile instanceof WaterTile && !((WaterTile) tile).hasLilyPad() && tile.getPlant() != null) {
                    System.out.println("Plant " + tile.getPlant() + " at (" + x + ", " + y + ") is destroyed.");
                    tile.removePlant();
                }
            }
        }
    }
}
