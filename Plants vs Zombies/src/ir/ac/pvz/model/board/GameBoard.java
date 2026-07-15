package ir.ac.pvz.model.board;

import java.util.ArrayList;
import java.util.List;

public class GameBoard {
    private int rows;
    private int cols;
    private Tile[][] tiles;
    private List<LawnMower> lawnMowers;

    public GameBoard() {
        this.rows = 4;
        this.cols = 5;
        this.tiles = new Tile[rows][cols];
        this.lawnMowers = new ArrayList<>();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                tiles[y][x] = new NormalTile(new Position(x + 1, y + 1));
            }
        }

        for (int row = 1; row <= rows; row++) {
            lawnMowers.add(new LawnMower(row));
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public boolean isInside(int x, int y) {
        return x >= 1 && x <= cols && y >= 1 && y <= rows;
    }

    public Tile getTile(int x, int y) {
        if (!isInside(x, y)) {
            return null;
        }
        return tiles[y - 1][x - 1];
    }

    public void setTile(int x, int y, Tile tile) {
        if (isInside(x, y)) {
            tiles[y - 1][x - 1] = tile;
        }
    }

    public List<LawnMower> getLawnMowers() {
        return lawnMowers;
    }

    public LawnMower getLawnMower(int row) {
        for (LawnMower mower : lawnMowers) {
            if (mower.getRow() == row) {
                return mower;
            }
        }
        return null;
    }

    public boolean placePlant(String plantType, int x, int y) {
        Tile tile = getTile(x, y);
        if (tile == null || !tile.canPlant()) {
            return false;
        }
        tile.setPlant(plantType);
        return true;
    }

    public void removePlant(int x, int y) {
        Tile tile = getTile(x, y);
        if (tile != null) {
            tile.removePlant();
        }
    }

    public List<String> getZombiesInLane(int row) {
        List<String> zombies = new ArrayList<>();
        for (int x = 1; x <= cols; x++) {
            Tile tile = getTile(x, row);
            if (tile != null) {
                zombies.addAll(tile.getZombies());
            }
        }
        return zombies;
    }

    public void showMap() {
        for (int y = 1; y <= rows; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 1; x <= cols; x++) {
                Tile tile = getTile(x, y);
                String cell = tile.getPlant() == null ? "." : tile.getPlant().charAt(0) + "";
                line.append("[").append(cell).append("]");
            }
            System.out.println(line);
        }
    }
}
