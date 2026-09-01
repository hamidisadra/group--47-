package ir.ac.pvz.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ir.ac.pvz.controller.gui.CampaignController;
import ir.ac.pvz.controller.gui.CouchPlay;
import ir.ac.pvz.controller.gui.SessionController;
import ir.ac.pvz.controller.gui.ZombossController;
import ir.ac.pvz.controller.gui.ZombossSupport;
import ir.ac.pvz.controller.managers.GameSettings;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Sun;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.assets.GameAssets;
import ir.ac.pvz.view.render.BattleOverlay;
import ir.ac.pvz.view.render.BoardRenderer;
import ir.ac.pvz.view.render.EffectLayer;
import ir.ac.pvz.view.render.Lawn;
import ir.ac.pvz.view.ui.Modal;
import ir.ac.pvz.view.ui.PlantCard;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

import java.util.List;

public class GameScreen extends ScreenAdapter {
    private static final float VIEW_WIDTH = 1400f;
    private static final float SUN_PICKUP_RADIUS = 55f;

    private final PvzGame game;
    private final GameAssets assets;
    private final SessionController controller;
    private final BoardRenderer renderer;
    private final BattleOverlay overlay;
    private final EffectLayer effects;
    private final FillViewport worldViewport;
    private final OrthographicCamera worldCamera;
    private final Stage ui;

    private final Table seedBar = new Table();
    private final List<PlantCard> seedCards = new java.util.ArrayList<>();
    private List<String> seedTypes = new java.util.ArrayList<>();
    private com.badlogic.gdx.scenes.scene2d.Actor boardLayer;
    private Label sunLabel;
    private Label plantFoodLabel;
    private Label coinsLabel;
    private Label gemsLabel;
    private Label objectiveLabel;
    private Label announcement;
    private Label bossName;
    private Table bossBar;
    private final java.util.List<ProgressBar> bossSegments = new java.util.ArrayList<>();
    private ProgressBar waveBar;

    private float animationTime;
    private String armedPlant;
    private boolean shovelMode;
    private boolean plantFoodMode;
    private boolean outcomeShown;
    private final Vector3 pointer = new Vector3();
    private VersusOverlay versus;
    private CouchPlay couch;

    public GameScreen(PvzGame game, Chapter chapter,
                      ir.ac.pvz.model.stage.Stage stage, String minigame,
                      List<String> plants) {
        this(game, chapter, stage, minigame, plants, 1);
    }

    public GameScreen(PvzGame game, Chapter chapter,
                      ir.ac.pvz.model.stage.Stage stage, String minigame,
                      List<String> plants, int minigameLevel) {
        this.game = game;
        this.assets = GameAssets.get();
        this.controller = new SessionController(chapter, stage, minigame, plants,
            minigameLevel);

        if (game.isScoredRun()) {
            this.controller.setScoredSeed(game.getScoredSeed());
        }

        this.renderer = new BoardRenderer(controller.getSeason());
        this.overlay = new BattleOverlay(game.batch, controller, assets, renderer);
        this.effects = new EffectLayer(controller.getBoard().rows);
        this.renderer.setShowGrid(GameSettings.getInstance().isShowGrid());

        this.worldCamera = createWorldCamera();
        this.worldViewport = new FillViewport(VIEW_WIDTH, Lawn.HEIGHT,
            worldCamera);

        ui = new Stage(new FitViewport(1280f, 720f), game.batch);
        installSessionListeners();
        this.versus = new VersusOverlay(game, controller);
        this.couch = game.isCouchPlay() ? new CouchPlay(controller) : null;
        buildHud();
        versus.installReactionBar(ui);
        presentIntro(stage, minigame);

        game.jukebox.playMusic(controller.isBossStage() ? "boss" : "battle");
    }

    private OrthographicCamera createWorldCamera() {
        OrthographicCamera camera = new OrthographicCamera();

        camera.position.set(
            Lawn.GRID_LEFT
                + controller.getBoard().columns * Lawn.CELL_WIDTH * 0.5f,
            Lawn.HEIGHT * 0.5f, 0f);
        camera.update();

        return camera;
    }

