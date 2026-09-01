package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.User;

final class SessionOutcome {
    void awardVictory(SessionController controller) {
        User user = MenuManager.getInstance().getActiveUser();

        if (user == null) {
            return;
        }

        SessionRewardService.awardVictory(user, controller.getChapter(),
            controller.getStage(), controller.getMinigame(),
            controller.getMinigameLevel(), controller.boostedThisStage);

        QuestProgressService.advance(user, controller.getChapter(),
            controller.getStage(), controller.getMinigame(),
            controller.getSession());

        recordScore(controller, user);
        UserManager.getInstance().saveAll();
    }

    private void recordScore(SessionController controller, User user) {
        controller.lastScore = controller.computeScore();
        SessionRewardService.recordScore(user, controller.lastScore);
    }

    void recordAttempt(SessionController controller) {
        User user = MenuManager.getInstance().getActiveUser();

        if (user != null) {
            QuestProgressService.advance(user, controller.getChapter(),
                controller.getStage(), controller.getMinigame(),
                controller.getSession());
            SessionRewardService.recordAttempt(user,
                controller.boostedThisStage);
        }
    }
}
