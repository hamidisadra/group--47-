package com.pvz.game;

import com.pvz.model.enums.SeasonType;
import com.pvz.model.enums.TileType;
import com.pvz.model.support.GridPosition;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class StageConfigFileLoader {

    private StageConfigFileLoader() {
    }
    public static GameBootstrapConfig load(Path path) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        SeasonType season = parseSeason(required(properties, "season"));
        int rows = optionalPositive(properties, "rows", 5);
        int columns = optionalPositive(properties, "columns", 9);
        int startingSun = requiredNonNegative(properties, "startingSun");
        int totalWaves = requiredPositive(properties, "totalWaves");
        int baseWaveCost = requiredPositive(properties, "baseWaveCost");
        float growth = optionalNonNegativeFloat(properties,
                "waveGrowthRate", 0.25f);
        float finalMultiplier = optionalPositiveFloat(properties,
                "finalWaveMultiplier", 2f);
        List<String> zombies = requiredList(properties, "allowedZombies");
        List<String> plants = requiredList(properties, "selectedPlants");
        StageConfig stage = new StageConfig(season, totalWaves,
                baseWaveCost, growth, finalMultiplier, zombies);
        stage.setSelectedPlantTypes(plants.toArray(new String[0]));
        applyOptionalLists(stage, properties);
        applyWaveCosts(stage, properties);
        applyZombieSettings(stage, properties);
        applySpecialSpawnEvents(stage, properties, rows, columns);
        List<TileConfiguration> tiles = parseTiles(properties, rows, columns);
        applyFrozenContents(tiles, properties, rows, columns);
        return new GameBootstrapConfig(rows, columns, startingSun, stage, tiles);
    }
    private static void applyOptionalLists(StageConfig stage,
                                           Properties properties) {
        List<String> boosted = commaSeparated(properties.getProperty(
                "boostedPlants"));
        if (!boosted.isEmpty()) {
            stage.setBoostedPlantTypes(boosted.toArray(new String[0]));
        }
        String imitater = properties.getProperty("imitaterTarget");
        if (imitater != null && !imitater.trim().isEmpty()) {
            stage.setImitaterTargetType(imitater.trim());
        }
    }
    private static void applyWaveCosts(StageConfig stage,
                                       Properties properties) {
        List<String> values = commaSeparated(properties.getProperty(
                "waveCosts"));
        if (values.isEmpty()) {
            return;
        }
        int[] costs = new int[values.size()];
        for (int index = 0; index < values.size(); index++) {
            costs[index] = parsePositive(values.get(index),
                    "waveCosts[" + index + "]");
        }
        stage.setExplicitWaveCosts(costs);
    }
    private static void applyZombieSettings(StageConfig stage,
                                            Properties properties) {
        for (String name : properties.stringPropertyNames()) {
            if (name.startsWith("abilityCooldown.")) {
                String zombieType = name.substring("abilityCooldown.".length());
                stage.setZombieAbilityCooldown(zombieType,
                        parsePositiveFloat(properties.getProperty(name), name));
            }
            else if (name.startsWith("abilityValue.")) {
                String key = name.substring("abilityValue.".length());
                stage.setZombieAbilityValue(key,
                        parsePositive(properties.getProperty(name), name));
            }
            else if (name.startsWith("zombieStats.")) {
                applyZombieStats(stage, name, properties.getProperty(name));
            }
            else if (name.startsWith("plantLevel.")) {
                String plantType = name.substring("plantLevel.".length());
                int level = parsePositive(properties.getProperty(name), name);
                if (level > 4) {
                    throw new StageConfigurationException(name
                            + " cannot be greater than 4.");
                }
                stage.setPlantLevel(plantType, level);
            }
        }
    }
    private static void applySpecialSpawnEvents(StageConfig stage,
                                                Properties properties,
                                                int rows, int columns) {
        for (String name : properties.stringPropertyNames()) {
            if (!name.startsWith("specialSpawn.")) {
                continue;
            }
            String[] parts = name.substring("specialSpawn.".length())
                    .split("\\.");
            if (parts.length != 3) {
                throw new StageConfigurationException(name
                        + " must use specialSpawn.<tick>.<x>.<y>=<ZombieType>.");
            }
            int tick = parseNonNegative(parts[0], name + ".tick");
            int x = parseNonNegative(parts[1], name + ".x");
            int y = parseNonNegative(parts[2], name + ".y");
            if (x >= columns || y >= rows) {
                throw new StageConfigurationException(name
                        + " is outside the configured board.");
            }
            stage.addSpecialSpawnEvent(tick, required(properties, name),
                    new GridPosition(x, y));
        }
    }
    private static void applyZombieStats(StageConfig stage, String property,
                                         String value) {
        List<String> parts = commaSeparated(value);
        if (parts.size() != 5) {
            throw new StageConfigurationException(property
                    + " must contain speed, health, eatDps, waveCost, accessoryHealth.");
        }
        String zombieType = property.substring("zombieStats.".length());
        stage.setZombieBaseStats(zombieType,
                parsePositiveFloat(parts.get(0), property + ".speed"),
                parsePositive(parts.get(1), property + ".health"),
                parseNonNegative(parts.get(2), property + ".eatDps"),
                parseNonNegative(parts.get(3), property + ".waveCost"),
                parsePositive(parts.get(4), property + ".accessoryHealth"));
    }
    private static List<TileConfiguration> parseTiles(Properties properties,
                                                      int rows, int columns) {
        List<TileConfiguration> tiles = new ArrayList<>();
        for (String name : properties.stringPropertyNames()) {
            if (!name.startsWith("tile.")) {
                continue;
            }
            String[] coordinates = name.substring("tile.".length()).split("\\.");
            if (coordinates.length != 2) {
                throw new StageConfigurationException(
                        name + " must use tile.<x>.<y>=<TileType>.");
            }
            int x = parseNonNegative(coordinates[0], name + ".x");
            int y = parseNonNegative(coordinates[1], name + ".y");
            if (x >= columns || y >= rows) {
                throw new StageConfigurationException(
                        name + " is outside the configured board.");
            }
            TileType type;
            try {
                type = TileType.valueOf(required(properties, name));
            }
            catch (IllegalArgumentException exception) {
                throw new StageConfigurationException(
                        "Unknown tile type in " + name + ".");
            }
            tiles.add(new TileConfiguration(new GridPosition(x, y), type));
        }
        return tiles;
    }
    private static void applyFrozenContents(List<TileConfiguration> tiles,
                                            Properties properties,
                                            int rows, int columns) {
        for (String name : properties.stringPropertyNames()) {
            boolean plant = name.startsWith("frozenPlant.");
            boolean zombie = name.startsWith("frozenZombie.");
            if (!plant && !zombie) {
                continue;
            }
            String prefix = "frozenZombie.";
            if (plant) {
                prefix = "frozenPlant.";
            }
            GridPosition position = parsePosition(name, prefix, rows, columns);
            TileConfiguration configuration = findTile(tiles, position);
            if (configuration == null
                    || configuration.type != TileType.FROZEN_TILE) {
                throw new StageConfigurationException(name
                        + " requires tile.<x>.<y>=FROZEN_TILE.");
            }
            if (plant) {
                configuration.containedPlantType = required(properties, name);
            }
            else {
                configuration.containedZombieType = required(properties, name);
            }
            if (configuration.containedPlantType != null
                    && configuration.containedZombieType != null) {
                throw new StageConfigurationException(name
                        + " cannot contain both a plant and a zombie.");
            }
        }
    }
    private static GridPosition parsePosition(String name, String prefix,
                                              int rows, int columns) {
        String[] coordinates = name.substring(prefix.length()).split("\\.");
        if (coordinates.length != 2) {
            throw new StageConfigurationException(name
                    + " must use " + prefix + "<x>.<y>=<type>.");
        }
        int x = parseNonNegative(coordinates[0], name + ".x");
        int y = parseNonNegative(coordinates[1], name + ".y");
        if (x >= columns || y >= rows) {
            throw new StageConfigurationException(name
                    + " is outside the configured board.");
        }
        return new GridPosition(x, y);
    }
    private static TileConfiguration findTile(
            List<TileConfiguration> tiles, GridPosition position) {
        for (TileConfiguration tile : tiles) {
            if (tile.position.equals(position)) {
                return tile;
            }
        }
        return null;
    }
    private static SeasonType parseSeason(String value) {
        try {
            return SeasonType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new StageConfigurationException("Unknown season: " + value);
        }
    }
    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new StageConfigurationException("Missing stage property: " + key);
        }
        return value.trim();
    }
    private static List<String> requiredList(Properties properties,
                                             String key) {
        List<String> values = commaSeparated(required(properties, key));
        if (values.isEmpty()) {
            throw new StageConfigurationException(key + " cannot be empty.");
        }
        return values;
    }
    private static int optionalPositive(Properties properties, String key,
                                        int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return parsePositive(value, key);
    }
    private static float optionalNonNegativeFloat(Properties properties,
                                                  String key,
                                                  float defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        float parsed = parseFloat(value, key);
        if (parsed < 0f) {
            throw new StageConfigurationException(key + " cannot be negative.");
        }
        return parsed;
    }
    private static float optionalPositiveFloat(Properties properties,
                                               String key,
                                               float defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return parsePositiveFloat(value, key);
    }
    private static int requiredPositive(Properties properties, String key) {
        return parsePositive(required(properties, key), key);
    }
    private static int requiredNonNegative(Properties properties, String key) {
        return parseNonNegative(required(properties, key), key);
    }
    private static int parsePositive(String value, String key) {
        int parsed = parseInt(value, key);
        if (parsed <= 0) {
            throw new StageConfigurationException(key + " must be positive.");
        }
        return parsed;
    }
    private static int parseNonNegative(String value, String key) {
        int parsed = parseInt(value, key);
        if (parsed < 0) {
            throw new StageConfigurationException(key + " cannot be negative.");
        }
        return parsed;
    }
    private static int parseInt(String value, String key) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new StageConfigurationException(key + " must be an integer.");
        }
    }
    private static float parsePositiveFloat(String value, String key) {
        float parsed = parseFloat(value, key);
        if (parsed <= 0f) {
            throw new StageConfigurationException(key + " must be positive.");
        }
        return parsed;
    }
    private static float parseFloat(String value, String key) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException exception) {
            throw new StageConfigurationException(key + " must be numeric.");
        }
    }
    private static List<String> commaSeparated(String value) {
        List<String> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
