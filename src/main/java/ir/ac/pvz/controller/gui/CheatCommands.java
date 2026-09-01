package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.user.User;

final class CheatCommands {
    private static final int SUN_AMOUNT = 500;
    private static final int COIN_AMOUNT = 1000;
    private static final int GEM_AMOUNT = 100;

    private CheatCommands() {
    }

    static void addSun(GameSession session) {
        session.getSunManager().addSuns(SUN_AMOUNT);
        session.refreshPublicState();
    }

    static void addPlantFood(GameSession session) {
        session.getPlantFoodInventory().cheatAddPlantFood();
        session.refreshPublicState();
    }

    static void addCoins() {
        User user = MenuManager.getInstance().getActiveUser();

        if (user != null) {
            user.getWallet().addCoins(COIN_AMOUNT);
            UserManager.getInstance().saveAll();
        }
    }

    static void addGems() {
        User user = MenuManager.getInstance().getActiveUser();

        if (user != null) {
            user.getWallet().addGems(GEM_AMOUNT);
            UserManager.getInstance().saveAll();
        }
    }
}
