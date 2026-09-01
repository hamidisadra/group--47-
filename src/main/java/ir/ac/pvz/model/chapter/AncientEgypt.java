package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;

import java.util.Random;

public class AncientEgypt extends Chapter {
    private static final int MIN_GRAVES = 3;
    private static final int EXTRA_GRAVES = 4;

    private final Random random;

    public AncientEgypt() {
        super("Ancient Egypt");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(Board board) {
        createGraves(board);
    }

    public void createGraves(Board board) {
        int graveCount = MIN_GRAVES + random.nextInt(EXTRA_GRAVES);

        for (int index = 0; index < graveCount; index++) {
            GridPosition position = new GridPosition(
                    board.getColumns() - 1
                            - random.nextInt(Math.max(1, board.getColumns() / 2)),
                    random.nextInt(board.getRows()));
            Tile tile = board.getTile(position);
            if (tile != null && tile.canPlant) {
                board.configureTile(position, TileType.TOMBSTONE);
            }
        }
    }
}
