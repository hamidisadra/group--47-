package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.zombies.ArcadeZombie;
import ir.ac.pvz.model.zombies.FishermanZombie;
import ir.ac.pvz.model.zombies.KingZombie;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ZombieDataRepository implements ZombieDefinitionRepository {
    private static final String[] NUMERIC_KEYS = {
            "Hitpoints", "EatDPS", "Speed", "WavePointCost", "Weight",
            "MaxClaimedSunCurrency", "MaxTorchReach", "TimeBetweenRaisings",
            "NumberOfTombsToSpawn", "MaximumGridSquaresToFlyOver",
            "SnowballsPerBarrage", "DelayBetweenCasting",
            "MoveSpeedMultiplierWhileJuggling", "DelayBetweenKnightings",
            "ChargingTime", "LaserCooldownTime", "LaunchCountdown",
            "EnragedDamageScale", "EnragedSpeedScale",
            "ArcadeMachineHealth"
    };
    private static final ZombieDataRepository INSTANCE = load();
    private final Map<String, ZombieDefinition> definitions;
    private ZombieDataRepository(Map<String, ZombieDefinition> definitions) {
        this.definitions = definitions;
    }
    public static ZombieDataRepository getInstance() {
        return INSTANCE;
    }
    public ZombieDefinition getByAlias(String alias) {
        ZombieDefinition direct = definitions.get(normalize(alias));
        if (direct != null) {
            return direct;
        }
        String normalizedAlias = normalize(alias);
        for (ZombieDefinition definition : definitions.values()) {
            for (String candidate : definition.aliases) {
                if (normalize(candidate).equals(normalizedAlias)) {
                    return definition;
                }
            }
        }
        return null;
    }
    public ZombieDefinition getByZombieType(String zombieType) {
        ZombieDefinition direct = definitions.get(normalize(zombieType));
        if (direct != null) {
            return direct;
        }
        String normalizedType = normalize(zombieType);
        for (ZombieDefinition definition : definitions.values()) {
            if (normalize(definition.gameType).equals(normalizedType)
                    || normalize(definition.runtimeType).equals(normalizedType)) {
                return definition;
            }
        }
        return getByAlias(zombieType);
    }
    public List<ZombieDefinition> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(definitions.values()));
    }
    public void applyTo(Zombie zombie, String zombieType) {
        ZombieDefinition definition = getByZombieType(zombieType);
        if (zombie == null || definition == null) {
            return;
        }
        float speed = definition.speed;
        int health = definition.health;
        if (zombie instanceof ArcadeZombie) {
            health = zombie.currentHealth;
        }
        zombie.applyBaseData(speed, health, definition.eatDamagePerSecond,
                definition.waveCost, definition.weight,
                definition.canSpawnPlantFood);
        for (String abilityName : definition.abilities) {
            boolean alreadyPresent = zombie.abilities.stream().anyMatch(ability ->
                    normalize(ability.name).equals(normalize(abilityName)));
            if (!alreadyPresent) {
                zombie.abilities.add(ZombieAbilityRegistry.create(
                        abilityName, definition));
            }
        }
    }
    public int getWeight(String zombieType) {
        ZombieDefinition definition = getByZombieType(zombieType);
        if (definition == null) {
            return 0;
        }
        return definition.weight;
    }
    public double getNumber(String zombieType, String key,
                            double defaultValue) {
        ZombieDefinition definition = getByZombieType(zombieType);
        if (definition == null) {
            return defaultValue;
        }
        return definition.getNumber(key, defaultValue);
    }
    private static ZombieDataRepository load() {
        try (InputStream input = DataFileLocator.open("zombies.json")) {
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, ZombieDefinition> definitions = new LinkedHashMap<>();
            for (String object : splitTopLevelObjects(json)) {
                ZombieDefinition definition = parseDefinition(object);
                if (definition != null) {
                    validateDefinition(definition);
                    if (definitions.containsKey(normalize(definition.alias))) {
                        throw new IOException("Duplicate zombie definition: "
                                + definition.alias);
                    }
                    definitions.put(normalize(definition.alias), definition);
                }
            }
            if (definitions.size() < 27) {
                throw new IOException("zombies.json must contain at least 27 records.");
            }
            return new ZombieDataRepository(definitions);
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
    private static ZombieDefinition parseDefinition(String object) {
        List<String> aliases = stringArrayValue(object, "aliases");
        if (aliases.isEmpty()) {
            return null;
        }
        ZombieDefinition definition = new ZombieDefinition();
        definition.aliases.addAll(aliases);
        definition.alias = aliases.get(0);
        String gameType = stringValue(object, "GameType");
        if (gameType == null) {
            definition.gameType = definition.alias;
        }
        else {
            definition.gameType = gameType;
        }
        String runtimeType = stringValue(object, "RuntimeType");
        if (runtimeType == null) {
            definition.runtimeType = definition.gameType;
        }
        else {
            definition.runtimeType = runtimeType;
        }
        definition.speed = (float) numberValue(object, "Speed", 0d);
        definition.health = (int) numberValue(object, "Hitpoints", 0d);
        definition.eatDamagePerSecond = (int) numberValue(object, "EatDPS", 0d);
        definition.waveCost = (int) numberValue(object, "WavePointCost", 0d);
        definition.weight = (int) numberValue(object, "Weight", 0d);
        definition.canSpawnPlantFood = booleanValue(object,
                "CanSpawnPlantFood", false);
        definition.abilities.addAll(stringArrayValue(object, "Abilities"));
        definition.armorAliases.addAll(referenceArrayValue(object,
                "ZombieArmorProps"));
        for (String key : NUMERIC_KEYS) {
            double value = numberValue(object, key, Double.NaN);
            if (!Double.isNaN(value)) {
                definition.numericProperties.put(key, value);
            }
        }
        return definition;
    }
    private static void validateDefinition(ZombieDefinition definition)
            throws IOException {
        if (definition.alias == null || definition.alias.isBlank()
                || definition.gameType == null || definition.gameType.isBlank()
                || definition.runtimeType == null || definition.runtimeType.isBlank()
                || definition.health <= 0 || definition.speed < 0f
                || definition.eatDamagePerSecond < 0
                || definition.waveCost <= 0 || definition.weight <= 0) {
            String invalidAlias = definition.alias;
            if (invalidAlias == null) {
                invalidAlias = "unknown";
            }
            throw new IOException("Invalid official zombie definition: "
                    + invalidAlias);
        }
        for (String ability : definition.abilities) {
            if (!ZombieAbilityRegistry.contains(ability)) {
                throw new IOException("Unknown ability " + ability + " for "
                        + definition.gameType + ".");
            }
        }
        for (String armorAlias : definition.armorAliases) {
            if (ArmorDataRepository.getInstance().getHealth(armorAlias) <= 0) {
                throw new IOException("Unknown armor " + armorAlias + " for "
                        + definition.gameType + ".");
            }
        }
    }
    private static List<String> splitTopLevelObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean quoted = false;
        boolean escaped = false;
        for (int index = 0; index < json.length(); index++) {
            char character = json.charAt(index);
            if (quoted) {
                if (escaped) {
                    escaped = false;
                }
                else if (character == '\\') {
                    escaped = true;
                }
                else if (character == '"') {
                    quoted = false;
                }
                continue;
            }
            if (character == '"') {
                quoted = true;
            }
            else if (character == '{') {
                if (depth == 0) {
                    start = index;
                }
                depth++;
            }
            else if (character == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(json.substring(start, index + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }
    private static String stringValue(String object, String key) {
        Pattern pattern;
        if (key.equals("aliases")) {
            pattern = Pattern.compile("\\\"aliases\\\"\\s*:\\s*\\[\\s*\\\"([^\\\"]+)\\\"");
        }
        else {
            pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                    + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
        }
        Matcher matcher = pattern.matcher(object);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }
    private static double numberValue(String object, String key,
                                      double defaultValue) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(object);
        if (!matcher.find()) {
            return defaultValue;
        }
        return Double.parseDouble(matcher.group(1));
    }
    private static boolean booleanValue(String object, String key,
                                        boolean defaultValue) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                + "\\\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(object);
        if (!matcher.find()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(matcher.group(1));
    }
    private static List<String> stringArrayValue(String object, String key) {
        List<String> values = new ArrayList<>();
        Pattern arrayPattern = Pattern.compile("\\\"" + Pattern.quote(key)
                + "\\\"\\s*:\\s*\\[([^]]*)]", Pattern.DOTALL);
        Matcher array = arrayPattern.matcher(object);
        if (!array.find()) {
            return values;
        }
        Matcher item = Pattern.compile("\\\"([^\\\"]+)\\\"")
                .matcher(array.group(1));
        while (item.find()) {
            values.add(item.group(1));
        }
        return values;
    }
    private static List<String> referenceArrayValue(String object, String key) {
        List<String> values = new ArrayList<>();
        for (String reference : stringArrayValue(object, key)) {
            int start = reference.indexOf('(');
            int end = reference.indexOf('@');
            if (start >= 0 && end > start) {
                values.add(reference.substring(start + 1, end));
            } else {
                values.add(reference);
            }
        }
        return values;
    }
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
