package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.board.GameBoard;
import ir.ac.pvz.model.board.IceTile;
import ir.ac.pvz.model.board.Position;
import ir.ac.pvz.model.event.ColdWind;

import java.util.Random;

public class FrostbiteCaves extends Chapter {
    private Random random;

    public FrostbiteCaves() {
        super("Frostbite Caves");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(GameBoard board) {
        createIceTiles(board);
    }

    public void applyColdWind(int[] affectedRows) {
        ColdWind coldWind = new ColdWind(affectedRows);
        coldWind.apply();
    }

    public void spawnFrozenZombies() {
        System.out.println("Some zombies start this stage already frozen.");
    }

    public void createIceTiles(GameBoard board) {
        int slidingColumn = 1 + random.nextInt(board.getCols());
        String direction = random.nextBoolean() ? "up" : "down";
        for (int y = 1; y <= board.getRows(); y++) {
            board.setTile(slidingColumn, y, new IceTile(new Position(slidingColumn, y), direction));
        }
    }
}
