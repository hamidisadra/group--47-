package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlantFoodStrategyRegistry {
    private final Map<String, PlantFoodStrategy> strategies = new LinkedHashMap<>();
    public void register(String effectType, PlantFoodStrategy strategy) {
        if (effectType == null || effectType.isBlank() || strategy == null) {
            throw new IllegalArgumentException("Plant Food strategy registration is incomplete.");
        }
        strategies.put(normalize(effectType), strategy);
    }
    public PlantFoodStrategy resolve(String effectType) {
        return strategies.get(normalize(effectType));
    }
    public boolean contains(String effectType) {
        return strategies.containsKey(normalize(effectType));
    }
    private static String normalize(String value) {
        if (value == null) return "";
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
