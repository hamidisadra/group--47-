package ir.ac.pvz.model.others;

import ir.ac.pvz.model.support.ShotEvent;

import ir.ac.pvz.controller.game_core.*;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.*;
import ir.ac.pvz.model.interfaces.IAttacker;
import ir.ac.pvz.model.interfaces.UpgradeCostProvider;
import ir.ac.pvz.model.interfaces.UpgradeResourceWallet;
import ir.ac.pvz.model.plants.*;
import ir.ac.pvz.model.support.*;
import ir.ac.pvz.model.zombies.*;
import java.util.*;
public class GameSession {
    public GameStatus status;
    public int currentSunAmount;
    public int plantFoodCount;
    public int currentWaveNumber;
    private final Board board;
    private final SunManager sunManager;
    private final PlantFoodInventory plantFoodInventory;
    private final WaveController waveController;
    private final TickClock clock;
    private final ZombieSpawner zombieSpawner;
    private final LootDropService lootDropService;
    private final ProjectileResolver projectileResolver;
    private final ZombieBehaviorController zombieBehaviorController;
    private final GameTickProcessor tickProcessor;
    private final StageConfig stageConfig;
    private final GameStatistics statistics;
    private final Map<String, Float> cooldowns;
    private boolean cooldownDisabled;
    private int nextPlantId;
    private int coins;
    private int diamonds;
    private int pots;
    private GameOutcomeListener outcomeListener;
    private boolean outcomeNotified;
    private float simulationTickAccumulator;
    public GameSession(Board board, int startingSun) {
        this(board, startingSun, StageConfig.unconfigured(board.seasonType));
    }

    @SuppressWarnings("this-escape")
    public GameSession(Board board, int startingSun, StageConfig stageConfig) {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null.");
        }

