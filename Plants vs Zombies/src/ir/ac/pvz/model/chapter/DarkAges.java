package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.board.GameBoard;
import ir.ac.pvz.model.board.GraveTile;
import ir.ac.pvz.model.board.Position;

import java.util.Random;

public class DarkAges extends Chapter {
    private Random random;

    public DarkAges() {
        super("Dark Ages");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(GameBoard board) {
        spawnGravesPerWave(board);
    }

    public void spawnGravesPerWave(GameBoard board) {
        int x = 1 + random.nextInt(board.getCols());
        int y = 1 + random.nextInt(board.getRows());
        if (board.getTile(x, y).canPlant()) {
            GraveTile grave = new GraveTile(new Position(x, y), 50);
            grave.setNecromancy(random.nextInt(4) == 0);
            board.setTile(x, y, grave);
        }
    }

    public void activateNecromancy(GameBoard board) {
        for (int y = 1; y <= board.getRows(); y++) {
            for (int x = 1; x <= board.getCols(); x++) {
                if (board.getTile(x, y) instanceof GraveTile) {
                    GraveTile grave = (GraveTile) board.getTile(x, y);
                    String zombieType = grave.spawnZombieUnderneath();
                    if (zombieType != null) {
                        System.out.println("A zombie rises from beneath a grave at (" + x + ", " + y + ").");
                    }
                }
            }
        }
    }
}
