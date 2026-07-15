package ir.ac.pvz.model.minigame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Vasebreaker extends MiniGame {
    private List<Vase> vases;

    public Vasebreaker(int stageNumber, int rows, int cols) {
        super("Vasebreaker", stageNumber);
        this.vases = new ArrayList<>();
        Random random = new Random();

        for (int y = 1; y <= rows; y++) {
            for (int x = 1; x <= cols; x++) {
                VaseType type = VaseType.NORMAL;
                int roll = random.nextInt(20);
                if (roll == 0) {
                    type = VaseType.PLANT_VASE;
                } else if (roll == 1) {
                    type = VaseType.GHOUL_VASE;
                }
                vases.add(new Vase(x, y, type));
            }
        }
    }

    public List<Vase> getVases() {
        return vases;
    }

    public Vase getVase(int x, int y) {
        for (Vase vase : vases) {
            if (vase.getX() == x && vase.getY() == y) {
                return vase;
            }
        }
        return null;
    }

    public String breakVase(Vase vase, List<String> unlockedPlants) {
        return vase.breakOpen(unlockedPlants);
    }

    public boolean allVasesBroken() {
        for (Vase vase : vases) {
            if (!vase.isBroken()) {
                return false;
            }
        }
        return true;
    }
}
