package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.enums.PlantCategory;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.plants.ExplosivePlant;
import com.pvz.model.plants.LobberPlant;
import com.pvz.model.plants.ShooterPlant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlantDataRepository {

    private static final Pattern FIRST_NUMBER = Pattern.compile("-?\\d+");
    private static final PlantDataRepository INSTANCE = load();

    private final Map<String, PlantDefinition> definitions;

    private PlantDataRepository(Map<String, PlantDefinition> definitions) {
        this.definitions = definitions;
    }

    public static PlantDataRepository getInstance() {
        return INSTANCE;
    }

    public PlantDefinition get(String plantType) {
        return definitions.get(normalize(plantType));
    }

    public List<PlantDefinition> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(definitions.values()));
    }

    public void applyTo(Plant plant) {
        if (plant == null) {
            return;
        }
        PlantDefinition definition = get(plant.type);
        if (definition == null) {
            throw new IllegalStateException("No plant data for " + plant.type + ".");
        }
        plant.name = definition.name;
        plant.type = definition.name;
        plant.cost = definition.cost;
        plant.sunCost = definition.cost;
        plant.baseHp = definition.baseHealth;
        plant.currentHp = definition.baseHealth;
        plant.health = definition.baseHealth;
        plant.rechargeTime = definition.recharge;
        plant.actionInterval = definition.actionInterval;
        plant.category = resolveCategory(definition);
        plant.plantTags = parseTags(definition.tags);
        plant.canStack = plant.plantTags.contains(PlantTag.STACK);
        applyDamage(plant, definition.damage);
    }

    private void applyDamage(Plant plant, String expression) {
        if (expression == null || expression.toLowerCase().contains("insta")) {
            return;
        }
        Matcher matcher = FIRST_NUMBER.matcher(expression);
        if (!matcher.find()) {
            return;
        }
        int damage = Integer.parseInt(matcher.group());
        plant.attackPower = damage;
        if (plant instanceof ShooterPlant) {
            ShooterPlant shooter = (ShooterPlant) plant;
            shooter.damage = damage;
            Matcher multiplier = Pattern.compile("[xX](\\d+)").matcher(expression);
            if (multiplier.find()) {
                shooter.multiShot = Integer.parseInt(multiplier.group(1));
            }
        } else if (plant instanceof LobberPlant) {
            ((LobberPlant) plant).damage = damage;
        } else if (plant instanceof ExplosivePlant) {
            ((ExplosivePlant) plant).explosionDamage = damage;
        }
    }

    private PlantCategory resolveCategory(PlantDefinition definition) {
        if (normalize(definition.name).endsWith("mint")) {
            return PlantCategory.MINT;
        }
        switch (normalize(definition.category)) {
            case "sunproducer": return PlantCategory.SUN_PRODUCER;
            case "shooter": return PlantCategory.SHOOTER;
            case "homing": return PlantCategory.HOMING;
            case "strikethrough": return PlantCategory.STRIKE_THROUGH;
            case "lobber": return PlantCategory.LOBBER;
            case "explosive": return PlantCategory.EXPLOSIVE;
            case "melee": return PlantCategory.MELEE;
            case "wallnut": return PlantCategory.WALL;
            case "modifier": return PlantCategory.MODIFIER;
            default: throw new IllegalStateException("Unknown plant category: "
                    + definition.category);
        }
    }

    private List<PlantTag> parseTags(String text) {
        List<PlantTag> tags = new ArrayList<>();
        if (text == null || text.trim().isEmpty() || text.trim().equals("-")) {
            return tags;
        }
        for (String value : text.split(",")) {
            PlantTag tag = resolveTag(value);
            if (tag != null && !tags.contains(tag)) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private PlantTag resolveTag(String value) {
        switch (normalize(value)) {
            case "day": return PlantTag.DAY;
            case "night": return PlantTag.NIGHT;
            case "shroom": return PlantTag.SHROOM;
            case "wrampup": case "rampup": return PlantTag.RAMP_UP;
            case "pea": return PlantTag.PEA;
            case "ice": return PlantTag.ICE;
            case "fire": return PlantTag.FIRE;
            case "stack": return PlantTag.STACK;
            case "charge": return PlantTag.CHARGE;
            case "magic": return PlantTag.MAGIC;
            case "poison": return PlantTag.POISON;
            case "water": return PlantTag.WATER;
            case "aoe": return PlantTag.AOE;
            case "trap": return PlantTag.TRAP;
            case "movezombies": return PlantTag.MOVE_ZOMBIES;
            case "sun": return PlantTag.SUN;
            case "explosive": return PlantTag.EXPLOSIVE;
            default: return null;
        }
    }

    private static PlantDataRepository load() {
        try {
            Map<String, PlantDefinition> definitions = new LinkedHashMap<>();
            for (Map<String, String> row : XlsxTableReader.readFirstSheet(
                    "plants.xlsx")) {
                PlantDefinition definition = fromRow(row);
                definitions.put(normalize(definition.name), definition);
            }
            if (definitions.size() != 69) {
                throw new IOException("plants.xlsx must contain 69 plant rows.");
            }
            return new PlantDataRepository(definitions);
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static PlantDefinition fromRow(Map<String, String> row) {
        PlantDefinition definition = new PlantDefinition();
        definition.definitionId = parseInt(row.get("ID"));
        definition.name = row.get("Name");
        definition.category = row.get("Category");
        definition.tags = row.get("Tags");
        definition.cost = parseInt(row.get("Cost"));
        definition.baseHealth = parseInt(row.get("Base HP"));
        definition.damage = row.get("Damage");
        definition.baseAbility = row.get("Base Ability");
        definition.plantFoodEffect = row.get("Plant Food Effect");
        definition.levelDescriptions.put(2, row.get("Lvl 2"));
        definition.levelDescriptions.put(3, row.get("Lvl 3"));
        definition.levelDescriptions.put(4, row.get("Lvl 4"));
        definition.actionInterval = parseFloat(row.get("Action Interval (s)"));
        definition.recharge = parseFloat(row.get("Recharge (s)"));
        return definition;
    }

    private static int parseInt(String value) {
        if (value == null || value.isBlank() || value.equals("-")) {
            return 0;
        }
        return (int) Math.round(Double.parseDouble(value));
    }

    private static float parseFloat(String value) {
        if (value == null || value.isBlank() || value.equals("-")) {
            return 0f;
        }
        return Float.parseFloat(value);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace("-", "")
                .replace("_", "").replace(" ", "").toLowerCase();
    }
}
