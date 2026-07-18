package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.event.ColdWind;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;

import java.util.Random;

public class FrostbiteCaves extends Chapter {
    private Random random;

    public FrostbiteCaves() {
        super("Frostbite Caves");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(Board board) {
        createIceTiles(board);
    }

    public void applyColdWind(int[] affectedRows) {
        ColdWind coldWind = new ColdWind(affectedRows);
        coldWind.apply();
    }

    public void spawnFrozenZombies() {
        System.out.println("Some zombies start this stage already frozen.");
    }

    public void createIceTiles(Board board) {
        int slidingColumn = random.nextInt(board.getColumns());
        TileType direction = random.nextBoolean()
                ? TileType.SLIPPERY_UP : TileType.SLIPPERY_DOWN;
        for (int y = 0; y < board.getRows(); y++) {
            board.configureTile(new GridPosition(slidingColumn, y), direction);
        }
    }
}