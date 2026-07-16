package com.pvz.game;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.*;
import com.pvz.model.interfaces.IAttacker;
import com.pvz.model.interfaces.UpgradeCostProvider;
import com.pvz.model.interfaces.UpgradeResourceWallet;
import com.pvz.model.plants.*;
import com.pvz.model.support.*;
import com.pvz.model.zombies.*;
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
    public GameSession(Board board, int startingSun) {
        this(board, startingSun, StageConfig.unconfigured(board.seasonType));
    }
    @SuppressWarnings("this-escape")
    public GameSession(Board board, int startingSun, StageConfig stageConfig) {
        this.board = board;
        this.stageConfig = stageConfig == null
                ? StageConfig.unconfigured(board.seasonType) : stageConfig;
        this.status = GameStatus.PLANT_SELECTION_READY;
        this.currentSunAmount = startingSun;
        this.plantFoodCount = 0;
        this.currentWaveNumber = 0;
        this.sunManager = new SunManager(startingSun, board);
        this.plantFoodInventory = new PlantFoodInventory(3);
        this.clock = new TickClock(10);
        this.zombieSpawner = new ZombieSpawner(board, this.stageConfig);
        this.waveController = new WaveController(this.stageConfig.baseWaveCost,
                this.stageConfig.waveGrowthRate,
                this.stageConfig.finalWaveMultiplier,
                this.stageConfig.totalWaves,
                this.stageConfig.explicitWaveCosts, zombieSpawner);
        this.lootDropService = new LootDropService();
        this.projectileResolver = new ProjectileResolver();
        this.zombieBehaviorController = new ZombieBehaviorController();
        this.statistics = new GameStatistics();
        this.cooldowns = new LinkedHashMap<>();
        this.cooldownDisabled = false;
        this.tickProcessor = new GameTickProcessor(this, cooldowns,
                projectileResolver, zombieBehaviorController, lootDropService);
        this.nextPlantId = 1;
        this.coins = 0;
        this.diamonds = 0;
        this.pots = 0;
        this.outcomeListener = null;
        this.outcomeNotified = false;
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
            clock.advance(1);
            tickProcessor.updateOneTick();
            synchronizePublicState();
        }
    }
    public boolean collectSun(GridPosition position) {
        int before = sunManager.currentSunAmount;
        boolean collected = sunManager.collectSun(position);
        if (collected) {
            statistics.recordSunCollected(sunManager.currentSunAmount - before);
        }
        synchronizePublicState();
        return collected;
    }
    public boolean plantPlant(String type, GridPosition position) {
        String cardType = normalize(type);
        Plant plant = createPlantForCard(type);
        if (getPlantingError(type, position, plant) != null) {
            return false;
        }
        Tile tile = board.getTile(position);
        if (mergePeaPod(tile, plant)) {
            sunManager.spendSuns(plant.sunCost);
            if (!cooldownDisabled) {
                cooldowns.put(cardType, plant.rechargeTime);
            }
            Plant mergedPlant = tile.getPlant();
            statistics.recordPlantPlaced(mergedPlant);
            if (stageConfig.isPlantBoosted(cardType)) {
                plantFoodInventory.boostPlant(mergedPlant, this,
                        projectileResolver);
            }
            synchronizePublicState();
            return true;
        }
        if (!tile.addPlant(plant)) {
            return false;
        }
        finishPlanting(plant, cardType);
        applyImitaterEntranceFood(cardType, plant);
        if (plant.getNormalizedType().equals("goldbloom")) {
            sunManager.producePlantSun(plant);
            plant.die();
        }
        if (isInstantPlant(plant)) {
            if (plant.getNormalizedType().endsWith("mint")) {
                plantFoodInventory.applyMint(plant, this, projectileResolver);
            }
            else if (!plant.getNormalizedType().equals("goldbloom")) {
                ExplosivePlant.resolveInstantPlant(plant, this, projectileResolver);
            }
            tickProcessor.removeDestroyedObjects();
        }
        synchronizePublicState();
        return true;
    }
    public String getPlantingError(String type, GridPosition position) {
        return getPlantingError(type, position, createPlantForCard(type));
    }
    private String getPlantingError(String type, GridPosition position,
                                    Plant plant) {
        if (plant == null) {
            return "Unknown plant type.";
        }
        if (!stageConfig.isPlantSelected(type)) {
            return "Plant is not selected.";
        }
        if (position == null || !board.isInside(position)) {
            return "Invalid location.";
        }
        if (!cooldownDisabled && getCooldown(normalize(type)) > 0f) {
            return "Plant is on cooldown.";
        }
        if (!plant.canPlantOn(board.getTile(position))) {
            return "Plant cannot be planted on this tile.";
        }
        if (currentSunAmount < plant.sunCost) {
            return "Not enough sun.";
        }
        return null;
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
    private boolean mergePeaPod(Tile tile, Plant plant) {
        if (!plant.getNormalizedType().equals("peapod")) {
            return false;
        }
        for (Plant existing : tile.getPlants()) {
            if (existing.getNormalizedType().equals("peapod")
                    && existing instanceof ShooterPlant) {
                ShooterPlant peaPod = (ShooterPlant) existing;
                if (peaPod.multiShot < 5) {
                    peaPod.multiShot++;
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isInstantPlant(Plant plant) {
        String type = plant.getNormalizedType();
        return type.endsWith("mint") || type.equals("goldbloom")
                || type.equals("iceshroom")
                || type.equals("hotpotato") || type.equals("gravebuster")
                || type.equals("doomshroom") || type.equals("jalapeno")
                || plant instanceof ExplosivePlant && ((ExplosivePlant) plant).instantUse;
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
        tickProcessor.removeDestroyedObjects();
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
        notifyOutcome();
    }

    Plant findPlantTarget(Zombie zombie) {
        if (zombie instanceof ProspectorZombie
                && ((ProspectorZombie) zombie).reversedByDynamite) {
            return findNearestPlantToRight(zombie);
        }
        return findNearestPlantAhead(zombie);
    }

    private Plant findNearestPlantToRight(Zombie zombie) {
        int minimumX = Math.max(0, (int) Math.floor(zombie.currentPosition.x));
        for (int x = minimumX; x < board.columns; x++) {
            Tile tile = board.getTile(new GridPosition(x, zombie.lane));
            if (tile == null) {
                continue;
            }
            List<Plant> plants = tile.getPlants();
            for (int index = plants.size() - 1; index >= 0; index--) {
                Plant plant = plants.get(index);
                if (plant.isAlive && !plant.isCatTransformed) {
                    return plant;
                }
            }
        }
        return null;
    }

    public Plant findNearestPlantAhead(Zombie zombie) {
        if (zombie == null) {
            return null;
        }
        int maximumX = Math.min(board.columns - 1,
                (int) Math.floor(zombie.currentPosition.x));
        for (int x = maximumX; x >= 0; x--) {
            Tile tile = board.getTile(new GridPosition(x, zombie.lane));
            if (tile == null) {
                continue;
            }
            List<Plant> plants = tile.getPlants();
            for (int index = plants.size() - 1; index >= 0; index--) {
                Plant plant = plants.get(index);
                if (plant.isAlive && !plant.isCatTransformed) {
                    return plant;
                }
            }
        }
        return null;
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
    public List<Plant> getPlantCatalog() {
        List<Plant> plants = new ArrayList<>();
        for (String type : Plant.getSpreadsheetTypes()) {
            if (!stageConfig.isPlantSelected(type)) {
                continue;
            }
            Plant plant = createPlant(type);
            if (plant != null) {
                plants.add(plant);
            }
        }
        return plants;
    }
    public float getCooldown(String type) {
        return cooldowns.getOrDefault(normalize(type), 0f);
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

    public int getCoins() { return coins; }
    public int getDiamonds() { return diamonds; }
    public int getPots() { return pots; }
    void addCoins(int amount) { coins += amount; }
    void addDiamonds(int amount) { diamonds += amount; }
    void addPots(int amount) { pots += amount; }
    private void synchronizePublicState() {
        currentSunAmount = sunManager.currentSunAmount;
        plantFoodCount = plantFoodInventory.count;
        currentWaveNumber = waveController.currentWaveNumber;
    }
    private Plant createPlant(String type) {
        if (type == null) {
            return null;
        }
        Plant plant = Plant.createSpreadsheetPlant(nextPlantId++, type);
        if (plant == null) {
            return null;
        }
        int targetLevel = stageConfig.getPlantLevel(type);
        while (plant.level < targetLevel) {
            plant.upgrade();
        }
        return plant;
    }
    private Plant createPlantForCard(String type) {
        if (!normalize(type).equals("imitater")) {
            return createPlant(type);
        }
        if (stageConfig.imitaterTargetType == null) {
            return null;
        }
        Plant copiedPlant = createPlant(stageConfig.imitaterTargetType);
        applyImitaterCardUpgrades(copiedPlant);
        return copiedPlant;
    }

    private void applyImitaterCardUpgrades(Plant copiedPlant) {
        if (copiedPlant == null) {
            return;
        }
        int imitaterLevel = stageConfig.getPlantLevel("Imitater");
        if (imitaterLevel >= 2) {
            copiedPlant.rechargeTime = Math.max(0f,
                    copiedPlant.rechargeTime - 2f);
        }
        if (imitaterLevel >= 3) {
            copiedPlant.sunCost = Math.max(0, copiedPlant.sunCost - 25);
            copiedPlant.cost = copiedPlant.sunCost;
        }
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
}
