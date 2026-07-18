package ir.ac.pvz.controller.managers;

import ir.ac.pvz.model.chapter.AncientEgypt;
import ir.ac.pvz.model.chapter.BigWaveBeach;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.chapter.DarkAges;
import ir.ac.pvz.model.chapter.FrostbiteCaves;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.shop.Shop;
import ir.ac.pvz.model.stage.NormalStage;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.travel.Leaderboard;

public class GameplayManager {
    private static GameplayManager instance;

    private Board board;
    private Chapter currentChapter;
    private Stage currentStage;
    private Shop shop;
    private Leaderboard leaderboard;

    private GameplayManager() {
        this.shop = new Shop();
        this.leaderboard = new Leaderboard();
    }

    public static GameplayManager getInstance() {
        if (instance == null) {
            instance = new GameplayManager();
        }
        return instance;
    }

    public boolean enterChapter(String chapterName) {
        Chapter chapter;
        SeasonType season;

        switch (chapterName.toLowerCase()) {
            case "ancient egypt":
                chapter = new AncientEgypt();
                season = SeasonType.ANCIENT_EGYPT;
                break;
            case "frostbite caves":
                chapter = new FrostbiteCaves();
                season = SeasonType.FROSTBITE_CAVES;
                break;
            case "big wave beach":
                chapter = new BigWaveBeach();
                season = SeasonType.BIG_WAVE_BEACH;
                break;
            case "dark ages":
                chapter = new DarkAges();
                season = SeasonType.DARK_AGES;
                break;
            default:
                return false;
        }

        this.board = new Board(5, 9, season);
        chapter.startChapter();
        chapter.applyChapterEffects(board);

        Stage stage = new NormalStage(1, 1, 3);
        chapter.addStage(stage);

        this.currentChapter = chapter;
        this.currentStage = stage;
        return true;
    }

    public Board getBoard() {
        return board;
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public boolean startZombieWaves() {
        if (currentStage == null) {
            return false;
        }
        currentStage.startZombieWaves();
        return true;
    }

    public Shop getShop() {
        return shop;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
