package com.pvz.game;

import com.pvz.game.SpecialSpawnEvent;
import com.pvz.model.enums.SeasonType;
import com.pvz.model.support.Board;
import com.pvz.model.support.GridPosition;
import com.pvz.model.support.BalanceDefaults;
import com.pvz.model.support.ZombieBaseStats;
import com.pvz.model.support.PlantDataRepository;
import com.pvz.model.support.ZombieDataRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StageConfig {
    public SeasonType seasonType;
    public int totalWaves;
    public int baseWaveCost;
    public float waveGrowthRate;
    public float finalWaveMultiplier;
    public List<String> allowedZombieTypes;
    public Map<String, Float> zombieAbilityCooldowns;
    public Map<String, Integer> zombieAbilityValues;
    public Map<String, ZombieBaseStats> zombieBaseStats;
    public List<String> selectedPlantTypes;
    public List<String> boostedPlantTypes;
    public List<Integer> explicitWaveCosts;
    public Map<String, Integer> plantLevels;
    public List<SpecialSpawnEvent> specialSpawnEvents;
    public String imitaterTargetType;
    public boolean sandboxMode;
    public StageConfig(SeasonType seasonType, int totalWaves, int baseWaveCost,
                       float waveGrowthRate, float finalWaveMultiplier,
                       List<String> allowedZombieTypes) {
        this.seasonType = seasonType;
        this.totalWaves = Math.max(0, totalWaves);
        this.baseWaveCost = Math.max(0, baseWaveCost);
        this.waveGrowthRate = waveGrowthRate;
        this.finalWaveMultiplier = finalWaveMultiplier;
        if (allowedZombieTypes == null) {
            this.allowedZombieTypes = new ArrayList<>();
        } else {
            this.allowedZombieTypes = new ArrayList<>(allowedZombieTypes);
        }
        this.zombieAbilityCooldowns = defaultAbilityCooldowns();
        this.zombieAbilityValues = defaultAbilityValues();
        this.zombieBaseStats = defaultZombieBaseStats();
        this.selectedPlantTypes = new ArrayList<>();
        this.boostedPlantTypes = new ArrayList<>();
        this.explicitWaveCosts = new ArrayList<>();
        this.plantLevels = new LinkedHashMap<>();
        this.specialSpawnEvents = new ArrayList<>();
        this.imitaterTargetType = null;
        this.sandboxMode = false;
    }
    public static StageConfig unconfigured(SeasonType seasonType) {
        return new StageConfig(seasonType, 0, 0, 0.25f, 2f,
                new ArrayList<>());
    }
    public static StageConfig openEnded(SeasonType seasonType) {
        StageConfig config = unconfigured(seasonType);
        config.sandboxMode = true;
        config.selectAllPlants();
        return config;
    }
    public static StageConfig defaultPlayable(SeasonType seasonType) {
        return unconfigured(seasonType);
    }
    public static StageConfig of(SeasonType seasonType, int totalWaves,
                                 int baseWaveCost, String... zombieTypes) {
        List<String> pool;
        if (zombieTypes == null || zombieTypes.length == 0) {
            pool = defaultZombiePool(seasonType);
        }
        else {
            pool = Arrays.asList(zombieTypes);
        }
        return new StageConfig(seasonType, totalWaves,
                baseWaveCost, 0.25f, 2f, pool);
    }
    public static List<String> defaultZombiePool(SeasonType seasonType) {
        List<String> types = new ArrayList<>(commonZombiePool());
        if (seasonType == SeasonType.ANCIENT_EGYPT) {
            Collections.addAll(types, "RaZombie", "ExplorerZombie",
                    "TombRaiserZombie");
        }
        else if (seasonType == SeasonType.FROSTBITE_CAVES) {
            Collections.addAll(types, "DodoRiderZombie", "HunterZombie",
                    "Troglobite");
        }
        else if (seasonType == SeasonType.BIG_WAVE_BEACH) {
            Collections.addAll(types, "FishermanZombie", "SnorkelZombie",
                    "OctopusZombie");
        }
        else if (seasonType == SeasonType.DARK_AGES) {
            Collections.addAll(types, "JesterZombie", "WizardZombie",
                    "KingZombie", "ImpDragon");
        }
        return types;
    }
    public StageConfig addSpecialSpawnEvent(int tick, String zombieType,
                                            GridPosition position) {
        if (tick >= 0 && zombieType != null && position != null) {
            specialSpawnEvents.add(new SpecialSpawnEvent(tick, zombieType,
                    new GridPosition(position.x, position.y)));
        }
        return this;
    }
    public StageConfig setImitaterTargetType(String plantType) {
        this.imitaterTargetType = plantType;
        return this;
    }
    public StageConfig setSelectedPlantTypes(String... plantTypes) {
        selectedPlantTypes.clear();
        if (plantTypes != null) {
            selectedPlantTypes.addAll(Arrays.asList(plantTypes));
        }
        return this;
    }
    public StageConfig setBoostedPlantTypes(String... plantTypes) {
        boostedPlantTypes.clear();
        if (plantTypes != null) {
            boostedPlantTypes.addAll(Arrays.asList(plantTypes));
        }
        return this;
    }
    public StageConfig setPlantLevel(String plantType, int level) {
        if (plantType != null && level >= 1 && level <= 4) {
            plantLevels.put(normalize(plantType), level);
        }
        return this;
    }
    public int getPlantLevel(String plantType) {
        return plantLevels.getOrDefault(normalize(plantType), 1);
    }
    public StageConfig setExplicitWaveCosts(int... waveCosts) {
        explicitWaveCosts.clear();
        if (waveCosts != null) {
            for (int waveCost : waveCosts) {
                explicitWaveCosts.add(waveCost);
            }
        }
        return this;
    }
    public boolean isPlantBoosted(String plantType) {
        String normalized = normalize(plantType);
        return boostedPlantTypes.stream().anyMatch(type ->
                normalize(type).equals(normalized));
    }
    public boolean isPlantSelected(String plantType) {
        String normalized = normalize(plantType);
        return selectedPlantTypes.stream().anyMatch(type ->
                normalize(type).equals(normalized));
    }
    public StageConfig setZombieBaseStats(String zombieType, float speed,
                                          int health, int eatDps,
                                          int waveCost, int accessoryHealth) {
        ZombieBaseStats stats = new ZombieBaseStats(speed, health, eatDps,
                waveCost, accessoryHealth);
        if (zombieType != null && stats.isComplete()) {
            zombieBaseStats.put(normalize(zombieType), stats);
        }
        return this;
    }
    public ZombieBaseStats getZombieBaseStats(String zombieType) {
        return zombieBaseStats.get(normalize(zombieType));
    }
    public StageConfig setZombieAbilityValue(String key, int value) {
        if (key != null && value >= 0) {
            zombieAbilityValues.put(normalize(key), value);
        }
        return this;
    }
    public int getZombieAbilityValue(String key) {
        return zombieAbilityValues.getOrDefault(normalize(key), 0);
    }
    public StageConfig setZombieAbilityCooldown(String zombieType,
                                                float seconds) {
        if (zombieType != null && seconds > 0f) {
            zombieAbilityCooldowns.put(normalize(zombieType), seconds);
        }
        return this;
    }
    public float getZombieAbilityCooldown(String zombieType) {
        return zombieAbilityCooldowns.getOrDefault(normalize(zombieType), 0f);
    }
    public List<Integer> calculateWaveCosts() {
        if (!explicitWaveCosts.isEmpty()) {
            return new ArrayList<>(explicitWaveCosts);
        }
        List<Integer> costs = new ArrayList<>();
        if (totalWaves <= 0) {
            return costs;
        }
        int firstCost = Math.max(1000, baseWaveCost);
        costs.add(firstCost);
        if (totalWaves == 1) {
            return costs;
        }
        for (int number = 2; number < totalWaves; number++) {
            int previous = costs.get(costs.size() - 1);
            int percentageCost = Math.round(previous
                    * (1f + Math.max(0.25f, waveGrowthRate)));
            costs.add(Math.max(percentageCost, previous + 500));
        }
        int previousCost = baseWaveCost;
        if (!costs.isEmpty()) {
            previousCost = costs.get(costs.size() - 1);
        }
        // Page 25: data-driven stage values retain the mandatory flag minimum.
        costs.add(Math.round(previousCost * Math.max(2f,
                finalWaveMultiplier)));
        return costs;
    }
    public void validateForStart(Board board, ZombieSpawner spawner) {
        List<String> errors = new ArrayList<>();
        validateGeneralData(board, errors);
        validatePlantData(errors);
        validateSpecialSpawnEvents(board, spawner, errors);
        if (!sandboxMode) {
            validateZombieData(spawner, errors);
            validateWaveData(spawner, errors);
        }
        if (!errors.isEmpty()) {
            throw new StageConfigurationException(String.join(
                    System.lineSeparator(), errors));
        }
    }
    private void validateGeneralData(Board board, List<String> errors) {
        if (board == null) {
            errors.add("Board is required.");
            return;
        }
        if (seasonType == null || board.seasonType != seasonType) {
            errors.add("Stage season must match board season.");
        }
        if (sandboxMode) {
            return;
        }
        if (totalWaves < 2) {
            errors.add("A stage requires at least one normal wave and one final wave.");
        }
        if (waveGrowthRate < 0f) {
            errors.add("Wave growth rate cannot be negative.");
        }
        if (finalWaveMultiplier < 2f) {
            errors.add("Final wave multiplier must be at least 2.");
        }
        if (baseWaveCost < 1000 && explicitWaveCosts.isEmpty()) {
            errors.add("The first wave cost must be at least 1000.");
        }
        if (!explicitWaveCosts.isEmpty()
                && explicitWaveCosts.size() != totalWaves) {
            errors.add("Explicit wave costs must contain one value per wave.");
        }
        validateExplicitWaveProgression(errors);
        if (allowedZombieTypes.isEmpty()) {
            errors.add("At least one allowed zombie type is required.");
        }
    }
    private void validateExplicitWaveProgression(List<String> errors) {
        if (explicitWaveCosts.isEmpty()) {
            return;
        }
        if (explicitWaveCosts.get(0) < 1000) {
            errors.add("The first explicit wave cost must be at least 1000.");
        }
        for (int index = 1; index < explicitWaveCosts.size(); index++) {
            int previous = explicitWaveCosts.get(index - 1);
            int current = explicitWaveCosts.get(index);
            if (current < previous + 500) {
                errors.add("Each explicit wave must cost at least 500 more "
                        + "than the previous wave.");
                break;
            }
        }
        if (explicitWaveCosts.size() >= 2) {
            int previous = explicitWaveCosts.get(explicitWaveCosts.size() - 2);
            int last = explicitWaveCosts.get(explicitWaveCosts.size() - 1);
            if (last != Math.round(previous * Math.max(2f,
                    finalWaveMultiplier))) {
                errors.add("The final explicit wave cost must equal the final "
                        + "wave multiplier applied to the previous wave.");
            }
        }
    }
    private void validatePlantData(List<String> errors) {
        PlantDataRepository plants = PlantDataRepository.getInstance();
        for (String plantType : selectedPlantTypes) {
            if (plants.get(plantType) == null) {
                errors.add("Unknown selected plant type: " + plantType);
            }
        }
        for (String plantType : boostedPlantTypes) {
            if (plants.get(plantType) == null) {
                errors.add("Unknown boosted plant type: " + plantType);
            } else if (!isPlantSelected(plantType)) {
                errors.add("Boosted plant must also be selected: " + plantType);
            }
        }
        if (!sandboxMode && selectedPlantTypes.isEmpty()) {
            errors.add("At least one selected plant type is required.");
        }
        if (imitaterTargetType != null
                && plants.get(imitaterTargetType) == null) {
            errors.add("Unknown Imitater target: " + imitaterTargetType);
        }
    }
    private void validateSpecialSpawnEvents(Board board,
                                            ZombieSpawner spawner,
                                            List<String> errors) {
        if (board == null || spawner == null) {
            return;
        }
        for (SpecialSpawnEvent event : specialSpawnEvents) {
            if (event.tick < 0 || !board.isInside(event.position)) {
                errors.add("Invalid special spawn position or tick.");
                continue;
            }
            com.pvz.model.support.Tile tile = board.getTile(event.position);
            if (tile == null || !tile.isLowTideSpawn) {
                errors.add("Special spawn must target LOW_TIDE or NECROMANCY.");
            }
            if (!spawner.isSupportedType(event.zombieType)) {
                errors.add("Unsupported special-spawn zombie: "
                        + event.zombieType);
            }
        }
    }
    private void validateZombieData(ZombieSpawner spawner,
                                    List<String> errors) {
        if (spawner == null) {
            errors.add("Zombie spawner is required.");
            return;
        }
        for (String type : allowedZombieTypes) {
            if (!spawner.isSupportedType(type)) {
                errors.add("Unsupported zombie type: " + type);
            } else if (!spawner.hasRequiredBaseData(type)) {
                errors.add("Missing base data for zombie type: " + type);
            }
        }
    }
    private void validateWaveData(ZombieSpawner spawner,
                                  List<String> errors) {
        if (sandboxMode || totalWaves <= 0 || spawner == null) {
            return;
        }
        List<Integer> costs = calculateWaveCosts();
        for (int index = 0; index < costs.size(); index++) {
            int cost = costs.get(index);
            if (cost <= 0) {
                errors.add("Wave " + (index + 1) + " cost must be positive.");
            } else if (!spawner.canBuildExactCost(cost)) {
                errors.add("Wave " + (index + 1) + " cost " + cost
                        + " cannot be built exactly from allowed zombies.");
            }
        }
    }
    private void selectAllPlants() {
        selectedPlantTypes.clear();
        for (com.pvz.model.support.PlantDefinition definition
                : PlantDataRepository.getInstance().getAll()) {
            selectedPlantTypes.add(definition.name);
        }
    }
    private boolean containsZombie(String zombieType) {
        String normalized = normalize(zombieType);
        return allowedZombieTypes.stream().anyMatch(type ->
                normalize(type).equals(normalized));
    }
    private Map<String, Float> defaultAbilityCooldowns() {
        ZombieDataRepository data = ZombieDataRepository.getInstance();
        Map<String, Float> cooldowns = new LinkedHashMap<>();
        cooldowns.put("tombraiserzombie", (float) data.getNumber(
                "TombRaiserZombie", "TimeBetweenRaisings", 6d));
        cooldowns.put("fishermanzombie", (float) data.getNumber(
                "FishermanZombie", "DelayBetweenCasting", 2.5d));
        cooldowns.put("kingzombie", (float) data.getNumber(
                "KingZombie", "DelayBetweenKnightings", 2.5d));
        cooldowns.put("pianistzombie",
                BalanceDefaults.PIANIST_MUSIC_LOOP_SECONDS);
        return cooldowns;
    }
    private Map<String, Integer> defaultAbilityValues() {
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("huntericehealth", BalanceDefaults.HUNTER_ICE_HEALTH);
        values.put("octopusblockhealth", BalanceDefaults.OCTOPUS_BLOCK_HEALTH);
        return values;
    }
    private Map<String, ZombieBaseStats> defaultZombieBaseStats() {
        Map<String, ZombieBaseStats> stats = new LinkedHashMap<>();
        stats.put("barrelrollerzombie", new ZombieBaseStats(
                BalanceDefaults.BARREL_ROLLER_SPEED,
                BalanceDefaults.BARREL_ROLLER_HEALTH,
                BalanceDefaults.BARREL_ROLLER_EAT_DPS,
                BalanceDefaults.BARREL_ROLLER_WAVE_COST,
                BalanceDefaults.BARREL_HEALTH));
        return stats;
    }
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }

    private static List<String> commonZombiePool() {
        return new ArrayList<>(Arrays.asList(
                "BasicZombie", "ConeheadZombie", "BucketheadZombie",
                "KnightZombie", "BlockheadZombie", "Gargantuar",
                "ImpZombie", "FootballZombie", "ArcadeZombie",
                "ParasolZombie", "TurquoiseZombie", "ProspectorZombie",
                "PianistZombie", "NewspaperZombie"));
    }
}
