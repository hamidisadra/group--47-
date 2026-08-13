package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DarkAges extends Chapter {
    private Random random;
    private Set<GridPosition> necromancyGraves;

    public DarkAges() {
        super("Dark Ages");
        this.random = new Random();
        this.necromancyGraves = new HashSet<>();
    }

    @Override
    public void applyChapterEffects(Board board) {
        spawnGravesPerWave(board);
    }

    public void spawnGravesPerWave(Board board) {
        int x = random.nextInt(board.getColumns());
        int y = random.nextInt(board.getRows());
        GridPosition position = new GridPosition(x, y);
        Tile tile = board.getTile(position);
        if (tile != null && tile.canPlant) {
            board.configureTile(position, TileType.TOMBSTONE);
            if (random.nextInt(4) == 0) {
                necromancyGraves.add(position);
            }
        }
    }

    public void activateNecromancy(Board board) {
        for (GridPosition position : necromancyGraves) {
            Tile tile = board.getTile(position);
            if (tile != null && tile.type == TileType.TOMBSTONE) {
                System.out.println("A zombie rises from beneath a grave at "
                        + position.toUserString() + ".");
            }
        }
    }
}