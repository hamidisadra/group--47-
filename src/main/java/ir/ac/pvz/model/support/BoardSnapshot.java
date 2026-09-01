package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.others.GameSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BoardSnapshot {
    private static final String ENTITY_SEPARATOR = ";";
    private static final String FIELD_SEPARATOR = ":";

    private BoardSnapshot() {
    }

    public static String encodeZombies(Board board) {
        StringBuilder builder = new StringBuilder();

        for (Zombie zombie : board.getAllAliveZombies()) {
            if (builder.length() > 0) {
                builder.append(ENTITY_SEPARATOR);
            }

            builder.append(zombie.getType()).append(FIELD_SEPARATOR)
                .append(zombie.lane).append(FIELD_SEPARATOR)
                .append(round(zombie.currentPosition.x)).append(FIELD_SEPARATOR)
                .append(zombie.currentHealth).append(FIELD_SEPARATOR)
                .append(zombie.getRemainingArmorHealth());
        }

        return builder.toString();
    }

    public static String encodePlants(Board board) {
        StringBuilder builder = new StringBuilder();

        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                appendTilePlants(builder, board, column, row);
            }
        }

        return builder.toString();
    }

    private static void appendTilePlants(StringBuilder builder, Board board,
                                         int column, int row) {
        Tile tile = board.getTile(new GridPosition(column, row));

        if (tile == null) {
            return;
        }

        for (Plant plant : tile.getPlants()) {
            if (!plant.isAlive) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(ENTITY_SEPARATOR);
            }

            builder.append(plant.type).append(FIELD_SEPARATOR)
                .append(column).append(FIELD_SEPARATOR)
                .append(row).append(FIELD_SEPARATOR)
                .append(plant.currentHp);
        }
    }

    public static void applyZombies(Board board, GameSession session,
                                    String encoded) {
        List<Zombie> pool = new ArrayList<>(board.getAllAliveZombies());

        for (String[] parts : split(encoded, 5)) {
            reconcileZombie(board, session, pool, parts);
        }

        for (Zombie leftover : pool) {
            board.removeZombieEverywhere(leftover);
        }
    }

    private static void reconcileZombie(Board board, GameSession session,
                                        List<Zombie> pool, String[] parts) {
        String type = parts[0];
        int lane = parseInt(parts[1]);
        float x = parseFloat(parts[2]);
        int health = parseInt(parts[3]);

        Zombie existing = takeClosest(pool, type, lane, x);

        if (existing != null) {
            board.placeZombie(existing, new ContinuousPosition(x, lane));
            existing.currentHealth = health;
            existing.health = health;
            return;
        }

        Zombie spawned = session.spawnConfiguredZombie(type,
            new ContinuousPosition(x, lane));

        if (spawned != null) {
            spawned.currentHealth = health;
            spawned.health = health;
        }
    }

    private static Zombie takeClosest(List<Zombie> pool, String type, int lane,
                                      float x) {
        Zombie best = null;
        float bestDistance = Float.MAX_VALUE;

        for (Zombie candidate : pool) {
            if (!candidate.getType().equals(type) || candidate.lane != lane) {
                continue;
            }

            float distance = Math.abs(candidate.currentPosition.x - x);

            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }

        if (best != null) {
            pool.remove(best);
        }

        return best;
    }

    public static void applyPlants(Board board, String encoded) {
        Map<String, Plant> existing = indexPlants(board);
        List<String[]> rows = split(encoded, 4);
        Set<String> wanted = keysOf(rows);

        removeMissingPlants(board, existing, wanted);

        Map<String, Integer> seen = new LinkedHashMap<>();
        int identifier = 20000;

        for (String[] parts : rows) {
            String key = nextKey(seen, parts[0], parts[1], parts[2]);
            Plant plant = existing.get(key);

            if (plant != null) {
                plant.currentHp = parseInt(parts[3]);
                plant.health = plant.currentHp;
                continue;
            }

            addPlant(board, parts, identifier++);
        }
    }

    private static Set<String> keysOf(List<String[]> rows) {
        Map<String, Integer> seen = new LinkedHashMap<>();
        Set<String> keys = new LinkedHashSet<>();

        for (String[] parts : rows) {
            keys.add(nextKey(seen, parts[0], parts[1], parts[2]));
        }

        return keys;
    }

    private static void addPlant(Board board, String[] parts, int identifier) {
        Plant plant = Plant.createSpreadsheetPlant(identifier, parts[0]);

        if (plant == null) {
            return;
        }

        plant.currentHp = parseInt(parts[3]);
        plant.health = plant.currentHp;

        Tile tile = board.getTile(new GridPosition(parseInt(parts[1]),
            parseInt(parts[2])));

        if (tile != null) {
            tile.addPlant(plant);
        }
    }

    private static String nextKey(Map<String, Integer> seen, String type,
                                 String column, String row) {
        String base = type + "@" + column + "," + row;
        int occurrence = seen.merge(base, 1, Integer::sum);

        return base + "#" + occurrence;
    }

    private static Map<String, Plant> indexPlants(Board board) {
        Map<String, Plant> index = new LinkedHashMap<>();
        Map<String, Integer> seen = new LinkedHashMap<>();

        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));

                if (tile == null) {
                    continue;
                }

                for (Plant plant : tile.getPlants()) {
                    index.put(nextKey(seen, plant.type,
                        Integer.toString(column), Integer.toString(row)), plant);
                }
            }
        }

        return index;
    }

    private static void removeMissingPlants(Board board,
                                            Map<String, Plant> existing,
                                            Set<String> wanted) {
        for (Map.Entry<String, Plant> entry : existing.entrySet()) {
            if (wanted.contains(entry.getKey())) {
                continue;
            }

            Tile tile = board.getTile(entry.getValue().location);

            if (tile != null) {
                tile.getPlants().remove(entry.getValue());
            }
        }
    }

    private static List<String[]> split(String encoded, int expectedFields) {
        List<String[]> rows = new ArrayList<>();

        if (encoded == null || encoded.isBlank()) {
            return rows;
        }

        for (String entity : encoded.split(ENTITY_SEPARATOR)) {
            String[] parts = entity.split(FIELD_SEPARATOR);

            if (parts.length >= expectedFields) {
                rows.add(parts);
            }
        }

        return rows;
    }

    private static float round(float value) {
        return Math.round(value * 100f) / 100f;
    }

    private static int parseInt(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        }

        catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static float parseFloat(String raw) {
        try {
            return Float.parseFloat(raw.trim());
        }

        catch (NumberFormatException exception) {
            return 0f;
        }
    }
}
