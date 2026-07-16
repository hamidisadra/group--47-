package com.pvz.model.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlantDefinition {

    public int definitionId;
    public String name;
    public String category;
    public String tags;
    public int cost;
    public int baseHealth;
    public String damage;
    public String baseAbility;
    public String plantFoodEffect;
    public float actionInterval;
    public float recharge;
    public Map<Integer, String> levelDescriptions;

    public PlantDefinition() {
        levelDescriptions = new LinkedHashMap<>();
    }

    public Map<Integer, String> getLevelDescriptions() {
        return Collections.unmodifiableMap(levelDescriptions);
    }
}
