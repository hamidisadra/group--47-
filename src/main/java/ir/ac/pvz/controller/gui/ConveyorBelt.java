package ir.ac.pvz.controller.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class ConveyorBelt {
    static final float INTERVAL_SECONDS = 12f;
    static final int CAPACITY = 8;

    private static final Random RANDOM = new Random();

    private ConveyorBelt() {
    }

    static float refill(List<String> queue, List<String> selectedPlants,
                        String minigame, MinigameController minigameController,
                        float timer) {
        if (minigameController != null && minigameController.isVasebreaker()) {
            queue.clear();
            queue.addAll(minigameController.getHarvestedPlants());
            return timer;
        }

        List<String> pool = buildPool(selectedPlants, minigame);

        if (queue.isEmpty()) {
            queue.add(pick(pool));
            return 0f;
        }

        float remaining = timer;

        while (remaining >= INTERVAL_SECONDS && queue.size() < CAPACITY) {
            remaining -= INTERVAL_SECONDS;
            queue.add(pick(pool));
        }

        if (queue.size() >= CAPACITY) {
            return 0f;
        }

        return remaining;
    }

    private static List<String> buildPool(List<String> selectedPlants,
                                          String minigame) {
        List<String> pool = new ArrayList<>(selectedPlants);

        if ("Wall-nut Bowling".equals(minigame)) {
            pool.clear();
            pool.add("Wall-nut");
            pool.add("Explode-o-nut");
            pool.add("Tall-nut");
        }

        if (pool.isEmpty()) {
            pool.add("Peashooter");
            pool.add("Sunflower");
        }

        return pool;
    }

    private static String pick(List<String> pool) {
        return pool.get(RANDOM.nextInt(pool.size()));
    }
}