    private void installSessionListeners() {
        this.controller.setShotListener(event -> {
            effects.onShot(event);
            renderer.notifyPlantAction(event.source);
            game.jukebox.play("shoot");
        });

        this.controller.setLootListener((type, amount, total) -> {
            Toast.info(ui, lootMessage(type, amount, total));
            game.jukebox.play("coin");
        });

        this.controller.setPlantFoodListener(total -> {
            Toast.info(ui, "A glowing zombie dropped plant food!  (" + total + ")");
            game.jukebox.play("food");
        });

        if (controller.getZomboss() != null) {
            controller.getZomboss().setImpactListener((position, radius, shake) -> {
                effects.explosion(position.x, position.y, radius);
                effects.shake(0.25f, shake);
                game.jukebox.play("explode");
            });
        }
    }

    private void presentIntro(ir.ac.pvz.model.stage.Stage stage, String minigame) {
        java.util.List<ir.ac.pvz.view.ui.DialogueBox.Line> intro =
            minigame != null ? new java.util.ArrayList<>()
                : ir.ac.pvz.controller.gui.DialogueScript.intro(
                controller.getSeason(),
                stage == null ? 1 : stage.getNumber(),
                controller.isBossStage());

        if (intro.isEmpty()) {
            showObjectives();
            return;
        }

        new ir.ac.pvz.view.ui.DialogueBox(intro, this::showObjectives).present(ui);
    }

    private void buildHud() {
        installBoardLayer();

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        root.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);
        ui.addActor(root);

        waveBar = new ProgressBar(0f, 1f, 0.01f, false, Ui.skin(),
            Ui.skin().has("ingame_progress", ProgressBar.ProgressBarStyle.class)
                ? "ingame_progress" : "default-horizontal");

        root.add(buildTopBar()).growX().row();
        root.add(buildStatusStrip()).left().padTop(3f).padLeft(12f)
            .pad(3f, 12f, 0f, 12f).row();

        if (controller.getZomboss() != null) {
            root.add(buildBossBar()).padTop(6f).row();
        }

        announcement = Ui.label("", "big_outline");
        announcement.setColor(1f, 0.25f, 0.2f, 1f);
        announcement.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        root.add(announcement).expandY().center().row();
        root.add().expandY().row();

        if (controller.waitsForManualStart()) {
            installManualStartButton();
        }

