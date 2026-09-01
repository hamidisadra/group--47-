package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.managers.GameSettings;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.others.StageConfig;
import ir.ac.pvz.model.stage.BossStage;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.user.User;

import java.util.ArrayList;
import java.util.List;

public final class StageConfigFactory {
    private static final int EASY_COST_UNIT = 125;
    private static final int NORMAL_COST_UNIT = 100;
    private static final int HARD_COST_UNIT = 50;
    private static final int MINIMUM_WAVE_COST = 1000;
    private static final int TARGET_WAVE_STEP = 600;
    private static final int MAX_TIER = 8;
    private static final SeasonType[] SEASON_ORDER = {
        SeasonType.ANCIENT_EGYPT, SeasonType.FROSTBITE_CAVES,
        SeasonType.BIG_WAVE_BEACH, SeasonType.DARK_AGES
    };

    private final SeasonType season;
    private final Stage stage;
    private final String minigame;
    private final List<String> selectedPlants;
    private final List<String> boostedThisStage;
    private final Long scoredSeed;

    public StageConfigFactory(SeasonType season, Stage stage, String minigame,
                              List<String> selectedPlants,
                              List<String> boostedThisStage, Long scoredSeed) {
        this.season = season;
        this.stage = stage;
        this.minigame = minigame;
        this.selectedPlants = selectedPlants;
        this.boostedThisStage = boostedThisStage;
        this.scoredSeed = scoredSeed;
    }

    public static int activeDifficulty() {
        User user = MenuManager.getInstance().getActiveUser();
        if (user != null) {
            return user.getDifficultyLevel();
        }

        return GameSettings.getInstance().getDifficulty();
    }

    static int waveCostUnit(int difficulty) {
        if (difficulty < 3) {
            return EASY_COST_UNIT;
        }

        if (difficulty > 3) {
            return HARD_COST_UNIT;
        }

        return NORMAL_COST_UNIT;
    }

    public StageConfig build() {
        int waves = stage == null ? 3 : Math.max(2, stage.getWaveCount());
        int difficulty = activeDifficulty();
        int baseCost = MINIMUM_WAVE_COST
            + (stage == null ? 0 : (stage.getDifficulty() - 1) * 250);

        StageConfig config = createConfig(waves, baseCost);
        config.setDifficultyLevel(difficulty);
        config.setExplicitWaveCosts(buildWaveCosts(waves, baseCost, difficulty));

        if (selectedPlants.isEmpty()) {
            selectedPlants.addAll(defaultPlantPool());
        }

        config.setSelectedPlantTypes(selectedPlants.toArray(new String[0]));

        if (scoredSeed != null) {
            config.setRandomSeed(scoredSeed);
        }

        applyUserPreferences(config);
        return config;
    }

    private StageConfig createConfig(int waves, int baseCost) {
        if ("Zombotany".equals(minigame)) {
            return StageConfig.of(season, waves, baseCost, "BasicZombie",
                "PeashooterZombie", "WallnutZombie", "SquashZombie", "JalapenoZombie");
        }

        return StageConfig.of(season, waves, baseCost,
            stageZombiePool().toArray(new String[0]));
    }

    private int[] buildWaveCosts(int waves, int baseCost, int difficulty) {
        int unit = waveCostUnit(difficulty);
        int step = unit * Math.max(5, (int) Math.ceil(TARGET_WAVE_STEP / (double) unit));
        int[] costs = new int[waves];
        costs[0] = snapToUnit(baseCost, unit);

        for (int index = 1; index < waves - 1; index++) {
            int previous = costs[index - 1];
            int required = Math.round(previous * (1f + StageConfig.WAVE_GROWTH_RATE));
            costs[index] = Math.max(snapToUnit(previous + step, unit), required);
        }

        costs[waves - 1] = costs[waves - 2] * 2;
        return costs;
    }

    private static int snapToUnit(int value, int unit) {
        int snapped = Math.round(value / (float) unit) * unit;
        return Math.max(MINIMUM_WAVE_COST, snapped);
    }

