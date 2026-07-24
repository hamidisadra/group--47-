package com.pvz.model.support;

import com.pvz.model.interfaces.UpgradeCostProvider;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUpgradeCostProvider implements UpgradeCostProvider {
    private final Map<String, int[]> costs;
    private PropertiesUpgradeCostProvider(Map<String, int[]> costs) {
        this.costs = costs;
    }
    public static PropertiesUpgradeCostProvider load(Path path)
            throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        Map<String, int[]> costs = new LinkedHashMap<>();
        for (String name : properties.stringPropertyNames()) {
            if (!name.startsWith("upgrade.")) {
                continue;
            }
            String[] parts = properties.getProperty(name).split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException(name
                        + " must contain coinCost,seedPacketCost.");
            }
            int coinCost = parseNonNegative(parts[0], name + ".coinCost");
            int seedCost = parseNonNegative(parts[1], name + ".seedPacketCost");
            costs.put(normalizeKey(name.substring("upgrade.".length())),
                    new int[]{coinCost, seedCost});
        }
        return new PropertiesUpgradeCostProvider(costs);
    }
    @Override
    public void configureCost(String plantType, Upgrade upgrade) {
        if (upgrade == null) {
            return;
        }
        int[] cost = costs.get(key(plantType, upgrade.level));
        if (cost != null) {
            upgrade.setCost(cost[0], cost[1]);
        }
    }
    public boolean hasCost(String plantType, int level) {
        return costs.containsKey(key(plantType, level));
    }
    private static int parseNonNegative(String value, String key) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new IllegalArgumentException(key + " cannot be negative.");
            }
            return parsed;
        }
        catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be an integer.");
        }
    }
    private static String key(String plantType, int level) {
        return normalize(plantType) + "." + level;
    }
    private static String normalizeKey(String value) {
        int separator = value.lastIndexOf('.');
        if (separator < 1 || separator == value.length() - 1) {
            throw new IllegalArgumentException(
                    "Upgrade key must be upgrade.<plantType>.<level>.");
        }
        String plantType = value.substring(0, separator);
        int level;
        try {
            level = Integer.parseInt(value.substring(separator + 1));
        }
        catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Upgrade level must be an integer.");
        }
        return key(plantType, level);
    }
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
