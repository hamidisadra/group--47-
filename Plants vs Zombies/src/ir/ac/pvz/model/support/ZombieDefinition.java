package ir.ac.pvz.model.support;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class ZombieDefinition {
    public String alias;
    public List<String> aliases;
    public String gameType;
    public String runtimeType;
    public float speed;
    public int health;
    public int eatDamagePerSecond;
    public int waveCost;
    public int weight;
    public boolean canSpawnPlantFood;
    public Map<String, Double> numericProperties;
    public List<String> abilities;
    public List<String> armorAliases;
    public ZombieDefinition() {
        numericProperties = new LinkedHashMap<>();
        aliases = new ArrayList<>();
        abilities = new ArrayList<>();
        armorAliases = new ArrayList<>();
    }
    public double getNumber(String key, double defaultValue) {
        return numericProperties.getOrDefault(key, defaultValue);
    }
    public Map<String, Double> getNumericProperties() {
        return Collections.unmodifiableMap(numericProperties);
    }
    public List<String> getAbilities() {
        return Collections.unmodifiableList(abilities);
    }
    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }
    public List<String> getArmorAliases() {
        return Collections.unmodifiableList(armorAliases);
    }
}
