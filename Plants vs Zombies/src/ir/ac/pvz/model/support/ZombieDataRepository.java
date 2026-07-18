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

public final class ZombieDataRepository {

    private static final String[] NUMERIC_KEYS = {
            "Hitpoints", "EatDPS", "Speed", "WavePointCost", "Weight",
            "MaxClaimedSunCurrency", "MaxTorchReach", "TimeBetweenRaisings",
            "NumberOfTombsToSpawn", "MaximumGridSquaresToFlyOver",
            "SnowballsPerBarrage", "DelayBetweenCasting",
            "MoveSpeedMultiplierWhileJuggling", "DelayBetweenKnightings",
            "ChargingTime", "LaserCooldownTime", "LaunchCountdown",
            "EnragedDamageScale", "EnragedSpeedScale"
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
        return definitions.get(normalize(alias));
    }

    public ZombieDefinition getByZombieType(String zombieType) {
        return getByAlias(aliasForType(zombieType));
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
        if (zombie instanceof FishermanZombie || zombie instanceof KingZombie) {
            speed = 0f;
        }
        if (zombie instanceof ArcadeZombie) {
            health = zombie.currentHealth;
        }
        zombie.applyBaseData(speed, health, definition.eatDamagePerSecond,
                definition.waveCost, definition.weight,
                definition.canSpawnPlantFood);
    }

    public int getWeight(String zombieType) {
        ZombieDefinition definition = getByZombieType(zombieType);
        return definition == null ? 0 : definition.weight;
    }

    public double getNumber(String zombieType, String key,
                            double defaultValue) {
        ZombieDefinition definition = getByZombieType(zombieType);
        return definition == null ? defaultValue
                : definition.getNumber(key, defaultValue);
    }

    private static ZombieDataRepository load() {
        try (InputStream input = ir.ac.pvz.model.support.DataFileLocator.open("zombies.json")) {
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, ZombieDefinition> definitions = new LinkedHashMap<>();
            for (String object : splitTopLevelObjects(json)) {
                ZombieDefinition definition = parseDefinition(object);
                if (definition != null) {
                    definitions.put(normalize(definition.alias), definition);
                }
            }
            if (definitions.size() != 27) {
                throw new IOException("zombies.json must contain 27 records.");
            }
            return new ZombieDataRepository(definitions);
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static ZombieDefinition parseDefinition(String object) {
        String alias = stringValue(object, "aliases");
        if (alias == null) {
            return null;
        }
        ZombieDefinition definition = new ZombieDefinition();
        definition.alias = alias;
        definition.speed = (float) numberValue(object, "Speed", 0d);
        definition.health = (int) numberValue(object, "Hitpoints", 0d);
        definition.eatDamagePerSecond = (int) numberValue(object, "EatDPS", 0d);
        definition.waveCost = (int) numberValue(object, "WavePointCost", 0d);
        definition.weight = (int) numberValue(object, "Weight", 0d);
        definition.canSpawnPlantFood = booleanValue(object,
                "CanSpawnPlantFood", false);
        for (String key : NUMERIC_KEYS) {
            double value = numberValue(object, key, Double.NaN);
            if (!Double.isNaN(value)) {
                definition.numericProperties.put(key, value);
            }
        }
        return definition;
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
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    quoted = false;
                }
                continue;
            }
            if (character == '"') {
                quoted = true;
            } else if (character == '{') {
                if (depth == 0) {
                    start = index;
                }
                depth++;
            } else if (character == '}') {
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
        } else {
            pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                    + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
        }
        Matcher matcher = pattern.matcher(object);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static double numberValue(String object, String key,
                                      double defaultValue) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(object);
        return matcher.find() ? Double.parseDouble(matcher.group(1))
                : defaultValue;
    }

    private static boolean booleanValue(String object, String key,
                                        boolean defaultValue) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                + "\\\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(object);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1))
                : defaultValue;
    }

    private static String aliasForType(String zombieType) {
        switch (normalize(zombieType)) {
            case "basiczombie": return "ZombieDefault";
            case "coneheadzombie": return "ZombieArmor1";
            case "bucketheadzombie": return "ZombieArmor2";
            case "blockheadzombie": return "ZombieArmor4";
            case "knightzombie": return "ZombieDarkArmor3";
            case "gargantuar": return "ZombieGargantuar";
            case "impzombie": return "ZombieImp";
            case "razombie": return "ZombieRa";
            case "explorerzombie": return "ZombieExplorer";
            case "tombraiserzombie": return "ZombieTombRaiser";
            case "dodoriderzombie": return "ZombieIceAgeDodo";
            case "hunterzombie": return "ZombieIceAgeHunter";
            case "troglobite": return "ZombieIceAgeTroglobite";
            case "fishermanzombie": return "ZombieBeachFisherman";
            case "octopuszombie": return "ZombieBeachOctopus";
            case "snorkelzombie": return "ZombieBeachSnorkel";
            case "jesterzombie": return "ZombieDarkJuggler";
            case "wizardzombie": return "ZombieWizard";
            case "kingzombie": return "ZombieDarkKing";
            case "impdragon": return "ZombieDarkImpDragon";
            case "footballzombie": return "ZombieModernAllStar";
            case "parasolzombie": return "ZombieLostCityJane";
            case "turquoisezombie": return "ZombieCrystalSkull";
            case "prospectorzombie": return "ZombieProspector";
            case "pianistzombie": return "ZombiePiano";
            case "newspaperzombie": return "ZombieNewspaper";
            case "arcadezombie": return "ZombieArcade";
            default: return zombieType;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace("-", "")
                .replace("_", "").replace(" ", "").toLowerCase();
    }
}
