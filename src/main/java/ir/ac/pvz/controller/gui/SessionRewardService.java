package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.user.NewsType;
import ir.ac.pvz.model.user.User;

import java.util.List;

final class SessionRewardService {
    private static final int VICTORY_COINS = 150;

    private SessionRewardService() {
    }

    static void awardVictory(User user, Chapter chapter, Stage stage,
                             String minigame, int minigameLevel,
                             List<String> boostedThisStage) {
        user.addGame();
        user.getWallet().addCoins(VICTORY_COINS);
        recordProgress(user, chapter, stage);

        if (minigame != null) {
            user.completeMinigame(minigame + " #" + minigameLevel);
            user.addNews("Mini-game cleared: " + minigame
                + " (level " + minigameLevel + ").", NewsType.LEVEL_UNLOCK);
        }

        consumeEarnedBoosts(user, boostedThisStage);
    }

    static void recordProgress(User user, Chapter chapter, Stage stage) {
        if (chapter == null || stage == null) {
            return;
        }

        int index = CampaignController.getInstance().globalIndex(chapter, stage);

        if (index <= user.getGameProgress()) {
            return;
        }

        user.setGameProgress(index);
        user.addNews("A new level is unlocked after clearing "
            + chapter.getName() + " - stage " + stage.getNumber() + ".",
            NewsType.LEVEL_UNLOCK);
    }

    static void recordScore(User user, int score) {
        if (score > user.getMaxMuPoint()) {
            user.setMaxMuPoint(score);
        }

    }

    static void recordAttempt(User user, List<String> boostedThisStage) {
        user.addGame();
        consumeEarnedBoosts(user, boostedThisStage);
        UserManager.getInstance().saveAll();
    }

    static void consumeEarnedBoosts(User user, List<String> boostedThisStage) {
        if (boostedThisStage.isEmpty()) {
            return;
        }

        user.getCollection().consumeEarnedBoosts(boostedThisStage);
        boostedThisStage.clear();
    }
}
