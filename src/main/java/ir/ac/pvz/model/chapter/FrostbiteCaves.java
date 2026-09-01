package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;

import java.util.Random;

public class FrostbiteCaves extends Chapter {
    private final Random random;

    public FrostbiteCaves() {
        super("Frostbite Caves");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(Board board) {
        createIceTiles(board);
    }

    public void createIceTiles(Board board) {
        int slidingColumn = 1 + random.nextInt(Math.max(1, board.getColumns() - 2));
        TileType direction = random.nextBoolean()
                ? TileType.SLIPPERY_UP : TileType.SLIPPERY_DOWN;

        for (int y = 0; y < board.getRows(); y++) {
            if (random.nextInt(3) == 0) {
                continue;
            }

            board.configureTile(new GridPosition(slidingColumn, y), direction);
        }
    }
}