        refreshSeedBar();
    }

    private void installBoardLayer() {
        boardLayer = new Actor();
        boardLayer.setBounds(0f, 0f, ui.getViewport().getWorldWidth(),
            ui.getViewport().getWorldHeight());
        boardLayer.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointerId,
                                     int button) {
                com.badlogic.gdx.math.Vector2 screen = ui.stageToScreenCoordinates(
                    new com.badlogic.gdx.math.Vector2(event.getStageX(),
                        event.getStageY()));
                handleClick((int) screen.x, (int) screen.y);
                return true;
            }
        });
        ui.addActor(boardLayer);
    }

    private Table buildTopBar() {
        Table top = new Table();
        top.setBackground(Ui.solid(new Color(0f, 0f, 0f, 0.42f)));
        top.pad(4f, 12f, 4f, 12f);
        top.add(buildCounters()).left().padRight(12f);
        top.add(seedBar).expandX().left();
        top.add(buildTools()).right();
        return top;
    }

    private Table buildStatusStrip() {
        objectiveLabel = Ui.label("", "medium");
        objectiveLabel.setFontScale(0.7f);

        Table waveTrack = new Table();
        waveTrack.add(waveBar).width(240f).row();
        waveTrack.add(buildWaveMarkers()).width(240f).height(10f);

        Table statusStrip = new Table();
        statusStrip.setBackground(Ui.solid(new Color(0f, 0f, 0f, 0.42f)));
        statusStrip.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        statusStrip.add(waveTrack).padRight(14f);
        statusStrip.add(objectiveLabel).left();
        return statusStrip;
    }

    private Table buildBossBar() {
        bossBar = new Table();
        bossBar.setBackground(Ui.solid(new Color(0f, 0f, 0f, 0.55f)));
        bossBar.pad(6f, 12f, 6f, 12f);
        bossName = Ui.label("", "medium");
        bossBar.add(bossName).padRight(14f);

        for (int segment = 0; segment < ZombossController.SEGMENTS; segment++) {
            ProgressBar bar = new ProgressBar(0f, 1f, 0.01f, false, Ui.skin(),
                Ui.skin().has("xp_fuschia", ProgressBar.ProgressBarStyle.class)
                    ? "xp_fuschia" : "default-horizontal");
            bar.setValue(1f);
            bossSegments.add(bar);
            bossBar.add(bar).width(180f).padRight(6f);
        }

        return bossBar;
    }

    private void installManualStartButton() {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton start = Ui.button("Start!", "green");
        start.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.beginWaves();
                start.remove();
            }
        });

        Table wrapper = new Table();
        wrapper.setFillParent(true);
        wrapper.bottom().right().pad(120f);
        wrapper.add(start).width(220f).height(70f);
        ui.addActor(wrapper);
    }

    private Table buildWaveMarkers() {
        Table markers = new Table();
        int total = Math.max(1, controller.getSession()
            .getWaveController().getTotalWaves());

        for (int wave = 1; wave <= total; wave++) {
            Table tick = new Table();
            boolean finalWave = wave == total;
            tick.setBackground(Ui.solid(finalWave
                ? new Color(1f, 0.35f, 0.2f, 0.95f)
                : new Color(1f, 1f, 1f, 0.75f)));
            markers.add(tick).size(finalWave ? 5f : 3f, 10f)
                .expandX().right();
        }

        return markers;
    }

    private Table buildCounters() {
        sunLabel = Ui.label("0", "medium");
        plantFoodLabel = Ui.label("0", "medium");
        coinsLabel = Ui.label("0", "secondary");
        gemsLabel = Ui.label("0", "secondary");

        Table counters = new Table();
        counters.add(Ui.label("Sun", "medium")).padRight(5f);
        counters.add(sunLabel).padRight(14f);
        counters.add(Ui.label("Food", "medium")).padRight(5f);
        counters.add(plantFoodLabel).padRight(14f);
        counters.add(Ui.label("Coins", "secondary")).padRight(5f);
        counters.add(coinsLabel).padRight(12f);
        counters.add(Ui.label("Gems", "secondary")).padRight(5f);
        counters.add(gemsLabel);
        return counters;
    }

    private Table buildTools() {
        Table tools = new Table();
        tools.add(toolButton("Shovel", () -> {
            shovelMode = !shovelMode;
            plantFoodMode = false;
            armedPlant = null;
        })).padRight(5f);
        tools.add(toolButton("Plant food", () -> {
            plantFoodMode = !plantFoodMode;
            shovelMode = false;
            armedPlant = null;
        })).padRight(5f);
        tools.add(toolButton("Pause", this::openPauseMenu));

        if (GameSettings.getInstance().isDebugMode()) {
            tools.add(toolButton("+Sun", controller::addSunCheat)).padLeft(5f);
            tools.add(toolButton("+Food", controller::addPlantFoodCheat)).padLeft(5f);
            tools.add(toolButton("+Coins", controller::addCoinsCheat)).padLeft(5f);
            tools.add(toolButton("+Gems", controller::addGemsCheat)).padLeft(5f);
        }

        return tools;
    }

    private Actor toolButton(String text, Runnable onClick) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, "brown");
        button.getLabel().setFontScale(0.72f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(120f).height(48f);
        return wrapper;
    }

    private List<String> currentCardTypes() {
        if (versus.isPlantSide()) {
            return controller.getSelectedPlants();
        }

        if (controller.isIZombie()) {
            return controller.zombieCardTypes();
        }

        return controller.usesConveyor()
            ? controller.getConveyorQueue() : controller.getSelectedPlants();
    }

    private void refreshSeedBar() {
        List<String> available = currentCardTypes();
        if (!available.equals(seedTypes)) {
            rebuildSeedBar(available);
        }

        for (int index = 0; index < seedCards.size() && index < seedTypes.size(); index++) {
            PlantCard card = seedCards.get(index);
            if (card == null) {
                continue;
            }

            String type = seedTypes.get(index);
            card.selected(type.equals(armedPlant));
            Plant prototype = Plant.createSpreadsheetPlant(0, type);
            float cooldown = controller.getSession().getCooldown(type);
            card.cooldown(prototype == null || prototype.rechargeTime <= 0f
                ? 0f : cooldown / prototype.rechargeTime);
        }
    }

    private void rebuildSeedBar(List<String> available) {
        seedBar.clear();
        seedCards.clear();
        seedTypes = new java.util.ArrayList<>(available);

        if (controller.usesConveyor()) {
            seedBar.setBackground(Ui.solid(new Color(0.16f, 0.13f, 0.10f, 0.92f)));
            seedBar.pad(4f);
        }

        if (controller.isIZombie()) {
            buildZombieCards();
            return;
        }

        for (String type : seedTypes) {
            PlantCard card = plantCard(type);
            seedBar.add(card).pad(2f);
            seedCards.add(card);
        }
    }

    private void buildZombieCards() {
        for (String type : seedTypes) {
            com.badlogic.gdx.scenes.scene2d.ui.TextButton card = Ui.button(
                type + "\n" + controller.zombieCardCost(type), "brown");
            card.getLabel().setFontScale(0.62f);
            card.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    armedPlant = type.equals(armedPlant) ? null : type;
                }
            });
            seedBar.add(card).width(104f).height(56f).pad(2f);
            seedCards.add(null);
        }
    }

    private PlantCard plantCard(String type) {
        Plant prototype = Plant.createSpreadsheetPlant(0, type);
        PlantCard card = new PlantCard(type, 68f);
        card.cost(prototype == null ? 0 : prototype.sunCost);
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                armedPlant = type.equals(armedPlant) ? null : type;
                shovelMode = false;
                plantFoodMode = false;
            }
        });

        if (controller.usesConveyor()) {
            card.getColor().a = 0f;
            card.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(-40f, 0f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.35f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(
                        40f, 0f, 0.35f))));
        }

        return card;
    }

    private void showObjectives() {
        Modal modal = new Modal("Level Objectives");
        StringBuilder text = new StringBuilder();
        for (String objective : CampaignController.getInstance()
            .objectives(controller.getStage())) {
            text.append("- ").append(objective).append("\n");
        }

        if (controller.getMinigame() != null) {
            text.append("- Mini-game: ").append(controller.getMinigame());
        }

        modal.message(text.toString().trim());
        modal.action("Continue", "green", () -> {
            modal.close();
            controller.start();
        });
        modal.show(ui);
    }

    public SessionController getController() {
        return controller;
    }

    public void skipBriefing() {
        for (Actor actor : new com.badlogic.gdx.utils.Array.ArrayIterator<>(ui.getActors())) {
            if (actor instanceof Modal) {
                ((Modal) actor).close();
            }

            else if (actor instanceof ir.ac.pvz.view.ui.DialogueBox) {
                actor.remove();
            }
        }

        controller.start();
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(ui);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        assets.update();
        controller.update(delta);
        versus.update(delta);
        effects.update(delta);
        advanceAnimations(delta);
        handleInput();

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        drawWorld();

        updateHud();
        ui.getViewport().apply();
        ui.act(delta);
        ui.draw();

        if (versus.isFinished() && !outcomeShown) {
            outcomeShown = true;
            versus.showOutcome(ui, game);
            return;
        }

        if (!versus.isOnline() && controller.isFinished() && !outcomeShown) {
            outcomeShown = true;
            showOutcome();
        }
    }

    private void advanceAnimations(float delta) {
        float speed = GameSettings.getInstance().getGameSpeed();

        if (!controller.isPaused()) {
            renderer.advance(delta * speed, controller.getBoard());
        }

        animationTime += delta * (controller.isPaused() ? 0f : speed);
    }

    private void drawWorld() {
        worldViewport.apply();
        worldCamera.position.set(
            Lawn.GRID_LEFT + controller.getBoard().columns * Lawn.CELL_WIDTH * 0.5f
                + effects.shakeOffsetX(),
            Lawn.HEIGHT * 0.5f + effects.shakeOffsetY(), 0f);
        worldCamera.update();
        game.batch.setProjectionMatrix(worldCamera.combined);
        game.batch.begin();
        drawLayers();
        game.batch.end();
        renderer.drawGrid(worldCamera.combined, controller.getBoard());

        if (couch != null) {
            renderer.drawCursor(worldCamera.combined, couch.getCursorColumn(),
                couch.getCursorRow(), controller.getBoard().rows);
        }
    }

    private void drawLayers() {
        GridPosition hovered = pointerCell();
        overlay.sync(animationTime, pointer.x, pointer.y, armedPlant,
            shovelMode, plantFoodMode);

        renderer.drawBackground(game.batch);
        renderer.drawTiles(game.batch, controller.getBoard(), animationTime);
        overlay.drawStageMarkers();
        renderer.drawEntities(game.batch, controller.getBoard(), animationTime);
        renderer.drawSuns(game.batch,
            controller.getSession().getSunManager().getActiveSuns(),
            controller.getBoard().rows, animationTime);
        renderer.drawDebris(game.batch,
            ((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
                Ui.solid(Color.WHITE)).getRegion());
        renderer.drawPartDebris(game.batch);
        overlay.drawVases();
        overlay.drawMinigameExtras();
        effects.draw(game.batch);
        overlay.drawCursorPreview(hovered);
    }

    private GridPosition pointerCell() {
        return cellAt(Gdx.input.getX(), Gdx.input.getY());
    }

    private GridPosition cellAt(int screenX, int screenY) {
        pointer.set(screenX, screenY, 0f);
        worldViewport.unproject(pointer);
        int column = Lawn.columnAt(pointer.x);
        int row = Lawn.rowAt(pointer.y, controller.getBoard().rows);
        GridPosition position = new GridPosition(column, row);
        return controller.getBoard().isInside(position) ? position : null;
    }

    private void handleCouchKeys() {
        if (couch == null) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            couch.moveCursor(0, -1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            couch.moveCursor(0, 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            couch.moveCursor(-1, 0);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            couch.moveCursor(1, 0);
        }

        handleCouchCardKeys();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            couch.placeSelected();
        }

        String message = couch.pollMessage();

        if (message != null) {
            Toast.error(ui, message);
        }
    }

    private void handleCouchCardKeys() {
        int[] keys = {Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3,
            Input.Keys.NUM_4, Input.Keys.NUM_5};

        for (int index = 0; index < keys.length; index++) {
            if (Gdx.input.isKeyJustPressed(keys[index])) {
                couch.selectCard(index);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            couch.cycleCard(1);
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            openPauseMenu();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            GameSettings.getInstance().setShowGrid(!GameSettings.getInstance().isShowGrid());
            renderer.setShowGrid(GameSettings.getInstance().isShowGrid());
        }

        if (GameSettings.getInstance().isDebugMode()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                controller.addSunCheat();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                controller.addPlantFoodCheat();
            }
        }

        if (controller.isPaused() || controller.isFinished()) {
            return;
        }

        handleCouchKeys();

        collectSunUnderPointer();
    }

    private void collectSunUnderPointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        worldViewport.unproject(pointer);
        int rows = controller.getBoard().rows;
        for (Sun sun : new java.util.ArrayList<>(
            controller.getSession().getSunManager().getActiveSuns())) {
            if (sun.isCollected() || sun.groundPosition == null) {
                continue;
            }

            float sunX = Lawn.columnCenterX(sun.groundPosition.x);
            float sunY = Lawn.rowCenterY(sun.groundPosition.y, rows)
                + sun.height * BoardRenderer.SUN_FALL_HEIGHT;
            float dx = pointer.x - sunX;
            float dy = pointer.y - sunY;
            if (dx * dx + dy * dy <= SUN_PICKUP_RADIUS * SUN_PICKUP_RADIUS) {
                controller.getSession().collectSun(sun.groundPosition);
                game.jukebox.play("sun");
                break;
            }
        }
    }

    private void handleClick(int screenX, int screenY) {
        if (controller.isPaused() || controller.isFinished()) {
            return;
        }

        GridPosition cell = cellAt(screenX, screenY);
        if (cell == null) {
            return;
        }

        if (handleVaseClick(cell) || handleToolClick(cell)) {
            return;
        }

        if (armedPlant == null) {
            return;
        }

        handlePlacement(cell);
    }

    private boolean handleVaseClick(GridPosition cell) {
        ir.ac.pvz.controller.gui.MinigameController minigames =
            controller.getMinigameController();
        if (minigames == null || !minigames.isVasebreaker() || armedPlant != null
            || shovelMode || plantFoodMode) {
            return false;
        }

        ir.ac.pvz.controller.gui.MinigameController.VaseResult result =
            minigames.breakVase(cell.x + 1, cell.y + 1);
        if (result == null) {
            return false;
        }

        effects.explosion(cell.x, cell.y, 0.6f);
        game.jukebox.play("vase");
        announceVaseResult(result, cell);
        refreshSeedBar();
        return true;
    }

    private void announceVaseResult(
        ir.ac.pvz.controller.gui.MinigameController.VaseResult result,
        GridPosition cell) {
        if (result.kind.equals("zombie")) {
            controller.getSession().cheatSpawnZombie(result.value, cell.x, cell.y);
            Toast.error(ui, "A zombie was hiding in that vase!");
            return;
        }

        if (result.kind.equals("plant")) {
            Toast.info(ui, "You found a " + result.value
                + ".  Plant it before it disappears!");
            return;
        }

        Toast.info(ui, "That vase was empty.");
    }

    private boolean handleToolClick(GridPosition cell) {
        if (shovelMode) {
            Plant removed = controller.shovel(cell);
            shovelMode = false;
            if (removed == null) {
                Toast.error(ui, "There is no plant on that tile.");
            }

            return true;
        }

        if (!plantFoodMode) {
            return false;
        }

        boolean fed = controller.feed(cell);
        plantFoodMode = false;
        if (!fed) {
            Toast.error(ui, "You need plant food and a plant on that tile.");
        }

        else {
            effects.shake(0.2f, 5f);
        }

        return true;
    }

    private void handlePlacement(GridPosition cell) {
        if (controller.isIZombie() && !versus.isPlantSide()) {
            placeZombieCard(cell);
            return;
        }

        if (controller.isBowling()) {
            if (controller.plant(armedPlant, cell)) {
                armedPlant = null;
                refreshSeedBar();
            }

            else {
                Toast.error(ui, "Nuts can only be rolled from the first three columns.");
            }

            return;
        }

        String error = controller.plantingError(armedPlant, cell);
        if (error != null) {
            Toast.error(ui, error);
            return;
        }

        if (controller.plant(armedPlant, cell)) {
            game.jukebox.play("plant");
            armedPlant = null;
            refreshSeedBar();
        }
    }

    private void placeZombieCard(GridPosition cell) {
        if (versus.isGuest()) {
            versus.requestZombie(armedPlant, cell);
            armedPlant = null;
            refreshSeedBar();
            return;
        }

        if (controller.placeZombieCard(armedPlant, cell)) {
            armedPlant = null;
            refreshSeedBar();
            return;
        }

        if (cell.x < SessionController.IZOMBIE_PLANT_COLUMNS) {
            Toast.error(ui, "Zombies can only be placed right of the red line.");
            return;
        }

        Toast.error(ui, "Not enough sun for that zombie.");
    }

    private String lootMessage(ir.ac.pvz.model.enums.LootType type, int amount, int total) {
        if (type == ir.ac.pvz.model.enums.LootType.COIN) {
            return "A zombie dropped " + amount + " coins!  (" + total + ")";
        }

        if (type == ir.ac.pvz.model.enums.LootType.DIAMOND) {
            return "A zombie dropped a diamond!  (" + total + ")";
        }

        return "A zombie dropped a pot!  (" + total + ")";
    }

    private int lastKillCount;
    private int lastWaveHeard;

    private void playEventSounds() {
        int kills = controller.getSession().getStatistics().getKilledZombies();
        if (kills > lastKillCount) {
            lastKillCount = kills;
            game.jukebox.play("groan");
        }

        int wave = controller.getSession().currentWaveNumber;
        if (wave > lastWaveHeard) {
            lastWaveHeard = wave;
            game.jukebox.play("wave");
        }
    }

    private void updateHud() {
        playEventSounds();
        sunLabel.setText(String.valueOf(controller.isIZombie()
            ? controller.zombieSunAmount()
            : controller.getSession().currentSunAmount));
        plantFoodLabel.setText(String.valueOf(controller.getSession().plantFoodCount));
        waveBar.setValue(controller.waveProgress());
        objectiveLabel.setText(buildStatusText());

        String message = controller.getChapterEffects().getAnnouncement();
        ZombossController zomboss = controller.getZomboss();
        renderer.setBossMove(zomboss == null ? null : zomboss.getActiveMove());

        if (zomboss != null) {
            if (zomboss.getLastAction() != null) {
                message = zomboss.getLastAction();
            }

            bossName.setText(ZombossSupport.title(controller.getSeason()));
            float segmentSize = zomboss.getMaxHealth() / (float) ZombossController.SEGMENTS;
            for (int index = 0; index < bossSegments.size(); index++) {
                float low = segmentSize * (ZombossController.SEGMENTS - index - 1);
                float value = (zomboss.getHealth() - low) / segmentSize;
                bossSegments.get(index).setValue(Math.max(0f, Math.min(1f, value)));
            }
        }

        String notice = controller.pollScoreNotice();
        if (notice != null) {
            Toast.info(ui, notice);
        }

        announcement.setText(message == null ? "" : message);
        refreshSeedBar();
    }

    private String buildStatusText() {
        StringBuilder text = new StringBuilder();
        text.append("Wave ").append(controller.getSession().currentWaveNumber)
            .append(" / ")
            .append(controller.getSession().getWaveController().getTotalWaves());
        ir.ac.pvz.model.stage.Stage stage = controller.getStage();
        if (stage instanceof ir.ac.pvz.model.stage.TimedWarStage) {
            ir.ac.pvz.model.stage.TimedWarStage timed =
                (ir.ac.pvz.model.stage.TimedWarStage) stage;
            text.append("    Time ")
                .append(Math.max(0, timed.getTimeLimitSeconds() - timed.getElapsedSeconds()))
                .append("s    Kills ").append(timed.getKilledCount()).append(" / ")
                .append(timed.getKillTarget());
        }

        if (stage instanceof ir.ac.pvz.model.stage.LoveYourPlantsStage) {
            ir.ac.pvz.model.stage.LoveYourPlantsStage love =
                (ir.ac.pvz.model.stage.LoveYourPlantsStage) stage;
            text.append("    Plants lost ").append(love.getLostCount()).append(" / ")
                .append(love.getMaxPlantLosses());
        }

        return text.toString();
    }

    private void openPauseMenu() {
        if (controller.isFinished()) {
            return;
        }

        controller.setPaused(true);
        Modal modal = new Modal("Game Paused");
        modal.message("The lawn is frozen while this menu is open.");
        modal.action("Resume", "green", () -> {
            modal.close();
            controller.setPaused(false);
        });
        modal.action("Restart", "brown", () -> {
            modal.close();
            game.replace(ScreenId.GAME);
        });
        modal.action("Save and exit", "purple", () -> {
            modal.close();
            controller.saveProgress();
            game.replace(ScreenId.MAIN);
        });
        modal.show(ui);
    }

    private void showOutcome() {
        boolean won = controller.isWon();
        game.jukebox.play(won ? "win" : "lose");
        Modal modal = new Modal(won ? "You Win!" : "You Lost");
        StringBuilder summary = new StringBuilder(won
            ? "The lawn is safe. Well done."
            : "The zombies ate your brains. Try again!");

        if (won) {
            summary.append("\n\nMU points earned: ").append(controller.getLastScore());
        }

        modal.message(summary.toString());
        if (!won) {
            modal.action("Retry", "green", () -> {
                modal.close();
                game.replace(ScreenId.GAME);
            });
        }

        modal.action("Exit", "brown", () -> {
            modal.close();
            game.replace(ScreenId.MAIN);
        });
        modal.show(ui);
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height);
        ui.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        ui.dispose();
    }
}
