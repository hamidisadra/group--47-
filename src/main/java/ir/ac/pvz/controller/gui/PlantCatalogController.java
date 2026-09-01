package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.game_core.PlantUpgradeService;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.UpgradeResult;
import ir.ac.pvz.model.support.LevelBasedUpgradeCost;
import ir.ac.pvz.model.support.Upgrade;
import ir.ac.pvz.model.user.Collection;
import ir.ac.pvz.model.user.CollectionStatus;
import ir.ac.pvz.model.user.NewsType;
import ir.ac.pvz.model.user.PlayerUpgradeWallet;
import ir.ac.pvz.model.user.User;

import java.util.ArrayList;
import java.util.List;

public final class PlantCatalogController {
    public static final int UNLOCK_COST_COINS = 2000;
    public static final int UNLOCK_COST_GEMS = 8;

    private static final java.util.Set<String> PREMIUM_PLANTS =
        new java.util.LinkedHashSet<>(java.util.Arrays.asList(
            "Squash", "Jalapeno", "Torchwood", "Sweet Potato",
            "Fire Peashooter", "Imitater", "Hot Potato", "Primal Potato Mine"));

    private static final java.util.Set<String> PREMIUM_ZOMBIES =
        new java.util.LinkedHashSet<>(java.util.Arrays.asList(
            "Gargantuar", "FootballZombie", "PianistZombie", "ArcadeZombie",
            "TurquoiseZombie", "ImpDragon", "KingZombie"));

    public static final int BOOST_COST_GEMS = 2;

    private static final LevelBasedUpgradeCost UPGRADE_COSTS = new LevelBasedUpgradeCost();

    private static PlantCatalogController instance;
    private final List<String> allTypes = new ArrayList<>();

    private PlantCatalogController() {
        for (String type : Plant.getSpreadsheetTypes()) {
            allTypes.add(type);
        }
    }

    public static PlantCatalogController getInstance() {
        if (instance == null) {
            instance = new PlantCatalogController();
        }

        return instance;
    }

    public List<String> allTypes() {
        return new ArrayList<>(allTypes);
    }

    public Plant prototype(String type) {
        Plant plant = Plant.createSpreadsheetPlant(0, type);
        if (plant == null) {
            return null;
        }

        int level = level(type);
        for (int next = plant.level + 1; next <= level; next++) {
            int before = plant.level;
            plant.upgrade();
            if (plant.level == before) {
                break;
            }
        }

        return plant;
    }

    public boolean isUnlocked(String type) {
        Collection collection = collection();
        return collection != null && collection.getUnlockedPlants().contains(type);
    }

    public int level(String type) {
        Collection collection = collection();
        return collection == null ? 1 : Math.max(1, collection.getPlantLevel(type));
    }

    public boolean isBoosted(String type) {
        Collection collection = collection();
        return collection != null && collection.getBoostedPlants().contains(type);
    }

    public int seedPackets(String type) {
        User user = user();
        return user == null ? 0 : user.getInventory().getSeedPacketCount(type);
    }

    public int seedPacketsForNextLevel(String type) {
        if (!hasNextLevel(type)) {
            return 0;
        }

        return LevelBasedUpgradeCost.seedPacketCostForLevel(level(type) + 1);
    }

    public int coinsForNextLevel(String type) {
        if (!hasNextLevel(type)) {
            return 0;
        }

        return LevelBasedUpgradeCost.coinCostForLevel(level(type) + 1);
    }

    private boolean hasNextLevel(String type) {
        Plant plant = Plant.createSpreadsheetPlant(0, type);
        if (plant == null) {
            return false;
        }

        int nextLevel = level(type) + 1;
        for (Upgrade upgrade : plant.levelUpgrades) {
            if (upgrade.level == nextLevel) {
                return true;
            }
        }

        return false;
    }

    public boolean canUpgrade(String type) {
        if (!isUnlocked(type) || !hasNextLevel(type)) {
            return false;
        }

        User user = user();
        if (user == null) {
            return false;
        }

        return seedPackets(type) >= seedPacketsForNextLevel(type)
            && user.getWallet().getCoins() >= coinsForNextLevel(type);
    }

