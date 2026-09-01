package ir.ac.pvz.model.minigame;

import java.util.ArrayList;
import java.util.List;

public class WallnutBowling extends MiniGame {
    private int redLineCol;
    private List<BowlingNut> nuts;

    public WallnutBowling(int stageNumber, int redLineCol) {
        super("Wallnut Bowling", stageNumber);
        this.redLineCol = redLineCol;
        this.nuts = new ArrayList<>();
    }

    public int getRedLineCol() {
        return redLineCol;
    }

    public List<BowlingNut> getNuts() {
        return nuts;
    }

    public BowlingNut launchNut(int row, BowlingNutType type) {
        return launchNut(row, redLineCol, type);
    }

    public BowlingNut launchNut(int row, int col, BowlingNutType type) {
        BowlingNut nut = new BowlingNut(row, Math.min(col, redLineCol), type);
        nuts.add(nut);
        return nut;
    }

    public void removeFinishedNuts(int columns) {
        nuts.removeIf(nut -> !nut.isAlive() || nut.isOffBoard(columns));
    }

    public void resolveCollision(BowlingNut nut, String zombieType) {
        nut.onHitZombie(zombieType);
    }
}
