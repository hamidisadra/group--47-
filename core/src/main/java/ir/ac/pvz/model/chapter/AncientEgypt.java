package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.event.Tornado;
import ir.ac.pvz.model.others.Wave;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;

import java.util.Random;

public class AncientEgypt extends Chapter {
    private Random random;

    public AncientEgypt() {
        super("Ancient Egypt");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(Board board) {
        createGraves(board);
    }

    public void createGraves(Board board) {
        int graveCount = 1 + random.nextInt(3);
        for (int i = 0; i < graveCount; i++) {
            int x = random.nextInt(board.getColumns());
            int y = random.nextInt(board.getRows());
            GridPosition position = new GridPosition(x, y);
            Tile tile = board.getTile(position);
            if (tile != null && tile.canPlant) {
                board.configureTile(position, TileType.TOMBSTONE);
            }
        }
    }

    public void spawnTornado(Wave wave) {
        if (!wave.isFinalWave) {
            return;
        }
        int lane = random.nextInt(5);
        Tornado tornado = new Tornado(lane);
        tornado.spawn();
    }
}