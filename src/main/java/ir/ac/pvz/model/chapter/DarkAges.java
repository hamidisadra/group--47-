package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.Tombstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DarkAges extends Chapter {
    private static final int GRAVES_PER_WAVE = 3;
    private static final int SUN_GRAVE_PERCENT = 20;
    private static final int PLANT_FOOD_GRAVE_PERCENT = 10;

    private final Random random;

    public DarkAges() {
        super("Dark Ages");
        this.random = new Random();
    }

    @Override
    public void applyChapterEffects(Board board) {
        spawnGravesPerWave(board, random);
    }

    public List<GridPosition> spawnGravesPerWave(Board board, Random source) {
        List<GridPosition> raised = new ArrayList<>();
        Random generator = source == null ? random : source;

        for (int index = 0; index < GRAVES_PER_WAVE; index++) {
            GridPosition position = new GridPosition(
                    generator.nextInt(board.getColumns()),
                    generator.nextInt(board.getRows()));
            Tile tile = board.getTile(position);
            if (tile == null || !tile.canPlant || !tile.getPlants().isEmpty()
                    || tile.hasObstacle()) {
                continue;
            }

            board.configureTile(position, TileType.TOMBSTONE);
            applyVariant(tile, generator);
            raised.add(position);
        }

        return raised;
    }

    private void applyVariant(Tile tile, Random generator) {
        if (!(tile.obstacle instanceof Tombstone)) {
            return;
        }

        Tombstone grave = (Tombstone) tile.obstacle;
        int roll = generator.nextInt(100);
        if (roll < SUN_GRAVE_PERCENT) {
            grave.setVariant(Tombstone.SUN);
        }

        else if (roll < SUN_GRAVE_PERCENT + PLANT_FOOD_GRAVE_PERCENT) {
            grave.setVariant(Tombstone.PLANT_FOOD);
        }
    }
}
