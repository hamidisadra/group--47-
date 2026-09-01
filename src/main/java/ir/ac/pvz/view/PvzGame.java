package ir.ac.pvz.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.assets.GameAssets;
import ir.ac.pvz.view.screens.*;
import ir.ac.pvz.view.sound.Jukebox;

public class PvzGame extends Game {
    public SpriteBatch batch;
    public GameAssets assets;
    public Skin skin;
    public Jukebox jukebox;

    private Chapter pendingChapter;
    private Stage pendingStage;
    private String pendingMinigame;
    private int pendingMinigameLevel = 1;
    private java.util.List<String> pendingPlants = new java.util.ArrayList<>();
    private long scoredSeed;
    private ir.ac.pvz.controller.online.OnlineMatch versusMatch;
    private boolean couchPlay;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = GameAssets.get();
        skin = assets.skin();
        jukebox = new Jukebox();

        User previous = UserManager.getInstance().findLoggedInUser();
        if (previous != null) {
            UserManager.getInstance().ensurePlayable(previous);
            MenuManager.getInstance().loginUser(previous);
            navigate(ScreenId.MAIN);
        }

        else {
            navigate(ScreenId.LOGIN);
        }
    }

    public void navigate(ScreenId target) {
        MenuManager.getInstance().goTo(target);
        swap(create(target));
    }

    public void replace(ScreenId target) {
        MenuManager.getInstance().replaceWith(target);
        swap(create(target));
    }

    public void back() {
        if (MenuManager.getInstance().goBack()) {
            swap(create(MenuManager.getInstance().getCurrentScreen()));
            return;
        }

        if (MenuManager.getInstance().getCurrentScreen() != ScreenId.MAIN) {
            navigate(ScreenId.MAIN);
        }
    }

    private void swap(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) {
            previous.dispose();
        }
    }

    private Screen create(ScreenId target) {
        switch (target) {
            case REGISTER:
                return new RegisterScreen(this);
            case MAIN:
                return new MainMenuScreen(this);
            case PROFILE:
                return new ProfileScreen(this);
            case ADVENTURE:
                return new AdventureScreen(this);
            case CHAPTER:
                return new ChapterScreen(this, pendingChapter);
            case COLLECTION:
                return new CollectionScreen(this);
            case GREENHOUSE:
                return new GreenhouseScreen(this);
            case SHOP:
                return new ShopScreen(this);
            case NEWS:
                return new NewsScreen(this);
            case LEADERBOARD:
                return new LeaderboardScreen(this);
            case SETTINGS:
                return new SettingsScreen(this);
            case QUESTS:
                return new QuestsScreen(this);
            case MINIGAMES:
                return new MinigamesScreen(this);
            case MULTIPLAYER:
                return new MultiplayerScreen(this);
            case SCORE_GAME:
                return new ScoreGameScreen(this);
            case PLANT_SELECTION:
                return new PlantSelectionScreen(this, pendingChapter, pendingStage,
                    pendingMinigame);
            case GAME:
                return new GameScreen(this, pendingChapter, pendingStage, pendingMinigame,
                    pendingPlants, pendingMinigameLevel);
            case LOGIN:
            default:
                return new LoginScreen(this);
        }
    }

    public void startBattle(java.util.List<String> plants) {
        this.pendingPlants = new java.util.ArrayList<>(plants);
        navigate(ScreenId.GAME);
    }

    public void startVersusMatch(network.protocol.NetworkMessage start) {
        this.couchPlay = false;
        this.versusMatch = new ir.ac.pvz.controller.online.OnlineMatch(
            network.client.GameClient.getInstance(), start);
        this.versusMatch.observeReactions();

        setPendingMinigame("I, Zombie", 1);
        startBattle(java.util.Collections.emptyList());
    }

    public ir.ac.pvz.controller.online.OnlineMatch getVersusMatch() {
        return versusMatch;
    }

    public boolean isCouchPlay() {
        return couchPlay;
    }

    public void startCouchMatch() {
        this.couchPlay = true;
        this.versusMatch = null;
        setPendingMinigame("I, Zombie", 1);
        startBattle(java.util.Collections.emptyList());
    }

    public void clearCouchPlay() {
        this.couchPlay = false;
    }

    public void clearVersusMatch() {
        this.versusMatch = null;
    }

    public void setScoredSeed(long seed) {
        this.scoredSeed = seed;
    }

    public long getScoredSeed() {
        return scoredSeed;
    }

    public boolean isScoredRun() {
        return scoredSeed != 0L;
    }

    public void clearScoredRun() {
        this.scoredSeed = 0L;
    }

    public java.util.List<String> getPendingPlants() {
        return pendingPlants;
    }

    public void setPendingChapter(Chapter chapter) {
        this.pendingChapter = chapter;
        this.pendingMinigame = null;
        this.scoredSeed = 0L;
    }

    public void setPendingStage(Stage stage) {
        this.pendingStage = stage;
    }

    public void setPendingMinigame(String minigame) {
        setPendingMinigame(minigame, 1);
    }

    public void setPendingMinigame(String minigame, int level) {
        this.pendingMinigame = minigame;
        this.pendingMinigameLevel = Math.max(1, level);
        this.pendingStage = null;
    }

    public int getPendingMinigameLevel() {
        return pendingMinigameLevel;
    }

    public Chapter getPendingChapter() {
        return pendingChapter;
    }

    public Stage getPendingStage() {
        return pendingStage;
    }

    public String getPendingMinigame() {
        return pendingMinigame;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }

        jukebox.dispose();
        batch.dispose();
        assets.dispose();
    }
}
