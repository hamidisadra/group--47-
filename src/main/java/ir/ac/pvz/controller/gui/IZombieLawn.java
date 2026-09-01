package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

final class IZombieLawn {
    private static final String[][] LEVEL_DEFENDERS = {
        { "Peashooter", "Sunflower", "Wall-nut" },
        { "Peashooter", "Repeater", "Wall-nut", "Snow Pea" },
        { "Repeater", "Wall-nut", "Snow Pea", "Threepeater", "Tall-nut" }
    };

    private static final int[] MIN_PER_ROW = { 1, 2, 2 };
    private static final int[] MAX_PER_ROW = { 2, 3, 4 };

    private IZombieLawn() {
    }

    static int clampLevel(int level) {
        return Math.max(1, Math.min(LEVEL_DEFENDERS.length, level));
    }

    static List<String> defenderPool(int level) {
        return new ArrayList<>(
            Arrays.asList(LEVEL_DEFENDERS[clampLevel(level) - 1]));
    }

    static int defendersPerRow(int level, Random random) {
        int index = clampLevel(level) - 1;
        int minimum = MIN_PER_ROW[index];
        int maximum = MAX_PER_ROW[index];

        return minimum + random.nextInt(maximum - minimum + 1);
    }

    static void plantDefenders(Board board, int level, int plantColumns) {
        List<String> pool = defenderPool(level);
        Random random = new Random();
        int planted = 0;

        for (int row = 0; row < board.rows; row++) {
            int count = defendersPerRow(level, random);

            for (int index = 0; index < count; index++) {
                int column = random.nextInt(plantColumns);
                GridPosition position = new GridPosition(column, row);

                if (!board.getTile(position).getPlants().isEmpty()) {
                    continue;
                }

                String type = pool.get(random.nextInt(pool.size()));
                Plant plant = Plant.createSpreadsheetPlant(8000 + planted, type);

                if (plant != null && board.getTile(position).addPlant(plant)) {
                    planted++;
                }
            }
        }
    }
}
