package com.pvz.model.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ZombieDefinition {

    public String alias;
    public float speed;
    public int health;
    public int eatDamagePerSecond;
    public int waveCost;
    public int weight;
    public boolean canSpawnPlantFood;
    public Map<String, Double> numericProperties;

    public ZombieDefinition() {
        numericProperties = new LinkedHashMap<>();
    }

    public double getNumber(String key, double defaultValue) {
        return numericProperties.getOrDefault(key, defaultValue);
    }

    public Map<String, Double> getNumericProperties() {
        return Collections.unmodifiableMap(numericProperties);
    }
}
