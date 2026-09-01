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
        int level = Math.max(1, Math.min(3, stageNumber));
        int usedColumns = Math.min(cols, 2 + level);
        int ghoulChance = 4 + level * 2;

        for (int y = 1; y <= rows; y++) {
            for (int x = cols - usedColumns + 1; x <= cols; x++) {
                vases.add(new Vase(x, y, rollType(random, ghoulChance)));
            }
        }

        guaranteeSpecialVases(random, level);
    }

    private void guaranteeSpecialVases(Random random, int level) {
        ensureAtLeast(random, VaseType.GHOUL_VASE, level);
        ensureAtLeast(random, VaseType.PLANT_VASE, 1);
    }

    private void ensureAtLeast(Random random, VaseType type, int required) {
        int present = 0;

        for (Vase vase : vases) {
            if (vase.getType() == type) {
                present++;
            }
        }

        for (int index = present; index < required; index++) {
            replaceRandomNormalVase(random, type);
        }
    }

    private void replaceRandomNormalVase(Random random, VaseType type) {
        List<Integer> candidates = new ArrayList<>();

        for (int index = 0; index < vases.size(); index++) {
            if (vases.get(index).getType() == VaseType.NORMAL) {
                candidates.add(index);
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        int target = candidates.get(random.nextInt(candidates.size()));
        Vase old = vases.get(target);
        vases.set(target, new Vase(old.getX(), old.getY(), type));
    }

    private static VaseType rollType(Random random, int ghoulChance) {
        int roll = random.nextInt(100);
        if (roll < ghoulChance) {
            return VaseType.GHOUL_VASE;
        }

        if (roll < ghoulChance + 12) {
            return VaseType.PLANT_VASE;
        }

        return VaseType.NORMAL;
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
