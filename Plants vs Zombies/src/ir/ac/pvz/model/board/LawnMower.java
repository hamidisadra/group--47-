package ir.ac.pvz.model.board;

import java.util.ArrayList;
import java.util.List;

public class LawnMower {
    private int row;
    private boolean used;
    private List<String> destroyedZombies;

    public LawnMower(int row) {
        this.row = row;
        this.used = false;
        this.destroyedZombies = new ArrayList<>();
    }

    public int getRow() {
        return row;
    }

    public boolean isUsed() {
        return used;
    }

    public List<String> getDestroyedZombies() {
        return destroyedZombies;
    }

    public void trigger(List<String> zombiesInRow) {
        if (used) {
            return;
        }
        used = true;
        destroyedZombies.addAll(zombiesInRow);
        System.out.println("The lawn mower in the row " + row + " is triggered and killed these zombies:");
        for (String zombie : zombiesInRow) {
            System.out.println(zombie);
        }
    }
}
