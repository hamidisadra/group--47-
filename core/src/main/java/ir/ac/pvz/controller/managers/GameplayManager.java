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
    private int currentChapterProgressOffset;

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
        int progressOffset = getChapterProgressOffset(chapterName);
        return enterChapter(chapterName, Math.max(0, progressOffset));
    }

    public boolean enterChapter(String chapterName, int gameProgress) {
        int progressOffset = getChapterProgressOffset(chapterName);
        if (progressOffset < 0 || gameProgress < progressOffset) {
            return false;
        }

        Chapter chapter = createChapter(chapterName);
        SeasonType season = getChapterSeason(chapterName);
        if (chapter == null || season == null) {
            return false;
        }

        this.board = new Board(5, 9, season);
        chapter.startChapter();
        chapter.applyChapterEffects(board);
        addStages(chapter);

        this.currentChapter = chapter;
        this.currentChapterProgressOffset = progressOffset;
        restoreStageProgress(gameProgress);
        return true;
    }

    private Chapter createChapter(String chapterName) {
        switch (chapterName.toLowerCase()) {
            case "ancient egypt":
                return new AncientEgypt();
            case "frostbite caves":
                return new FrostbiteCaves();
            case "big wave beach":
                return new BigWaveBeach();
            case "dark ages":
                return new DarkAges();
            default:
                return null;
        }
    }

    private SeasonType getChapterSeason(String chapterName) {
        switch (chapterName.toLowerCase()) {
            case "ancient egypt":
                return SeasonType.ANCIENT_EGYPT;
            case "frostbite caves":
                return SeasonType.FROSTBITE_CAVES;
            case "big wave beach":
                return SeasonType.BIG_WAVE_BEACH;
            case "dark ages":
                return SeasonType.DARK_AGES;
            default:
                return null;
        }
    }

    private int getChapterProgressOffset(String chapterName) {
        switch (chapterName.toLowerCase()) {
            case "ancient egypt":
                return 0;
            case "frostbite caves":
                return 4;
            case "big wave beach":
                return 8;
            case "dark ages":
                return 12;
            default:
                return -1;
        }
    }

    private void addStages(Chapter chapter) {
        for (int stageNumber = 1; stageNumber <= 4; stageNumber++) {
            int waveCount = stageNumber + 2;
            chapter.addStage(new NormalStage(stageNumber, stageNumber,
                    waveCount));
        }
    }

    private void restoreStageProgress(int gameProgress) {
        int completedCount = Math.max(0, Math.min(4,
                gameProgress - currentChapterProgressOffset));
        for (int index = 0; index < currentChapter.getStages().size(); index++) {
            Stage stage = currentChapter.getStage(index);
            if (index < completedCount) {
                stage.markCompleted();
            }
            if (index <= completedCount) {
                stage.unlock();
            }
        }
        int currentIndex = Math.min(completedCount,
                currentChapter.getStages().size() - 1);
        this.currentStage = currentChapter.getStage(currentIndex);
    }

    public int completeStage(Stage playedStage) {
        if (playedStage == null || playedStage != currentStage
                || !playedStage.isUnlocked() || !playedStage.markCompleted()) {
            return -1;
        }
        int stageIndex = currentChapter.getStages().indexOf(playedStage);
        Stage nextStage = currentChapter.getStage(stageIndex + 1);
        if (nextStage != null) {
            nextStage.unlock();
            currentStage = nextStage;
        }
        return currentChapterProgressOffset + playedStage.getNumber();
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
