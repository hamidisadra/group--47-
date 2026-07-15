package ir.ac.pvz.model.minigame;

import java.util.List;
import java.util.Random;

public class Vase {
    private int x;
    private int y;
    private VaseType type;
    private String content;
    private boolean broken;

    public Vase(int x, int y, VaseType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public VaseType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public boolean isBroken() {
        return broken;
    }

    public String breakOpen(List<String> unlockedPlants) {
        if (broken) {
            return content;
        }
        broken = true;
        Random random = new Random();

        switch (type) {
            case PLANT_VASE:
                content = unlockedPlants.isEmpty() ? "seedpacket:marigold" : "seedpacket:" + unlockedPlants.get(random.nextInt(unlockedPlants.size()));
                break;
            case GHOUL_VASE:
                content = "zombie:gargantuar";
                break;
            default:
                int roll = random.nextInt(3);
                if (roll == 0) {
                    content = "zombie:basic";
                } else if (roll == 1 && !unlockedPlants.isEmpty()) {
                    content = "seedpacket:" + unlockedPlants.get(random.nextInt(unlockedPlants.size()));
                } else {
                    content = "empty";
                }
                break;
        }
        return content;
    }
}
