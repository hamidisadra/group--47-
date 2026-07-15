package ir.ac.pvz.model.minigame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Beghouled extends MiniGame {
    private static final String[] PLANT_TYPES = {"peashooter", "wall-nut", "puff-shroom", "cabbage-pult", "melon-pult"};

    private int rows;
    private int cols;
    private String[][] grid;
    private boolean[][] craters;
    private int matchCount;
    private int targetMatches;
    private int sunAmount;
    private List<PlantUpgrade> upgrades;
    private Random random;

    public Beghouled(int stageNumber, int targetMatches) {
        super("Beghouled", stageNumber);
        this.rows = 5;
        this.cols = 5;
        this.grid = new String[rows][cols];
        this.craters = new boolean[rows][cols];
        this.targetMatches = targetMatches;
        this.random = new Random();
        this.upgrades = new ArrayList<>();

        upgrades.add(new PlantUpgrade("peashooter", "repeater", 500));
        upgrades.add(new PlantUpgrade("repeater", "mega gatling-pea", 1500));
        upgrades.add(new PlantUpgrade("wall-nut", "tall-nut", 500));
        upgrades.add(new PlantUpgrade("puff-shroom", "fume-shroom", 250));
        upgrades.add(new PlantUpgrade("cabbage-pult", "melon-pult", 1000));
        upgrades.add(new PlantUpgrade("melon-pult", "winter melon", 750));

        resetGrid();
    }

    public String[][] getGrid() {
        return grid;
    }

    public int getSunAmount() {
        return sunAmount;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void resetGrid() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = randomPlant();
                craters[y][x] = false;
            }
        }
    }

    public void markCrater(int x, int y) {
        craters[y - 1][x - 1] = true;
        grid[y - 1][x - 1] = null;
    }

    public boolean swapPlants(int x1, int y1, int x2, int y2) {
        if (!isAdjacent(x1, y1, x2, y2)) {
            return false;
        }
        if (craters[y1 - 1][x1 - 1] || craters[y2 - 1][x2 - 1]) {
            return false;
        }

        swap(x1, y1, x2, y2);

        if (checkMatches() == 0) {
            swap(x1, y1, x2, y2);
            return false;
        }

        applyGravity();
        fillEmpty();
        return true;
    }

    private boolean isAdjacent(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    private void swap(int x1, int y1, int x2, int y2) {
        String temp = grid[y1 - 1][x1 - 1];
        grid[y1 - 1][x1 - 1] = grid[y2 - 1][x2 - 1];
        grid[y2 - 1][x2 - 1] = temp;
    }

    public int checkMatches() {
        boolean[][] matched = new boolean[rows][cols];
        int matches = 0;

        for (int y = 0; y < rows; y++) {
            int runLength = 1;
            for (int x = 1; x <= cols; x++) {
                boolean sameAsPrevious = x < cols && grid[y][x] != null && grid[y][x].equals(grid[y][x - 1]);
                if (sameAsPrevious) {
                    runLength++;
                } else {
                    if (runLength >= 3) {
                        for (int k = x - runLength; k < x; k++) {
                            matched[y][k] = true;
                        }
                        matches++;
                        sunAmount += 50 * (runLength - 2);
                    }
                    runLength = 1;
                }
            }
        }

        for (int x = 0; x < cols; x++) {
            int runLength = 1;
            for (int y = 1; y <= rows; y++) {
                boolean sameAsPrevious = y < rows && grid[y][x] != null && grid[y][x].equals(grid[y - 1][x]);
                if (sameAsPrevious) {
                    runLength++;
                } else {
                    if (runLength >= 3) {
                        for (int k = y - runLength; k < y; k++) {
                            matched[k][x] = true;
                        }
                        matches++;
                        sunAmount += 50 * (runLength - 2);
                    }
                    runLength = 1;
                }
            }
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (matched[y][x]) {
                    grid[y][x] = null;
                }
            }
        }

        matchCount += matches;
        return matches;
    }

    public void applyGravity() {
        for (int x = 0; x < cols; x++) {
            int writeRow = rows - 1;
            for (int y = rows - 1; y >= 0; y--) {
                if (grid[y][x] != null) {
                    grid[writeRow][x] = grid[y][x];
                    if (writeRow != y) {
                        grid[y][x] = null;
                    }
                    writeRow--;
                }
            }
        }
    }

    public void fillEmpty() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (grid[y][x] == null && !craters[y][x]) {
                    grid[y][x] = randomPlant();
                }
            }
        }
    }

    public boolean upgradePlant(String plantType) {
        for (PlantUpgrade upgrade : upgrades) {
            if (upgrade.canUpgrade(plantType, sunAmount)) {
                sunAmount -= upgrade.getSunCost();
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        if (plantType.equals(grid[y][x])) {
                            grid[y][x] = upgrade.getToType();
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean checkWinCondition() {
        return matchCount >= targetMatches;
    }

    private String randomPlant() {
        return PLANT_TYPES[random.nextInt(PLANT_TYPES.length)];
    }
}
