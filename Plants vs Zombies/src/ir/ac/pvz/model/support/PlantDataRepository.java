package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.plants.ExplosivePlant;
import ir.ac.pvz.model.plants.LobberPlant;
import ir.ac.pvz.model.plants.ShooterPlant;
import ir.ac.pvz.model.plants.WallPlant;
import ir.ac.pvz.model.plants.SunProducerPlant;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlantDataRepository implements PlantDefinitionRepository {
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
        plant.abilityType = definition.abilityType;
        plant.abilityValue = definition.abilityValue;
        plant.plantFoodType = definition.plantFoodType;
        plant.plantFoodValue = definition.plantFoodValue;
        if (plant instanceof SunProducerPlant) {
            SunProducerPlant producer = (SunProducerPlant) plant;
            producer.sunAmount = Math.max(0,
                    (int) Math.round(definition.abilityValue));
            producer.productionInterval = definition.actionInterval;
        }
        if (plant instanceof ShooterPlant
                && normalize(definition.abilityType).equals("shootprojectile")) {
            ((ShooterPlant) plant).multiShot = Math.max(1,
                    (int) Math.round(definition.abilityValue));
        }
        if (normalize(definition.abilityType).equals("temporaryshooter")) {
            plant.setLifeSpanSeconds(60f);
        }
        plant.plantTags = parseTags(definition.tags);
        plant.canStack = plant.plantTags.contains(PlantTag.STACK);
        applyDamage(plant, definition.damage);
    }
    private void applyDamage(Plant plant, String expression) {
        if (expression == null || expression.toLowerCase().contains("insta")) {
            return;
        }
        String normalizedExpression = toLatinDigits(expression);
        Matcher matcher = FIRST_NUMBER.matcher(normalizedExpression);
        if (!matcher.find()) {
            return;
        }
        int damage = Integer.parseInt(matcher.group());
        plant.attackPower = damage;
        if (plant instanceof ShooterPlant) {
            ShooterPlant shooter = (ShooterPlant) plant;
            shooter.damage = damage;
            Matcher multiplier = Pattern.compile("[xX](\\d+)").matcher(
                    normalizedExpression);
            if (multiplier.find()) {
                shooter.multiShot = Integer.parseInt(multiplier.group(1));
            }
        }
        else if (plant instanceof LobberPlant) {
            ((LobberPlant) plant).damage = damage;
        }
        else if (plant instanceof ExplosivePlant) {
            ((ExplosivePlant) plant).explosionDamage = damage;
        }
        else if (plant instanceof WallPlant
                && normalize(plant.type).equals("endurian")) {
            ((WallPlant) plant).reflectDamage = damage;
        }
    }
    private PlantCategory resolveCategory(PlantDefinition definition) {
        if (normalize(definition.name).endsWith("mint")) {
            return PlantCategory.MINT;
        }
        switch (normalize(definition.category)) {
            case "sunproducer":
                return PlantCategory.SUN_PRODUCER;
            case "shooter":
                return PlantCategory.SHOOTER;
            case "homing":
                return PlantCategory.HOMING;
            case "strikethrough":
                return PlantCategory.STRIKE_THROUGH;
            case "lobber":
                return PlantCategory.LOBBER;
            case "explosive":
                return PlantCategory.EXPLOSIVE;
            case "melee":
                return PlantCategory.MELEE;
            case "wallnut":
                return PlantCategory.WALL;
            case "modifier":
                return PlantCategory.MODIFIER;
            default:
                throw new IllegalStateException("Unknown plant category: "
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
            case "day":
                return PlantTag.DAY;
            case "night":
                return PlantTag.NIGHT;
            case "shroom":
                return PlantTag.SHROOM;
            case "wrampup": case "rampup":
                return PlantTag.RAMP_UP;
            case "pea":
                return PlantTag.PEA;
            case "ice":
                return PlantTag.ICE;
            case "fire":
                return PlantTag.FIRE;
            case "stack":
                return PlantTag.STACK;
            case "charge":
                return PlantTag.CHARGE;
            case "magic":
                return PlantTag.MAGIC;
            case "poison":
                return PlantTag.POISON;
            case "water":
                return PlantTag.WATER;
            case "aoe":
                return PlantTag.AOE;
            case "trap":
                return PlantTag.TRAP;
            case "movezombies":
                return PlantTag.MOVE_ZOMBIES;
            case "sun":
                return PlantTag.SUN;
            case "explosive":
                return PlantTag.EXPLOSIVE;
            default:
                return null;
        }
    }
    private static PlantDataRepository load() {
        try {
            Map<String, PlantDefinition> definitions = new LinkedHashMap<>();
            Set<Integer> definitionIds = new HashSet<>();
            for (Map<String, String> row : JsonTableReader.readRootObjectArray(
                    "plants.json")) {
                PlantDefinition definition = fromJson(row);
                validateDefinition(definition);
                if (!definitionIds.add(definition.definitionId)) {
                    throw new IOException("Duplicate plant ID: "
                            + definition.definitionId);
                }
                if (definitions.containsKey(normalize(definition.name))) {
                    throw new IOException("Duplicate plant definition: "
                            + definition.name);
                }
                definitions.put(normalize(definition.name), definition);
            }
            if (definitions.size() < 69) {
                throw new IOException("plants.json must contain at least the 69 documented plant rows.");
            }
            return new PlantDataRepository(definitions);
        }
        catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
    private static PlantDefinition fromRow(Map<String, String> row) {
        PlantDefinition definition = new PlantDefinition();
        definition.definitionId = parseInt(requiredCell(row, "ID"));
        definition.name = requiredCell(row, "Name");
        definition.category = requiredCell(row, "Category");
        definition.tags = requiredCell(row, "Tags");
        definition.cost = parseInt(requiredCell(row, "Cost"));
        definition.baseHealth = parseInt(requiredCell(row, "Base HP"));
        definition.damage = requiredCell(row, "Damage");
        definition.baseAbility = requiredCell(row, "Base Ability");
        definition.plantFoodEffect = requiredCell(row, "Plant Food Effect");
        definition.levelDescriptions.put(2, row.get("Lvl 2"));
        definition.levelDescriptions.put(3, row.get("Lvl 3"));
        definition.levelDescriptions.put(4, row.get("Lvl 4"));
        definition.actionInterval = parseFloat(requiredCell(row,
                "Action Interval (s)"));
        definition.recharge = parseFloat(requiredCell(row, "Recharge (s)"));
        return definition;
    }
    private static PlantDefinition fromJson(Map<String, String> row) {
        PlantDefinition definition = new PlantDefinition();
        definition.definitionId = parseInt(requiredField(row, "id"));
        definition.name = requiredField(row, "name");
        definition.category = requiredField(row, "category");
        definition.tags = String.join(",",
                readStringArray(requiredField(row, "tags")));
        definition.cost = parseInt(requiredField(row, "cost"));
        definition.baseHealth = parseInt(requiredField(row, "baseHp"));
        definition.damage = requiredField(row, "damage");
        definition.baseAbility = requiredField(row, "baseAbility");
        definition.plantFoodEffect = requiredField(row, "plantFoodEffect");
        definition.abilityType = requiredField(row, "abilityType");
        definition.abilityValue = parseDouble(requiredField(row, "abilityValue"));
        definition.plantFoodType = requiredField(row, "plantFoodType");
        definition.plantFoodValue = parseDouble(requiredField(row, "plantFoodValue"));
        for (Map<String, String> upgrade : readObjectArray(
                requiredField(row, "upgrades"))) {
            int level = parseInt(requiredField(upgrade, "level"));
            if (level >= 2 && level <= 4) {
                definition.levelDescriptions.put(level,
                        requiredField(upgrade, "description"));
            }
        }
        for (int level = 2; level <= 4; level++) {
            if (!definition.levelDescriptions.containsKey(level)) {
                throw new IllegalStateException("Missing level " + level
                        + " upgrade for " + definition.name + ".");
            }
        }
        definition.actionInterval = parseFloat(requiredField(row,
                "actionInterval"));
        definition.recharge = parseFloat(requiredField(row, "recharge"));
        return definition;
    }
    private static List<String> readStringArray(String json) {
        try {
            return JsonTableReader.readInlineStringArray(json);
        }
        catch (IOException exception) {
            throw new IllegalStateException("Invalid plant tags array.", exception);
        }
    }
    private static List<Map<String, String>> readObjectArray(String json) {
        try {
            return JsonTableReader.readInlineObjectArray(json);
        }
        catch (IOException exception) {
            throw new IllegalStateException("Invalid plant upgrades array.", exception);
        }
    }
    private static String requiredField(Map<String, String> row, String field) {
        if (!row.containsKey(field) || row.get(field) == null
                || row.get(field).isBlank()) {
            throw new IllegalStateException("Missing plant JSON field or value: "
                    + field);
        }
        return row.get(field).trim();
    }
    private static String requiredCell(Map<String, String> row, String column) {
        if (!row.containsKey(column) || row.get(column) == null
                || row.get(column).isBlank()) {
            throw new IllegalStateException("Missing plant data column or value: "
                    + column);
        }
        return row.get(column).trim();
    }
    private static void validateDefinition(PlantDefinition definition)
            throws IOException {
        if (definition.definitionId <= 0 || definition.name.isBlank()) {
            throw new IOException("Invalid plant identity in official table.");
        }
        if (definition.cost < 0 || definition.baseHealth < 0
                || definition.actionInterval < 0f || definition.recharge < 0f) {
            throw new IOException("Negative plant value for " + definition.name);
        }
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
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
    private static double parseDouble(String value) {
        if (value == null || value.isBlank() || value.equals("-")) {
            return 0d;
        }
        return Double.parseDouble(value);
    }
    private static String toLatinDigits(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character >= '\u06F0' && character <= '\u06F9') {
                result.append((char) ('0' + character - '\u06F0'));
            }
            else if (character >= '\u0660' && character <= '\u0669') {
                result.append((char) ('0' + character - '\u0660'));
            }
            else {
                result.append(character);
            }
        }
        return result.toString();
    }
}
