package ir.ac.pvz.model.user;

import java.util.List;
import java.util.Random;

public class GreenHouse {
    private int rows;
    private int cols;
    private Pot[][] pots;
    private Random random;

    public GreenHouse() {
        this.rows = 4;
        this.cols = 5;
        this.pots = new Pot[rows][cols];
        this.random = new Random();

        for (int y = 1; y <= rows; y++) {
            for (int x = 1; x <= cols; x++) {
                pots[y - 1][x - 1] = new Pot(x, y, y != 1);
            }
        }
    }

    public Pot getPot(int x, int y) {
        if (x < 1 || x > cols || y < 1 || y > rows) {
            return null;
        }
        return pots[y - 1][x - 1];
    }

    public boolean unlockNextPot() {
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
        boolean marigold = random.nextBoolean() || unlockedPlants.isEmpty();
        if (marigold) {
            pot.plantSeed("marigold", true);
        } else {
            String plant = unlockedPlants.get(random.nextInt(unlockedPlants.size()));
            pot.plantSeed(plant, false);
        }
    }

    public void showGreenhouse() {
        for (int y = 1; y <= rows; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 1; x <= cols; x++) {
                Pot pot = getPot(x, y);
                line.append("(").append(x).append(",").append(y).append(":").append(describePot(pot)).append(") ");
            }
            System.out.println(line.toString().trim());
        }
    }

    private String describePot(Pot pot) {
        switch (pot.getStatus()) {
            case LOCKED:
                return "locked";
            case EMPTY:
                return "empty";
            case READY:
                return pot.getPlantType() + " ready";
            default:
                return pot.getPlantType() + " " + String.format("%.1f", pot.getRemainingHours()) + "h left";
        }
    }
}
