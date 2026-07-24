package ir.ac.pvz.model.minigame;

import ir.ac.pvz.model.zombies.BeghouledZombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Beghouled extends MiniGame {
    private static final String[] PLANT_TYPES = {"peashooter", "wall-nut", "puff-shroom", "cabbage-pult", "melon-pult"};

    private static final int SPAWN_INTERVAL = 30;
    private static final int MOVE_INTERVAL = 10;

    private int rows;
    private int cols;
    private String[][] grid;
    private boolean[][] craters;
    private int matchCount;
    private int targetMatches;
    private int sunAmount;
    private List<PlantUpgrade> upgrades;
    private Random random;

    private List<BeghouledZombie> zombies;
    private int ticksSinceSpawn;
    private boolean lost;

    public Beghouled(int stageNumber, int targetMatches) {
        super("Beghouled", stageNumber);
        this.rows = 5;
        this.cols = 5;
        this.grid = new String[rows][cols];
        this.craters = new boolean[rows][cols];
        this.targetMatches = targetMatches;
        this.random = new Random();
        this.upgrades = new ArrayList<>();
        this.zombies = new ArrayList<>();

        upgrades.add(new PlantUpgrade("peashooter", "repeater", 500));
        upgrades.add(new PlantUpgrade("repeater", "mega gatling-pea", 1500));
        upgrades.add(new PlantUpgrade("wall-nut", "tall-nut", 500));
        upgrades.add(new PlantUpgrade("puff-shroom", "fume-shroom", 250));
        upgrades.add(new PlantUpgrade("cabbage-pult", "melon-pult", 1000));
        upgrades.add(new PlantUpgrade("melon-pult", "winter melon", 750));

        prepareStartingGrid();
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

    public int getTargetMatches() {
        return targetMatches;
    }

    public List<BeghouledZombie> getZombies() {
        return zombies;
    }

    public boolean isLost() {
        return lost;
    }

    public boolean isCrater(int x, int y) {
        return craters[y - 1][x - 1];
    }

    private void prepareStartingGrid() {
        do {
            resetGrid();
        } while (hasRun() || !hasPossibleMove());
    }

    public void resetGrid() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (!craters[y][x]) {
                    grid[y][x] = randomPlant();
                }
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

        if (!hasRun()) {
            swap(x1, y1, x2, y2);
            return false;
        }

        resolveBoard();
        return true;
    }

    private void resolveBoard() {
        boolean cascade = false;

        while (true) {
            boolean[][] matched = collectRuns(cascade);
            if (matched == null) {
                break;
            }
            clearMatched(matched);
            applyGravity();
            fillEmpty();
            cascade = true;
        }

        reshuffleIfStuck();
    }

    private boolean[][] collectRuns(boolean cascade) {
        boolean[][] matched = new boolean[rows][cols];
        boolean found = false;

        for (int y = 0; y < rows; y++) {
            int runLength = 1;

            for (int x = 1; x <= cols; x++) {
                if (x < cols && sameCell(grid[y][x], grid[y][x - 1])) {
                    runLength++;
                }

                else {
                    if (runLength >= 3) {

                        for (int k = x - runLength; k < x; k++) {
                            matched[y][k] = true;
                        }
                        found = true;
                        awardSun(runLength, cascade);
                    }
                    runLength = 1;
                }
            }
        }

        for (int x = 0; x < cols; x++) {
            int runLength = 1;

            for (int y = 1; y <= rows; y++) {
                if (y < rows && sameCell(grid[y][x], grid[y - 1][x])) {
                    runLength++;
                }

                else {
                    if (runLength >= 3) {

                        for (int k = y - runLength; k < y; k++) {
                            matched[k][x] = true;
                        }
                        found = true;
                        awardSun(runLength, cascade);
                    }
                    runLength = 1;
                }
            }
        }

        return found ? matched : null;
    }

    private void awardSun(int runLength, boolean cascade) {
        int units = runLength - 2;

        if (cascade) {
            units++;
        }

        sunAmount += units * 50;
        matchCount++;
    }

    private void clearMatched(boolean[][] matched) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (matched[y][x]) {
                    grid[y][x] = null;
                }
            }
        }
    }

    public int checkMatches() {
        boolean[][] matched = countRunsOnly();
        if (matched == null) {
            return 0;
        }

        int total = 0;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (matched[y][x]) {
                    total++;
                }
            }
        }
        return total;
    }

    private boolean[][] countRunsOnly() {
        boolean[][] matched = new boolean[rows][cols];
        boolean found = false;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x + 2 < cols; x++) {
                if (sameThree(grid[y][x], grid[y][x + 1], grid[y][x + 2])) {
                    matched[y][x] = true;
                    matched[y][x + 1] = true;
                    matched[y][x + 2] = true;
                    found = true;
                }
            }
        }

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y + 2 < rows; y++) {
                if (sameThree(grid[y][x], grid[y + 1][x], grid[y + 2][x])) {
                    matched[y][x] = true;
                    matched[y + 1][x] = true;
                    matched[y + 2][x] = true;
                    found = true;
                }
            }
        }
        return found ? matched : null;
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

    public boolean hasPossibleMove() {
        for (int y = 1; y <= rows; y++) {
            for (int x = 1; x <= cols; x++) {
                if (swapWouldMatch(x, y, x + 1, y) || swapWouldMatch(x, y, x, y + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean swapWouldMatch(int x1, int y1, int x2, int y2) {
        if (x2 > cols || y2 > rows) {
            return false;
        }
        if (craters[y1 - 1][x1 - 1] || craters[y2 - 1][x2 - 1]) {
            return false;
        }

        swap(x1, y1, x2, y2);
        boolean matchFound = hasRun();
        swap(x1, y1, x2, y2);
        return matchFound;
    }

    private boolean hasRun() {
        return countRunsOnly() != null;
    }

    private boolean sameThree(String first, String second, String third) {
        return sameCell(first, second) && sameCell(first, third);
    }

    private boolean sameCell(String first, String second) {
        return first != null && first.equals(second);
    }

    public boolean reshuffleIfStuck() {
        if (hasPossibleMove()) {
            return false;
        }

        System.out.println("No possible moves left; the garden is reshuffled.");
        prepareStartingGrid();
        return true;
    }

    public void applyGravity() {
        for (int x = 0; x < cols; x++) {
            int writeRow = rows - 1;

            for (int y = rows - 1; y >= 0; y--) {
                if (craters[y][x]) {
                    continue;
                }

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

    public void advanceTime(int ticks) {
        for (int i = 0; i < ticks && !lost; i++) {
            ticksSinceSpawn++;

            if (ticksSinceSpawn >= SPAWN_INTERVAL) {
                spawnZombie();
                ticksSinceSpawn = 0;
            }
            moveZombies();
        }
    }

    private void spawnZombie() {
        int lane = random.nextInt(rows) + 1;
        zombies.add(new BeghouledZombie(lane, cols + 1));
        System.out.println("A zombie enters the garden in row " + lane + ".");
    }

    private void moveZombies() {
        for (BeghouledZombie zombie : new ArrayList<>(zombies)) {
            zombie.addTick();
            if (zombie.getTicksWaited() < MOVE_INTERVAL) {
                continue;
            }
            zombie.resetTicks();
            zombie.moveLeft();

            if (zombie.getColumn() < 1) {
                loseGame();
                return;
            }
            eatPlantUnder(zombie);
        }
    }

    private void eatPlantUnder(BeghouledZombie zombie) {
        int x = zombie.getColumn();
        int y = zombie.getLane();

        if (x < 1 || x > cols || craters[y - 1][x - 1]) {
            return;
        }
        markCrater(x, y);
        System.out.println("A zombie ate the plant at (" + x + ", " + y
                + ") and left a crater.");
    }

    private void loseGame() {
        lost = true;
        zombies.clear();
        System.out.println("The zombies reached your house; you lost this mini game.");
        finishGame(false);
    }

    public boolean checkWinCondition() {
        if (matchCount < targetMatches) {
            return false;
        }
        zombies.clear();
        return true;
    }

    private String randomPlant() {
        return PLANT_TYPES[random.nextInt(PLANT_TYPES.length)];
    }
}