    private void applyUserPreferences(StageConfig config) {
        User user = MenuManager.getInstance().getActiveUser();
        if (user == null) {
            return;
        }

        for (String plant : selectedPlants) {
            config.setPlantLevel(plant, user.getCollection().getPlantLevel(plant));
        }

        List<String> boosted = new ArrayList<>();
        for (String plant : user.getCollection().getEffectiveBoostedPlants()) {
            if (selectedPlants.contains(plant)) {
                boosted.add(plant);
            }
        }

        boostedThisStage.clear();
        boostedThisStage.addAll(boosted);

        if (!boosted.isEmpty()) {
            config.setBoostedPlantTypes(boosted.toArray(new String[0]));
        }

        applyImitaterTarget(config);
    }

    private void applyImitaterTarget(StageConfig config) {
        if (!selectedPlants.contains("Imitater")) {
            return;
        }

        String target = imitaterTargetFor();
        if (target != null) {
            config.setImitaterTargetType(target);
            return;
        }

        selectedPlants.remove("Imitater");
        config.setSelectedPlantTypes(selectedPlants.toArray(new String[0]));
    }

    private String imitaterTargetFor() {
        for (String plant : selectedPlants) {
            if (!plant.equalsIgnoreCase("Imitater")) {
                return plant;
            }
        }

        return null;
    }

    private List<String> stageZombiePool() {
        if (minigame != null) {
            return StageConfig.defaultZombiePool(season);
        }

        List<String> pool = new ArrayList<>();
        pool.add("BasicZombie");
        List<String> local = seasonZombies();
        addTieredZombies(pool, local, progressionTier());
        return pool;
    }

    private int progressionTier() {
        if (stage == null) {
            return 4;
        }

        int number = Math.max(1, stage.getNumber());
        int perChapter = CampaignController.STAGES_PER_CHAPTER;
        int position = chapterIndexOf(season) * perChapter + number;
        int total = SEASON_ORDER.length * perChapter;
        int tier = (int) Math.ceil(position * (double) MAX_TIER / total);

        if (stage instanceof BossStage) {
            tier++;
        }

        return Math.max(1, Math.min(MAX_TIER, tier));
    }

    private static int chapterIndexOf(SeasonType season) {
        for (int index = 0; index < SEASON_ORDER.length; index++) {
            if (SEASON_ORDER[index] == season) {
                return index;
            }
        }

        return 0;
    }

    private void addTieredZombies(List<String> pool, List<String> local, int number) {
        if (number >= 2) {
            pool.add("ConeheadZombie");
        }

        if (number >= 3 && !local.isEmpty()) {
            pool.add(local.get(0));
        }

        if (number >= 4) {
            pool.add("BucketheadZombie");
        }

        if (number >= 5 && local.size() > 1) {
            pool.add(local.get(1));
        }

        if (number >= 6) {
            pool.add("NewspaperZombie");
            if (local.size() > 2) {
                pool.add(local.get(2));
            }
        }

        addLateTierZombies(pool, local, number);
    }

    private void addLateTierZombies(List<String> pool, List<String> local, int number) {
        if (number >= 7) {
            pool.add("BlockheadZombie");
            pool.add("KnightZombie");
            if (local.size() > 3) {
                pool.add(local.get(3));
            }
        }

        if (number >= 8) {
            pool.add("FootballZombie");
            pool.add("ImpZombie");
            pool.add("Gargantuar");
        }
    }

    private List<String> seasonZombies() {
        List<String> all = StageConfig.defaultZombiePool(season);
        List<String> common = StageConfig.commonZombiePool();
        List<String> local = new ArrayList<>();

        for (String type : all) {
            if (!common.contains(type)) {
                local.add(type);
            }
        }

        return local;
    }

    private List<String> defaultPlantPool() {
        List<String> pool = new ArrayList<>();
        if ("Wall-nut Bowling".equals(minigame)) {
            pool.add("Wall-nut");
            pool.add("Explode-o-nut");
            pool.add("Tall-nut");
            return pool;
        }

        User user = MenuManager.getInstance().getActiveUser();
        if (user != null) {
            pool.addAll(user.getCollection().getUnlockedPlants());
        }

        if (pool.isEmpty()) {
            pool.add("Peashooter");
            pool.add("Sunflower");
            pool.add("Wall-nut");
        }

        return pool;
    }
}