    public PlantCategory family(String type) {
        Plant plant = Plant.createSpreadsheetPlant(0, type);
        return plant == null ? null : plant.category;
    }

    public boolean isPremium(String type) {
        return type != null && PREMIUM_PLANTS.contains(type);
    }

    public static boolean isPremiumZombie(String gameType) {
        return gameType != null && PREMIUM_ZOMBIES.contains(gameType);
    }

    public String unlock(String type) {
        User user = user();
        if (user == null) {
            return "You are not signed in.";
        }

        if (isUnlocked(type)) {
            return "You already own this plant.";
        }

        if (isPremium(type)) {
            return unlockPremium(user, type);
        }

        if (user.getWallet().getCoins() < UNLOCK_COST_COINS) {
            return "You need " + UNLOCK_COST_COINS + " coins to unlock this plant.";
        }

        user.getWallet().spendCoins(UNLOCK_COST_COINS);
        CollectionStatus status = user.getCollection().unlockPlant(type);
        if (status != CollectionStatus.SUCCESS) {
            user.getWallet().addCoins(UNLOCK_COST_COINS);
            return "This plant could not be unlocked.";
        }

        user.addNews("New plant unlocked: " + type + ".", NewsType.PLANT_UNLOCK);
        UserManager.getInstance().saveAll();
        return null;
    }

    private String unlockPremium(User user, String type) {
        if (user.getWallet().getGems() < UNLOCK_COST_GEMS) {
            return "This is a premium plant. You need "
                + UNLOCK_COST_GEMS + " gems to unlock it.";
        }

        user.getWallet().spendGems(UNLOCK_COST_GEMS);
        CollectionStatus status = user.getCollection().unlockPlant(type);
        if (status != CollectionStatus.SUCCESS) {
            user.getWallet().addGems(UNLOCK_COST_GEMS);
            return "This plant could not be unlocked.";
        }

        user.addNews("Premium plant unlocked: " + type + ".", NewsType.PLANT_UNLOCK);
        UserManager.getInstance().saveAll();
        return null;
    }

    public String upgrade(String type) {
        User user = user();
        if (user == null) {
            return "You are not signed in.";
        }

        if (!isUnlocked(type)) {
            return "Unlock this plant first.";
        }

        Plant plant = prototype(type);
        if (plant == null) {
            return "This plant cannot be upgraded.";
        }

        PlayerUpgradeWallet wallet = new PlayerUpgradeWallet(user.getWallet(),
            user.getInventory());
        UpgradeResult result = new PlantUpgradeService()
            .upgrade(plant, wallet, UPGRADE_COSTS);
        switch (result) {
            case SUCCESS:
                user.getCollection().setPlantLevel(type, plant.level);
                UserManager.getInstance().saveAll();
                return null;
            case MAX_LEVEL:
                return "This plant is already at its highest level.";
            case NOT_ENOUGH_COINS:
                return "You do not have enough coins.";
            case NOT_ENOUGH_SEED_PACKETS:
                return "You do not have enough seed packets.";
            default:
                return "No upgrade cost is configured for this plant.";
        }
    }

    public String toggleBoost(String type) {
        User user = user();
        if (user == null) {
            return "You are not signed in.";
        }

        Collection collection = user.getCollection();
        if (collection.getBoostedPlants().contains(type)) {
            collection.removeBoost(type);
            user.getWallet().addGems(BOOST_COST_GEMS);
            UserManager.getInstance().saveAll();
            return null;
        }

        if (user.getWallet().getGems() < BOOST_COST_GEMS) {
            return "Boosting a plant costs " + BOOST_COST_GEMS + " gems.";
        }

        user.getWallet().spendGems(BOOST_COST_GEMS);
        collection.boostPlant(type);
        UserManager.getInstance().saveAll();
        return null;
    }

    private User user() {
        return MenuManager.getInstance().getActiveUser();
    }

    private Collection collection() {
        User user = user();
        return user == null ? null : user.getCollection();
    }
}
