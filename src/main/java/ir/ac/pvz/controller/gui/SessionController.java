package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.game_core.GameOutcomeListener;
import ir.ac.pvz.controller.managers.GameSettings;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.GameStatus;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.controller.game_core.WaveController;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.Wave;
import ir.ac.pvz.model.others.StageConfig;
import ir.ac.pvz.model.stage.BossStage;
import ir.ac.pvz.model.stage.ConveyorBeltStage;
import ir.ac.pvz.model.stage.DeadLineStage;
import ir.ac.pvz.model.stage.LoveYourPlantsStage;
import ir.ac.pvz.model.stage.PlantWhatYouGetStage;
import ir.ac.pvz.model.stage.SaveOurSeedsStage;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.stage.TimedWarStage;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.ShotEvent;
import ir.ac.pvz.model.support.StationaryMovementStrategy;
import ir.ac.pvz.model.stage.NightOpsStage;
import ir.ac.pvz.model.user.NewsType;
import ir.ac.pvz.model.travel.ScoreTracker;
import ir.ac.pvz.model.travel.ScoredGame;
import ir.ac.pvz.model.user.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SessionController {
    final List<String> boostedThisStage = new ArrayList<>();
    int lastScore;
    private Long scoredSeed;

    private static final int DEFAULT_STARTING_SUN = 250;
    static final float TICK_SECONDS = 0.1f;

    private static final float SPAWN_COLUMN_INSET = 0.02f;

    private final Chapter chapter;
    final Stage stage;
    private final String minigame;
    private final int minigameLevel;
    private final SeasonType season;
    final Board board;
    final GameSession session;
    final List<String> selectedPlants;
    private final List<GridPosition> protectedTiles = new ArrayList<>();
    final MinigameRuntime minigameRuntime;
    private final StageCounters stageCounters = new StageCounters();
    private final SessionOutcome outcome = new SessionOutcome();
    private final SessionLoop loop = new SessionLoop();
    final WaveScheduler waveScheduler;

    final List<String> conveyorQueue = new ArrayList<>();
    float conveyorTimer;

    MinigameController minigameController;
    private ZombossController zomboss;
    private final ChapterEffectsController chapterEffects = new ChapterEffectsController();
    int lastWaveSeen;
    private float tickAccumulator;
    private float elapsedSeconds;
    private boolean paused;
    private boolean wavesStarted;
    private final java.util.Set<String> observedZombies = new java.util.HashSet<>();
    final ScoreNoticeCollector scoreNotices = new ScoreNoticeCollector();

    public SessionController(Chapter chapter, Stage stage, String minigame,
                             List<String> selectedPlants) {
        this(chapter, stage, minigame, selectedPlants, 1);
    }

    public SessionController(Chapter chapter, Stage stage, String minigame,
                             List<String> selectedPlants, int minigameLevel) {
        this.chapter = chapter;
        this.stage = stage;
        this.minigame = minigame;
        this.minigameLevel = Math.max(1, minigameLevel);
        this.selectedPlants = new ArrayList<>(selectedPlants);
        this.season = chapter == null ? SeasonType.ANCIENT_EGYPT
            : CampaignController.getInstance().seasonOf(chapter);

        this.board = new Board(5, 9, season);
        if (chapter != null && minigame == null) {
            chapter.applyChapterEffects(board);
        }

        StageConfig config = buildConfig();
        this.session = new GameSession(board, startingSun(), config);
        if (minigame != null) {
            this.minigameController = new MinigameController(minigame, board.rows,
                board.columns, minigameLevel);
        }

        if (isBossStage()) {
            this.zomboss = new ZombossController(season, board, session,
                StageConfigFactory.activeDifficulty());
        }

        this.minigameRuntime = new MinigameRuntime(board, session,
            minigameController);
        this.waveScheduler = new WaveScheduler(board, session);

        prepareStage();
    }

    private int startingSun() {
        if (stage instanceof PlantWhatYouGetStage) {
            return ((PlantWhatYouGetStage) stage).getInitialSun();
        }

        return DEFAULT_STARTING_SUN;
    }

    private StageConfig buildConfig() {
        return new StageConfigFactory(season, stage, minigame, selectedPlants,
            boostedThisStage, scoredSeed).build();
    }

    private void prepareStage() {
        if (stage instanceof SaveOurSeedsStage) {
            plantProtectedSeeds((SaveOurSeedsStage) stage);
        }

        if (blocksSkySun()) {
            session.getSunManager().setSkyDropEnabled(false);
        }

        if (season == SeasonType.FROSTBITE_CAVES && stage != null) {
            chapterEffects.freezeStartingZombies(board, session);
        }

        if (isIZombie()) {
            prepareIZombieLawn();
        }

        if (minigameController != null && minigameController.isVasebreaker()) {
            session.getWaveController().disableAutomaticWaves();
        }

        if (minigame == null) {
            chapterEffects.prepareChapterTiles(chapter, board);
        }
        else {
            clearBoardObstacles();
        }

        if (usesConveyor()) {
            refillConveyor();
        }

        installOutcomeListener();
    }

    private void plantProtectedSeeds(SaveOurSeedsStage saveOurSeeds) {
        for (int row = 0; row < board.rows; row += 2) {
            GridPosition position = new GridPosition(0, row);
            Plant guarded = Plant.createSpreadsheetPlant(9000 + row,
                "Sunflower");

            if (guarded == null || !board.getTile(position).addPlant(guarded)) {
                continue;
            }

            protectedTiles.add(position);
            saveOurSeeds.addProtectedPosition(0, row);
        }
    }

    private void prepareIZombieLawn() {
        board.clearLawnMowers();
        session.getWaveController().disableAutomaticWaves();
        IZombieLawn.plantDefenders(board, minigameLevel,
            IZOMBIE_PLANT_COLUMNS);
        minigameRuntime.placeSunProducerZombies(board.columns - 1);
    }

    private void clearBoardObstacles() {
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                ir.ac.pvz.model.support.Tile tile =
                    board.getTile(new GridPosition(column, row));
                if (tile == null) {
                    continue;
                }

                tile.obstacle = null;
                tile.isWater = false;
                tile.canPlant = true;
                tile.restoreNativeGround();
            }
        }
    }

    public int zombieSunAmount() {
        if (!isIZombie() || minigameController == null) {
            return 0;
        }

        return ((ir.ac.pvz.model.minigame.IZombie) minigameController.getGame())
            .getSunAmount();
    }

    private void installOutcomeListener() {
        session.setOutcomeListener(new GameOutcomeListener() {
            @Override
            public void onGameWon(GameSession session) {
                awardVictory();
            }

            @Override
            public void onGameLost(GameSession session) {
                recordAttempt();
            }
        });
    }

    public boolean usesConveyor() {
        return stage instanceof ConveyorBeltStage
            || isBossStage()
            || "Wall-nut Bowling".equals(minigame)
            || "Vasebreaker".equals(minigame)
            || "I, Zombie".equals(minigame);
    }

    private boolean blocksSkySun() {
        return stage instanceof NightOpsStage
            || stage instanceof PlantWhatYouGetStage
            || "Vasebreaker".equals(minigame)
            || "Wall-nut Bowling".equals(minigame)
            || isIZombie();
    }

    public boolean waitsForManualStart() {
        return stage instanceof PlantWhatYouGetStage;
    }

    public List<String> getConveyorQueue() {
        return conveyorQueue;
    }

    void refillConveyor() {
        conveyorTimer = ConveyorBelt.refill(conveyorQueue, selectedPlants,
            minigame, minigameController, conveyorTimer);
    }

    public void start() {
        if (!waitsForManualStart()) {
            beginWaves();
        }
    }

    public void beginWaves() {
        if (wavesStarted) {
            return;
        }

        wavesStarted = true;
        if (stage != null) {
            stage.startStage();
        }

        session.start();
        chapterEffects.announce("The zombies are coming!", 2.6f);
        waveScheduler.harvestNewWave();
        if (zomboss != null) {
            zomboss.spawn();
        }
    }

    void announceWaveStart() {
        WaveNarration.announce(session, chapterEffects, lastWaveSeen);
    }

    float entryColumn(int lane) {
        return WaveNarration.entryColumn(this, lane, SPAWN_COLUMN_INSET);
    }

    public int getPendingZombieCount() {
        return waveScheduler.getPendingZombieCount();
    }

    public ZombossController getZomboss() {
        return zomboss;
    }

    public ChapterEffectsController getChapterEffects() {
        return chapterEffects;
    }

    public void update(float delta) {
        if (paused || isFinished()) {
            return;
        }

        float scaled = delta * GameSettings.getInstance().getGameSpeed();
        elapsedSeconds += scaled;
        tickAccumulator += scaled;
        chapterEffects.update(scaled);
        loop.updateZomboss(this, scaled);
        loop.detectWaveChange(this);

        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            loop.advanceOneTick(this);
        }
    }

    void recordSeenZombies() {
        SeenZombieLog.record(board, observedZombies);
    }

    public String pollScoreNotice() {
        return scoreNotices.poll();
    }

    public MinigameController getMinigameController() {
        return minigameController;
    }

    void updateStageRules() {
        syncStageCounters();
        StageRuleEvaluator.evaluate(stage, board, session, protectedTiles,
            elapsedSeconds);
    }

    private void syncStageCounters() {
        stageCounters.sync(stage, session);
    }

    public boolean isBowling() {
        return "Wall-nut Bowling".equals(minigame);
    }

    public static final int IZOMBIE_PLANT_COLUMNS = 5;

    public boolean isIZombie() {
        return "I, Zombie".equals(minigame);
    }

    public List<String> zombieCardTypes() {
        return ZombieCards.types(minigameController);
    }

    public int zombieCardCost(String type) {
        return ZombieCards.cost(minigameController, type);
    }

    public int getMinigameLevel() {
        return minigameLevel;
    }

    public boolean placeZombieCard(String type, GridPosition position) {
        return ZombieCards.place(this, type, position);
    }

    public boolean launchNut(String type, GridPosition position) {
        if (!isBowling() || minigameController == null) {
            return false;
        }

        if (position.x > minigameController.getRedLineColumn()) {
            return false;
        }

        return minigameController.launchNut(position.y, position.x, type) != null;
    }

    public boolean plant(String type, GridPosition position) {
        return PlantPlacement.plant(this, type, position);
    }

    public String plantingError(String type, GridPosition position) {
        return session.getPlantingError(type, position);
    }

    public Plant shovel(GridPosition position) {
        return session.pluckPlant(position);
    }

    public boolean feed(GridPosition position) {
        return session.feedPlant(position);
    }

    public void addSunCheat() {
        CheatCommands.addSun(session);
    }

    public void addCoinsCheat() {
        CheatCommands.addCoins();
    }

    public void addGemsCheat() {
        CheatCommands.addGems();
    }

    public void addPlantFoodCheat() {
        CheatCommands.addPlantFood(session);
    }

    public void saveProgress() {
        User user = MenuManager.getInstance().getActiveUser();
        if (user != null) {
            UserManager.getInstance().saveAll();
        }
    }

    public void togglePause() {
        paused = !paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isFinished() {
        return session.status == GameStatus.WON || session.status == GameStatus.LOST;
    }

    public boolean isWon() {
        return session.status == GameStatus.WON;
    }

    public boolean hasStartedWaves() {
        return wavesStarted;
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public float waveProgress() {
        return WaveNarration.progress(session);
    }

    private void awardVictory() {
        outcome.awardVictory(this);
    }

    public void setShotListener(ShotEvent.Listener listener) {
        session.setShotListener(listener);
    }

    public void setPlantFoodListener(
        ir.ac.pvz.model.others.PlantFoodInventory.Listener listener) {
        session.setPlantFoodListener(listener);
    }

    public void setLootListener(
        ir.ac.pvz.controller.game_core.LootDropService.Listener listener) {
        session.setLootListener(listener);
    }

    public void setScoredSeed(Long seed) {
        this.scoredSeed = seed;
    }

    public boolean isScoredRun() {
        return scoredSeed != null;
    }

    public int computeScore() {
        ScoreTracker tracker = new ScoreTracker();
        ScoredGame scored = new ScoredGame(scoredSeed == null ? 0L : scoredSeed);
        return scored.calculateScore(tracker.detect(session.getStatistics()));
    }

    public int getLastScore() {
        return lastScore;
    }

    private void recordAttempt() {
        outcome.recordAttempt(this);
    }

    public int getRemainingBrains() {
        if (!isIZombie() || minigameController == null) {
            return 0;
        }

        return ((ir.ac.pvz.model.minigame.IZombie)
            minigameController.getGame()).getRemainingBrains();
    }

    public GameSession getSession() {
        return session;
    }

    public Board getBoard() {
        return board;
    }

    public Stage getStage() {
        return stage;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public String getMinigame() {
        return minigame;
    }

    public SeasonType getSeason() {
        return season;
    }

    public List<String> getSelectedPlants() {
        return selectedPlants;
    }

    public int waterBoundaryColumn() {
        if (!(chapter instanceof ir.ac.pvz.model.chapter.BigWaveBeach)) {
            return -1;
        }

        return board.columns
            - ((ir.ac.pvz.model.chapter.BigWaveBeach) chapter).getCurrentWaterLevel();
    }

    public List<GridPosition> getProtectedTiles() {
        return protectedTiles;
    }

    public int deadlineColumn() {
        return stage instanceof DeadLineStage
            ? ((DeadLineStage) stage).getDeadLineColumn() : -1;
    }

    public boolean isBossStage() {
        return stage instanceof BossStage;
    }
}
