package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.enums.TargetingMode;
import ir.ac.pvz.model.support.PlantDataRepository;
import ir.ac.pvz.model.support.PlantDefinition;
import ir.ac.pvz.model.support.PlantDefinitionRepository;
import ir.ac.pvz.model.support.BalanceDefaults;
import ir.ac.pvz.model.support.Upgrade;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlantFactory {
    private static final Map<String, IntFunction<Plant>> REGISTRY =
            createRegistry();
    private static final Map<String, BiFunction<Integer, PlantDefinition, Plant>>
            ARCHETYPES = createArchetypes();
    private PlantFactory() {
    }
    public static Plant create(int id, String type) {
        return create(id, type, PlantDataRepository.getInstance());
    }
    public static Plant create(int id, String type,
                               PlantDefinitionRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Plant repository is required.");
        }
        String normalized = normalize(type);
        IntFunction<Plant> constructor = REGISTRY.get(normalized);
        Plant plant;
        if (constructor == null) {
            plant = createFromData(id, type, repository);
        }
        else {
            plant = constructor.apply(id);
        }
        if (plant != null) {
            repository.applyTo(plant);
            Upgrade.configureFor(plant, normalized);
        }
        return plant;
    }
    private static Map<String, IntFunction<Plant>> createRegistry() {
        Map<String, IntFunction<Plant>> registry = new LinkedHashMap<>();
        register(registry, "Sunflower", Sunflower::new);
        register(registry, "Twin Sunflower", TwinSunflower::new);
        register(registry, "Sun-shroom", SunShroom::new);
        register(registry, "Primal Sunflower", PrimalSunflower::new);
        register(registry, "Snow Pea", SnowPea::new);
        register(registry, "Caulipower", Caulipower::new);
        register(registry, "Fire Peashooter", FirePea::new);
        register(registry, "Winter Melon", WinterMelon::new);
        register(registry, "Potato Mine", PotatoMine::new);
        register(registry, "Chomper", Chomper::new);
        register(registry, "Kiwibeast", Kiwibeast::new);
        register(registry, "Explode-o-nut", ExplodeONut::new);
        register(registry, "Hypno-shroom", HypnoShroom::new);
        register(registry, "Imitater", Imitater::new);
        return registry;
    }
    private static void register(Map<String, IntFunction<Plant>> registry,
                                 String type, IntFunction<Plant> constructor) {
        registry.put(normalize(type), constructor);
    }
    private static Plant createFromData(int id, String type,
                                        PlantDefinitionRepository repository) {
        PlantDefinition definition = repository.get(type);
        if (definition == null) {
            return null;
        }
        if (normalize(definition.name).endsWith("mint")) {
            return createMint(id, definition);
        }
        BiFunction<Integer, PlantDefinition, Plant> builder = ARCHETYPES.get(
                normalize(definition.category));
        if (builder == null) {
            return null;
        }
        return builder.apply(id, definition);
    }
    private static Map<String, BiFunction<Integer, PlantDefinition, Plant>>
    createArchetypes() {
        Map<String, BiFunction<Integer, PlantDefinition, Plant>> builders =
                new LinkedHashMap<>();
        builders.put("sunproducer", (id, data) -> new SunProducerPlant(id,
                data.name, data.cost, data.baseHealth, data.recharge,
                firstNumber(data.baseAbility, 25), data.actionInterval));
        builders.put("shooter", (id, data) -> new ShooterPlant(id, data.name,
                data.cost, data.baseHealth, data.recharge, data.actionInterval,
                firstNumber(data.damage, 0), projectileType(data),
                shotCount(data.damage)));
        builders.put("lobber", (id, data) -> new LobberPlant(id, data.name,
                data.cost, data.baseHealth, data.recharge, data.actionInterval,
                firstNumber(data.damage, 0),
                areaRadius(data)));
        builders.put("explosive", (id, data) -> new ExplosivePlant(id,
                data.name, data.cost, data.baseHealth, data.recharge,
                data.actionInterval, firstNumber(data.damage, 0), 1f,
                !normalize(data.tags).contains("trap")
                        && !normalize(data.name).equals("gravebuster")));
        builders.put("melee", (id, data) -> new MeleePlant(id, data.name,
                data.cost, data.baseHealth, data.recharge, data.actionInterval,
                firstNumber(data.damage, 0), false));
        builders.put("wallnut", (id, data) -> new WallPlant(id, data.name,
                data.cost, data.baseHealth, data.recharge, 0));
        builders.put("modifier", (id, data) -> new ModifierPlant(id, data.name,
                data.cost, data.baseHealth, data.recharge, data.actionInterval));
        builders.put("strikethrough", (id, data) -> new StrikeThroughPlant(id,
                data.name, data.cost, data.baseHealth, data.recharge,
                data.actionInterval, firstNumber(data.damage, 0),
                pierceCount(data)));
        builders.put("homing", (id, data) -> new HomingPlant(id, data.name,
                data.cost, data.baseHealth, data.recharge, data.actionInterval,
                firstNumber(data.damage, 0), TargetingMode.NEAREST));
        return builders;
    }
    private static Plant createMint(int id, PlantDefinition definition) {
        PlantCategory family = PlantCategory.MODIFIER;
        float duration = BalanceDefaults.ENCHANT_MINT_DURATION_SECONDS;
        String type = normalize(definition.name);
        if (type.equals("enlightenmint")) {
            family = PlantCategory.SUN_PRODUCER;
            duration = BalanceDefaults.ENLIGHTEN_MINT_DURATION_SECONDS;
        }
        else if (type.equals("appeasemint")) {
            family = PlantCategory.SHOOTER;
            duration = BalanceDefaults.APPEASE_MINT_DURATION_SECONDS;
        }
        else if (type.equals("armamint")) {
            family = PlantCategory.LOBBER;
            duration = BalanceDefaults.ARMA_MINT_DURATION_SECONDS;
        }
        else if (type.equals("bombardmint")) {
            family = PlantCategory.EXPLOSIVE;
            duration = BalanceDefaults.BOMBARD_MINT_DURATION_SECONDS;
        }
        else if (type.equals("enforcemint")) {
            family = PlantCategory.MELEE;
            duration = BalanceDefaults.ENFORCE_MINT_DURATION_SECONDS;
        }
        else if (type.equals("reinforcemint")) {
            family = PlantCategory.WALL;
            duration = BalanceDefaults.REINFORCE_MINT_DURATION_SECONDS;
        }
        else if (type.equals("piercemint")) {
            family = PlantCategory.STRIKE_THROUGH;
            duration = BalanceDefaults.PIERCE_MINT_DURATION_SECONDS;
        }
        else if (type.equals("cattailmint")) {
            family = PlantCategory.HOMING;
            duration = BalanceDefaults.CATTAIL_MINT_DURATION_SECONDS;
        }
        return new MintPlant(id, definition.name, definition.recharge,
                duration, family);
    }
    private static int firstNumber(String expression, int fallback) {
        if (expression == null) {
            return fallback;
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(toLatinDigits(expression));
        if (!matcher.find()) {
            return fallback;
        }
        return Integer.parseInt(matcher.group());
    }
    private static int shotCount(String expression) {
        if (expression == null) {
            return 1;
        }
        Matcher matcher = Pattern.compile("[xX](\\d+)").matcher(
                toLatinDigits(expression));
        if (!matcher.find()) {
            return 1;
        }
        return Integer.parseInt(matcher.group(1));
    }
    private static float areaRadius(PlantDefinition definition) {
        if (normalize(definition.tags).contains("aoe")) {
            return 1f;
        }
        return 0f;
    }

    private static int pierceCount(PlantDefinition definition) {
        if (normalize(definition.name).equals("cactus")) {
            return 3;
        }
        return 1;
    }
    private static String toLatinDigits(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character >= '\u06F0' && character <= '\u06F9') {
                result.append((char) ('0' + character - '\u06F0'));
            } else if (character >= '\u0660' && character <= '\u0669') {
                result.append((char) ('0' + character - '\u0660'));
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }
    private static ProjectileType projectileType(PlantDefinition definition) {
        String tags = normalize(definition.tags);
        if (tags.contains("poison")) {
            return ProjectileType.POISON;
        }
        if (tags.contains("ice")) {
            return ProjectileType.ICE;
        }
        if (tags.contains("fire")) {
            return ProjectileType.FIRE;
        }
        return ProjectileType.PEA;
    }
    public static String[] getPlantTypes() {
        return PlantDataRepository.getInstance().getAll().stream()
                .map(definition -> definition.name).toArray(String[]::new);
    }
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