        this.board = board;
        this.stageConfig = stageConfig == null
            ? StageConfig.unconfigured(board.seasonType) : stageConfig;
        this.status = GameStatus.PLANT_SELECTION_READY;
        this.currentSunAmount = startingSun;
        this.plantFoodCount = 0;
        this.currentWaveNumber = 0;
        this.sunManager = createSunManager(board, startingSun);
        this.plantFoodInventory = new PlantFoodInventory(3);
        this.clock = new TickClock(10);
        this.zombieSpawner = new ZombieSpawner(board, this.stageConfig,
            randomFor(this.stageConfig, 23L));
        this.waveController = createWaveController();
        this.lootDropService = new LootDropService(
            randomFor(this.stageConfig, 37L));
        this.projectileResolver = new ProjectileResolver(
            randomFor(this.stageConfig, 41L),
            zombieSpawner::prepareSpawnedZombie);
        this.zombieBehaviorController = new ZombieBehaviorController(
            randomFor(this.stageConfig, 53L),
            ZombieDataRepository.getInstance());
        this.statistics = new GameStatistics();
        this.cooldowns = new LinkedHashMap<>();
        this.tickProcessor = new GameTickProcessor(this, cooldowns,
            projectileResolver, zombieBehaviorController, lootDropService);
        resetRuntimeState();
        board.setObstacleListener(this::onObstacleCleared);
    }

    private SunManager createSunManager(Board board, int startingSun) {
        float difficultyMultiplier =
            this.stageConfig.getDifficultyIncreaseMultiplier();
        SunManager manager = new SunManager(startingSun, board,
            randomFor(this.stageConfig, 11L));
        manager.setSkySunDropIntervalMultiplier(difficultyMultiplier);
        return manager;
    }

    private WaveController createWaveController() {
        return new WaveController(this.stageConfig.baseWaveCost,
            this.stageConfig.waveGrowthRate,
            this.stageConfig.finalWaveMultiplier,
            this.stageConfig.totalWaves,
            this.stageConfig.explicitWaveCosts, zombieSpawner);
    }

    private void resetRuntimeState() {
        this.cooldownDisabled = false;
        this.nextPlantId = 1;
        this.coins = 0;
        this.diamonds = 0;
        this.pots = 0;
        this.outcomeListener = null;
        this.outcomeNotified = false;
        this.simulationTickAccumulator = 0f;
    }

    private void onObstacleCleared(Tile tile, TileObstacle obstacle) {
        if (!(obstacle instanceof Tombstone) || tile == null) {
            return;
        }

        Tombstone grave = (Tombstone) obstacle;
        if (grave.getStoredSun() > 0) {
            sunManager.dropGroundSun(tile.getPosition(), grave.getStoredSun());
        }

        if (grave.hasPlantFood()) {
            plantFoodInventory.cheatAddPlantFood();
            synchronizePublicState();
        }
    }

    boolean isCooldownDisabled() {
        return cooldownDisabled;
    }

    public void start() {
        if (status == GameStatus.RUNNING
            || status == GameStatus.WON || status == GameStatus.LOST) {
            return;
        }

        stageConfig.validateForStart(board, zombieSpawner);
        status = GameStatus.RUNNING;
        waveController.startNextWaveIfReady();
        statistics.recordFirstWaveStart(clock.currentTick);
        synchronizePublicState();
    }

    public void advanceTime(int count) {
        if (status != GameStatus.RUNNING || count <= 0) {
            return;
        }

        for (int tick = 0; tick < count && status == GameStatus.RUNNING; tick++) {
            simulationTickAccumulator +=
                stageConfig.getDifficultyIncreaseMultiplier();
            processSimulationTicks();
            synchronizePublicState();
        }
    }

    private void processSimulationTicks() {
        while (simulationTickAccumulator + 0.0001f >= 1f
            && status == GameStatus.RUNNING) {
            clock.advance(1);
            tickProcessor.updateOneTick();
            simulationTickAccumulator -= 1f;
        }
    }

    public boolean collectSun(GridPosition position) {
        int before = sunManager.currentSunAmount;
        boolean collected = sunManager.collectSun(position);
        if (collected) {
            statistics.recordSunCollected(sunManager.currentSunAmount - before);
            tickProcessor.reconcileExternalStateChange();
        }

        synchronizePublicState();
        return collected;
    }

    public boolean plantPlant(String type, GridPosition position) {
        String cardType = normalize(type);
        Plant plant = PlantCardFactory.createPlantForCard(stageConfig, type,
            nextPlantId);
        if (PlantingRules.describe(this, type, position, plant) != null) {
            return false;
        }

        Tile tile = board.getTile(position);
        if (PeaPodStacking.merge(tile, plant)) {
            finishPeaPodMerge(tile, plant, cardType);
            return true;
        }

        if (!tile.addPlant(plant)) {
            return false;
        }

        nextPlantId++;
        finishPlanting(plant, cardType);
        applyImitaterEntranceFood(cardType, plant);
        PlantEntranceEffects.resolve(this, plant);
        tickProcessor.reconcileExternalStateChange();
        synchronizePublicState();
        return true;
    }

    private void finishPeaPodMerge(Tile tile, Plant plant, String cardType) {
        sunManager.spendSuns(plant.sunCost);
        if (!cooldownDisabled) {
            cooldowns.put(cardType, plant.rechargeTime);
        }

        Plant mergedPlant = tile.getPlant();
        statistics.recordPlantPlaced(mergedPlant);
        if (stageConfig.isPlantBoosted(cardType)) {
            plantFoodInventory.boostPlant(mergedPlant, this, projectileResolver);
        }

        tickProcessor.reconcileExternalStateChange();
        synchronizePublicState();
    }

    public String getPlantingError(String type, GridPosition position) {
        return PlantingRules.describe(this, type, position,
            PlantCardFactory.createPlantForCard(stageConfig, type, 0));
    }

    private void applyImitaterEntranceFood(String cardType, Plant plant) {
        if (cardType.equals("imitater")
            && stageConfig.getPlantLevel("Imitater") >= 4) {
            plantFoodInventory.boostPlant(plant, this, projectileResolver);
        }
    }

    private void finishPlanting(Plant plant, String cardType) {
        sunManager.spendSuns(plant.sunCost);
        if (!cooldownDisabled) {
            cooldowns.put(cardType, plant.rechargeTime);
        }

        statistics.recordPlantPlaced(plant);
        tickProcessor.registerPlant(plant);
        if (stageConfig.isPlantBoosted(cardType)) {
            plantFoodInventory.boostPlant(plant, this, projectileResolver);
        }
    }

    boolean isPeaPodAtMaximum(Tile tile, Plant plant) {
        return PeaPodStacking.isAtMaximum(tile, plant);
    }

    public Plant pluckPlant(GridPosition position) {
        Tile tile = board.getTile(position);
        if (tile == null) {
            return null;
        }

        Plant plant = tile.removePlant();
        tickProcessor.forgetPlant(plant);
        return plant;
    }

    public boolean feedPlant(GridPosition position) {
        Tile tile = board.getTile(position);
        if (tile == null || tile.getPlant() == null || plantFoodInventory.count <= 0) {
            return false;
        }

        boolean fed = plantFoodInventory.feedPlant(tile.getPlant(), this,
            projectileResolver);
        if (fed) {
            tickProcessor.reconcileExternalStateChange();
        }

        synchronizePublicState();
        return fed;
    }

    public void launchGrapeshot(Plant source) {
        tickProcessor.launchGrapeshot(source);
    }

    public void releaseNuke() {
        for (Zombie zombie : board.getAllAliveZombies()) {
            zombie.forceDie();
        }

        tickProcessor.reconcileExternalStateChange();
        synchronizePublicState();
    }

    public void win() {
        if (status == GameStatus.WON || status == GameStatus.LOST) {
            return;
        }

        status = GameStatus.WON;
        System.out.println("Dear humanz, zis is not done yet; we will come back "
            + "to eat your brainz, humanz.");
        notifyOutcome();
    }

    public void lose() {
        if (status == GameStatus.WON || status == GameStatus.LOST) {
            return;
        }

        status = GameStatus.LOST;
        System.out.println("The zombie ate your brain; LOSER!!!");
        notifyOutcome();
    }

    public Plant findPlantTarget(Zombie zombie) {
        return PlantTargetFinder.findPlantTarget(board, zombie);
    }

    public Plant findNearestPlantAhead(Zombie zombie) {
        return PlantTargetFinder.findNearestPlantAhead(board, zombie);
    }

    public int stealSuns(int requested) {
        int stolen = Math.min(Math.max(0, requested), sunManager.currentSunAmount);
        sunManager.currentSunAmount -= stolen;
        synchronizePublicState();
        return stolen;
    }

    public void cheatRemoveCooldown() {
        cooldownDisabled = true;
        cooldowns.replaceAll((key, value) -> 0f);
    }

    public Zombie spawnZombieFromSpecialTile(String type, GridPosition position) {
        Tile tile = board.getTile(position);
        if (tile == null || !tile.isLowTideSpawn) {
            return null;
        }

        return zombieSpawner.spawnZombie(type, new ContinuousPosition(
            position.x, position.y));
    }

    public UpgradeResult upgradePlant(Plant plant,
                                      UpgradeResourceWallet wallet,
                                      UpgradeCostProvider costProvider) {
        return new PlantUpgradeService().upgrade(plant, wallet, costProvider);
    }

    public UpgradeResult upgradePlant(Plant plant,
                                      UpgradeResourceWallet wallet) {
        return new PlantUpgradeService().upgrade(plant, wallet);
    }

    public Zombie cheatSpawnZombie(String type, int x, int y) {
        return zombieSpawner.spawnZombie(type, new ContinuousPosition(x, y));
    }

    public Zombie spawnConfiguredZombie(String type,
                                        ContinuousPosition position) {
        if (type == null || position == null) {
            return null;
        }

        return zombieSpawner.spawnZombie(type, position);
    }

    public void prepareSpawnedZombie(Zombie zombie) {
        zombieSpawner.prepareSpawnedZombie(zombie);
    }

    public List<Plant> getPlantCatalog() {
        List<Plant> plants = new ArrayList<>();

        for (String type : Plant.getSpreadsheetTypes()) {
            if (!stageConfig.isPlantSelected(type)) {
                continue;
            }

            Plant plant = PlantCardFactory.createPlant(stageConfig, type, 0);

            if (plant != null) {
                plants.add(plant);
            }
        }

        return plants;
    }

    public float getCooldown(String type) {
        return cooldowns.getOrDefault(normalize(type), 0f);
    }

    ProjectileResolver getProjectileResolver() {
        return projectileResolver;
    }

    public Board getBoard() { return board; }
    public SunManager getSunManager() { return sunManager; }
    public PlantFoodInventory getPlantFoodInventory() { return plantFoodInventory; }
    public WaveController getWaveController() { return waveController; }
    public TickClock getClock() { return clock; }
    public StageConfig getStageConfig() { return stageConfig; }
    public GameStatistics getStatistics() { return statistics; }
    public List<LawnMower> getLawnMowers() {
        List<LawnMower> mowers = new ArrayList<>();
        for (int row = 0; row < board.rows; row++) {
            mowers.add(board.getLawnMower(row));
        }

        return mowers;
    }

    public void setOutcomeListener(GameOutcomeListener listener) {
        this.outcomeListener = listener;
    }

    private static Random randomFor(StageConfig config, long salt) {
        Long seed = config == null ? null : config.getRandomSeed();
        return seed == null ? new Random() : new Random(seed + salt);
    }

    private void notifyOutcome() {
        if (outcomeNotified || outcomeListener == null) {
            return;
        }

        outcomeNotified = true;
        if (status == GameStatus.WON) {
            outcomeListener.onGameWon(this);
        }

        else if (status == GameStatus.LOST) {
            outcomeListener.onGameLost(this);
        }
    }

    public int getCurrentSunAmount() { return sunManager.showSunAmount(); }
    public int getPlantFoodCount() { return plantFoodInventory.count; }
    public int getCurrentWaveNumber() { return waveController.currentWaveNumber; }
    public int getCoins() { return coins; }
    public int getDiamonds() { return diamonds; }
    public int getPots() { return pots; }
    public void addCoins(int amount) { coins = checkedResourceTotal(coins, amount); }
    public void addDiamonds(int amount) { diamonds = checkedResourceTotal(diamonds, amount); }
    public void addPots(int amount) { pots = checkedResourceTotal(pots, amount); }
    private int checkedResourceTotal(int current, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                "Resource amount cannot be negative.");
        }

        long result = (long) current + amount;

        if (result > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "Resource amount exceeds the supported limit.");
        }

        return (int) result;
    }

    private void synchronizePublicState() {
        currentSunAmount = sunManager.currentSunAmount;
        plantFoodCount = plantFoodInventory.count;
        currentWaveNumber = waveController.currentWaveNumber;
    }

    public void resetFamilyCooldown(PlantCategory category) {
        for (String type : Plant.getSpreadsheetTypes()) {
            Plant plant = Plant.createSpreadsheetPlant(0, type);
            if (plant != null && plant.category == category) {
                cooldowns.put(normalize(type), 0f);
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("-", "").replace("_", "")
            .replace(" ", "").toLowerCase();
    }

    public void refreshPublicState() {
        synchronizePublicState();
    }

    public void setPlantFoodListener(PlantFoodInventory.Listener listener) {
        plantFoodInventory.setListener(listener);
    }

    public void setLootListener(ir.ac.pvz.controller.game_core.LootDropService.Listener listener) {
        lootDropService.setListener(listener);
    }

    public void setShotListener(ShotEvent.Listener listener) {
        projectileResolver.setShotListener(listener);
    }
}
