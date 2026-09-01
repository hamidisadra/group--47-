package ir.ac.pvz.model.travel;

import ir.ac.pvz.controller.gui.CampaignController;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.user.User;

public final class LeaderboardBuilder {

    private LeaderboardBuilder() {
    }

    public static Leaderboard build() {
        Leaderboard leaderboard = new Leaderboard();

        for (User user : UserManager.getInstance().getAllUsers()) {
            LeaderboardEntry entry = leaderboard.getOrCreateEntry(user.getUsername());
            entry.setLastProgress(chapterNameFor(user.getGameProgress()),
                stageNumberFor(user.getGameProgress()));
            entry.updateHighScore(user.getMaxMuPoint());
            entry.setRankedScore(user.hasRankedScore());

            for (int index = 0; index < user.getCompletedMinigameCount(); index++) {
                entry.addMinigameCompleted();
            }

            for (int index = 0; index < user.getQuestLog().getDailyCompleted(); index++) {
                entry.addDailyQuestCompleted();
            }

            for (int index = 0; index < user.getQuestLog().getNonDailyCompleted(); index++) {
                entry.addNonDailyQuestCompleted();
            }
        }

        leaderboard.sortBy("score", false);
        return leaderboard;
    }

    private static String chapterNameFor(int gameProgress) {
        if (gameProgress <= 0) {
            return "Not started";
        }

        int chapterIndex = (gameProgress - 1) / CampaignController.STAGES_PER_CHAPTER;
        java.util.List<Chapter> chapters = CampaignController.getInstance().getChapters();
        if (chapterIndex >= chapters.size()) {
            chapterIndex = chapters.size() - 1;
        }

        return chapters.get(chapterIndex).getName();
    }

    private static int stageNumberFor(int gameProgress) {
        if (gameProgress <= 0) {
            return 0;
        }

        int stage = gameProgress % CampaignController.STAGES_PER_CHAPTER;
        return stage == 0 ? CampaignController.STAGES_PER_CHAPTER : stage;
    }
}
