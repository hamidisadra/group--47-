package ir.ac.pvz.controller.managers;

import ir.ac.pvz.model.shop.Shop;

public class GameplayManager {
    private static GameplayManager instance;

    private final Shop shop;

    private GameplayManager() {
        this.shop = new Shop();
    }

    public static GameplayManager getInstance() {
        if (instance == null) {
            instance = new GameplayManager();
        }

        return instance;
    }

    public Shop getShop() {
        return shop;
    }
}
