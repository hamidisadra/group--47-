package ir.ac.pvz.model.user;

import java.util.List;
import java.util.Random;

public class GreenHouse {
    private int rows;
    private int cols;
    private Pot[][] pots;
    private transient Random random;

    public GreenHouse() {
        this(new Random());
    }

    public GreenHouse(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random generator cannot be null.");
        }
        rows = 4;
        cols = 5;
        pots = new Pot[rows][cols];
        this.random = random;
        for (int y = 1; y <= rows; y++) {
            for (int x = 1; x <= cols; x++) {
                pots[y - 1][x - 1] = new Pot(x, y, y != 1);
            }
        }
    }

    public Pot getPot(int x, int y) {
        ensureState();
        if (x < 1 || x > cols || y < 1 || y > rows) {
            return null;
        }
        return pots[y - 1][x - 1];
    }

    public boolean unlockNextPot() {
        ensureState();
        for (int y = 1; y <= rows; y++) {
            for (int x = 1; x <= cols; x++) {
                Pot pot = getPot(x, y);
                if (pot.isLocked()) {
                    pot.unlock();
                    return true;
                }
            }
        }
        return false;
    }

    public void plantRandom(Pot pot, List<String> unlockedPlants) {
        ensureState();
        if (pot == null) {
            throw new IllegalArgumentException("Pot cannot be null.");
        }
        if (pot.isLocked()) {
            throw new IllegalStateException("This pot is locked.");
        }
        if (!pot.isEmpty()) {
            throw new IllegalStateException("This pot is already occupied.");
        }
        List<String> plants = unlockedPlants == null ? List.of() : unlockedPlants;
        boolean marigold = plants.isEmpty() || random.nextBoolean();
        if (marigold) {
            pot.plantSeed("marigold", true);
            return;
        }
        String plant = plants.get(random.nextInt(plants.size()));
        pot.plantSeed(plant, false);
    }

    public void showGreenhouse() {
        ensureState();
        for (int y = 1; y <= rows; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 1; x <= cols; x++) {
                Pot pot = getPot(x, y);
                line.append("(").append(x).append(",").append(y)
                        .append(":").append(describePot(pot)).append(") ");
            }
            System.out.println(line.toString().trim());
        }
    }

    private String describePot(Pot pot) {
        return switch (pot.getStatus()) {
            case LOCKED -> "locked";
            case EMPTY -> "empty";
            case READY -> pot.getPlantType() + " ready";
            case GROWING -> pot.getPlantType() + " "
                    + String.format("%.1f", pot.getRemainingHours()) + "h left";
        };
    }

    private void ensureState() {
        if (rows <= 0) {
            rows = 4;
        }
        if (cols <= 0) {
            cols = 5;
        }
        if (pots == null || pots.length != rows || pots.length == 0
                || pots[0] == null || pots[0].length != cols) {
            Pot[][] restored = new Pot[rows][cols];
            for (int y = 1; y <= rows; y++) {
                for (int x = 1; x <= cols; x++) {
                    restored[y - 1][x - 1] = new Pot(x, y, y != 1);
                }
            }
            pots = restored;
        }
        if (random == null) {
            random = new Random();
        }
    }
}
