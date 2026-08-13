package ir.ac.pvz.model.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ZombieAbilityRegistry {
    private static final Map<String, Function<ZombieDefinition, ZombieAbility>> FACTORIES =
            createFactories();
    private ZombieAbilityRegistry() {
    }
    public static ZombieAbility create(String name) {
        return create(name, null);
    }
    public static ZombieAbility create(String name, ZombieDefinition definition) {
        Function<ZombieDefinition, ZombieAbility> factory = FACTORIES.get(normalize(name));
        if (factory == null) {
            throw new IllegalArgumentException("Unknown zombie ability: " + name);
        }
        return factory.apply(definition);
    }
    public static boolean contains(String name) {
        return FACTORIES.containsKey(normalize(name));
    }
    private static Map<String, Function<ZombieDefinition, ZombieAbility>> createFactories() {
        Map<String, Function<ZombieDefinition, ZombieAbility>> factories = new LinkedHashMap<>();
        factories.put("fireimmunity", data -> new StandardZombieAbility(
                StandardZombieAbility.Kind.FIRE_IMMUNITY));
        factories.put("freezeimmunity", data -> new StandardZombieAbility(
                StandardZombieAbility.Kind.FREEZE_IMMUNITY));
        factories.put("lobbershield", data -> new StandardZombieAbility(
                StandardZombieAbility.Kind.LOBBER_SHIELD));
        factories.put("torch", data -> new TorchAbility());
        factories.put("newspaperenrage", data -> new NewspaperEnrageAbility(
                number(data, "EnragedSpeedScale", 4d),
                number(data, "EnragedDamageScale", 4d)));
        factories.put("dodoflight", data -> new DodoFlightAbility((int) number(
                data, "MaximumGridSquaresToFlyOver", 2d)));
        factories.put("snorkel", data -> new SnorkelAbility());
        return factories;
    }
    private static double number(ZombieDefinition definition, String key,
                                 double fallback) {
        if (definition == null) {
            return fallback;
        }
        return definition.getNumber(key, fallback);
    }
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
