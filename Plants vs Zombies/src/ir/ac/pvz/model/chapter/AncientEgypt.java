package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.board.GameBoard;
import ir.ac.pvz.model.board.GraveTile;
import ir.ac.pvz.model.board.Position;
import ir.ac.pvz.model.event.Tornado;
import ir.ac.pvz.model.stage.Wave;

import java.util.Random;

public class AncientEgypt extends Chapter {
    private Random random;

    public AncientEgypt() {
        super("Ancient Egypt");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(GameBoard board) {
        createGraves(board);
    }

    public void createGraves(GameBoard board) {
        int graveCount = 1 + random.nextInt(3);
        for (int i = 0; i < graveCount; i++) {
            int x = 1 + random.nextInt(board.getCols());
            int y = 1 + random.nextInt(board.getRows());
            if (board.getTile(x, y).canPlant()) {
                GraveTile grave = new GraveTile(new Position(x, y), 50);
                grave.setHasSun(random.nextBoolean());
                board.setTile(x, y, grave);
            }
        }
    }

    public void spawnTornado(Wave wave) {
        if (!wave.isFinalWave()) {
            return;
        }
        int lane = 1 + random.nextInt(4);
        Tornado tornado = new Tornado(lane);
        tornado.spawn();
    }
}
