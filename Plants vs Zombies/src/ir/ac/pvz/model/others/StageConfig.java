package ir.ac.pvz.model.others;

import ir.ac.pvz.controller.game_core.StageConfigurationException;
import ir.ac.pvz.controller.game_core.ZombieSpawner;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.BalanceDefaults;
import ir.ac.pvz.model.support.ZombieBaseStats;
import ir.ac.pvz.model.support.PlantDataRepository;
import ir.ac.pvz.model.support.*;

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
        this.allowedZombieTypes = allowedZombieTypes == null
                ? new ArrayList<>() : new ArrayList<>(allowedZombieTypes);
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
        return config;
    }

    public static StageConfig defaultPlayable(SeasonType seasonType) {
        return unconfigured(seasonType);
    }

    public static StageConfig of(SeasonType seasonType, int totalWaves,
                                 int baseWaveCost, String... zombieTypes) {
        List<String> pool = zombieTypes == null || zombieTypes.length == 0
                ? defaultZombiePool(seasonType) : Arrays.asList(zombieTypes);
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
        for (int number = 1; number < totalWaves; number++) {
            int cost = Math.round((float) (baseWaveCost
                    * Math.pow(1f + waveGrowthRate, number - 1)));
            costs.add(cost);
        }
        int previousCost = costs.isEmpty() ? baseWaveCost
                : costs.get(costs.size() - 1);
        costs.add(Math.round(previousCost * finalWaveMultiplier));
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
        if (finalWaveMultiplier <= 0f) {
            errors.add("Final wave multiplier must be positive.");
        }
        if (baseWaveCost <= 0 && explicitWaveCosts.isEmpty()) {
            errors.add("Base wave cost must be positive.");
        }
        if (!explicitWaveCosts.isEmpty()
                && explicitWaveCosts.size() != totalWaves) {
            errors.add("Explicit wave costs must contain one value per wave.");
        }
        if (allowedZombieTypes.isEmpty()) {
            errors.add("At least one allowed zombie type is required.");
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
            Tile tile = board.getTile(event.position);
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
        for (PlantDefinition definition
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
        return value == null ? "" : value.replace("-", "")
                .replace("_", "").replace(" ", "").toLowerCase();
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
