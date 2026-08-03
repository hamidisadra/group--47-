package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.user.CollectionStatus;
import ir.ac.pvz.model.user.TransactionStatus;
import ir.ac.pvz.controller.game_core.PlantUpgradeService;
import ir.ac.pvz.model.enums.UpgradeResult;
import ir.ac.pvz.model.support.LevelBasedUpgradeCost;
import ir.ac.pvz.model.support.Upgrade;
import ir.ac.pvz.model.support.ZombieDataRepository;
import ir.ac.pvz.model.support.ZombieDefinition;
import ir.ac.pvz.model.user.PlayerUpgradeWallet;
import ir.ac.pvz.model.user.User;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionMenu extends Menu{

    private UserManager userManager;

    public CollectionMenu() {
        super("Collection Menu");
        this.userManager = UserManager.getInstance();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String enterMenuRegex = "^menu\\s+enter\\s+(.+)$";
        String showPlantRegex = "^menu\\s+collection\\s+show-plant\\s+-p\\s+(.+)$";
        String showZombieRegex = "^menu\\s+collection\\s+show-zombie\\s+-z\\s+(.+)$";
        String upgradePlantRegex = "^menu\\s+collection\\s+upgrade-plant\\s+-p\\s+(.+)$";
        String purchasePlantRegex = "^menu\\s+collection\\s+purchase-plant\\s+-p\\s+(.+)$";

        Matcher enterMenuMatcher = Pattern.compile(enterMenuRegex).matcher(command);
        Matcher showPlantMatcher = Pattern.compile(showPlantRegex).matcher(command);
        Matcher showZombieMatcher = Pattern.compile(showZombieRegex).matcher(command);
        Matcher upgradePlantMatcher = Pattern.compile(upgradePlantRegex).matcher(command);
        Matcher purchasePlantMatcher = Pattern.compile(purchasePlantRegex).matcher(command);

        if (showPlantMatcher.matches()) {
            String plantName = showPlantMatcher.group(1);

            showPlantDetails(plantName);
        }

        else if (showZombieMatcher.matches()) {
            String zombieName = showZombieMatcher.group(1);

            showZombieDetails(zombieName);
        }

        else if (upgradePlantMatcher.matches()) {
            String plantName = upgradePlantMatcher.group(1);

            upgradePlant(plantName);
        }

        else if (purchasePlantMatcher.matches()) {
            String plantName = purchasePlantMatcher.group(1);

            purchasePlant(plantName);
        }

        else if (!handleSimpleCommands(command, enterMenuMatcher)) {
            System.out.println("Invalid command.");
        }
    }

    private boolean handleSimpleCommands(String command, Matcher enterMenuMatcher) {
        if (command.matches("^menu\\s+collection\\s+show-plants$")) {
            showUnlockedPlants();
        }

        else if (command.matches("^menu\\s+collection\\s+show-all-plants$")) {
            showAllPlants();
        }

        else if (command.matches("^menu\\s+collection\\s+show-zombies$")) {
            showSeenZombies();
        }

        else if (command.matches("^menu\\s+collection\\s+show-all-zombies$")) {
            showAllZombies();
        }

        else if (enterMenuMatcher.matches()) {
            String menuName = enterMenuMatcher.group(1).trim().toLowerCase();
            enterMenu(menuName);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else if (command.matches("^menu\\s+exit$")) {
            menuManager.popMenu();
        }

        else {
            return false;
        }

        return true;
    }

    private void showAllZombies() {
        List<ZombieDefinition> zombies = ZombieDataRepository.getInstance().getAll();

        if (zombies.isEmpty()) {
            System.out.println("No zombies are defined in the game.");
            return;
        }

        System.out.println("========== All Zombies ==========");
        for (ZombieDefinition zombie : zombies) {
            System.out.println("- " + zombie.gameType);
        }
    }

    private void showSeenZombies() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        List<String> zombies = user.getCollection().getSeenZombies();

        if (zombies.isEmpty()) {
            System.out.println("You have not seen any zombies.");
            return;
        }

        System.out.println("========== Seen Zombies ==========");
        for (String zombie : zombies) {
            System.out.println("- " + zombie);
        }
    }

    private void showAllPlants() {
        System.out.println("========== All Plants ==========");

        String[] allPlants = Plant.getSpreadsheetTypes();

        if (allPlants != null) {
            for (String name : allPlants) {
                System.out.println("- " + name + " -");
            }
        }
    }

    private void showUnlockedPlants() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        List<String> plants = user.getCollection().getUnlockedPlants();

        if (plants.isEmpty()) {
            System.out.println("You do not have any plants.");
            return;
        }

        System.out.println("========== Your Plants ==========");
        for (String plant : plants) {
            System.out.println("- " + plant);
        }
    }

    private void purchasePlant(String plantName) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (user.getCollection().getUnlockedPlants().contains(plantName)) {
            System.out.println("Error: You already have this plant!");
            return;
        }

        TransactionStatus purchaseStatus = user.getWallet().spendCoins(2000);

        if (purchaseStatus == TransactionStatus.INSUFFICIENT_FUND) {
            System.out.println("Error: Not enough coins!");
            return;
        }

        user.getCollection().unlockPlant(plantName);
        System.out.println("Plant " + plantName + " purchased successfully.");
        userManager.saveAll();
    }

    private void upgradePlant(String plantName) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (!user.getCollection().getUnlockedPlants().contains(plantName)) {
            System.out.println("Error: You do not have this plant!");
            return;
        }

        Plant plant;
        try {
            plant = Plant.createSpreadsheetPlant(0, plantName);
        } catch (Exception exception) {
            System.out.println("Error: Plant not found.");
            return;
        }

        Upgrade.configureFor(plant, plantName);
        plant.level = user.getCollection().getPlantLevel(plantName);

        UpgradeResult result = new PlantUpgradeService().upgrade(plant,
                new PlayerUpgradeWallet(user.getWallet(), user.getInventory()),
                new LevelBasedUpgradeCost());

        reportUpgrade(user, plantName, plant, result);
        userManager.saveAll();
    }

    private void reportUpgrade(User user, String plantName, Plant plant,
                               UpgradeResult result) {
        int nextLevel = plant.level + 1;
        switch (result) {
            case SUCCESS: {
                user.getCollection().setPlantLevel(plantName, plant.level);
                System.out.println("Plant " + plantName
                        + " upgraded to level " + plant.level + ".");
                System.out.println("Damage: " + plant.attackPower
                        + " | Health: " + plant.getBaseHp()
                        + " | Sun cost: " + plant.getCost());
                break;
            }

            case MAX_LEVEL: {
                System.out.println("Error: " + plantName
                        + " is already at its maximum level!");
                break;
            }

            case NOT_ENOUGH_COINS: {
                System.out.println("Error: Not enough coins! You need "
                        + LevelBasedUpgradeCost.coinCostForLevel(nextLevel)
                        + " coins.");
                break;
            }

            case NOT_ENOUGH_SEED_PACKETS: {
                System.out.println("Error: Not enough seed packets! You need "
                        + LevelBasedUpgradeCost.seedPacketCostForLevel(nextLevel)
                        + " packets of " + plantName + ".");
                break;
            }

            default: {
                System.out.println("Error: This plant cannot be upgraded.");
                break;
            }
        }
    }

    private void showZombieDetails(String zombieName) {
        ZombieDefinition definition =
                ZombieDataRepository.getInstance().getByZombieType(zombieName);
        if (definition == null) {
            System.out.println("Error: Zombie not found.");
            return;
        }

        System.out.println(zombieName + " details: ");
        System.out.println("Health:       " + definition.health);
        System.out.println("Speed:        " + definition.speed);
        System.out.println("Eat damage:   " + definition.eatDamagePerSecond
                + " per second");
        System.out.println("Wave cost:    " + definition.waveCost);
        System.out.println("Plant food:   "
                + (definition.canSpawnPlantFood ? "can drop" : "never drops"));
        if (!definition.armorAliases.isEmpty()) {
            System.out.println("Armor:        " + definition.armorAliases);
        }
        if (!definition.abilities.isEmpty()) {
            System.out.println("Abilities:    " + definition.abilities);
        }
    }

    private void showPlantDetails(String plantName) {
        try {
            Plant plant = Plant.createSpreadsheetPlant(0, plantName);

            System.out.println(plantName + " details: ");
            System.out.println("Sun Cost:    " + plant.getCost());
            System.out.println("Base Health: " + plant.getBaseHp());
            System.out.println("Damage:      " + plant.attackPower);
            System.out.println("Cooldown:    " + plant.rechargeTime + "s");
            System.out.println("Category:    " + plant.getCategory());

        } catch (Exception e) {
            System.out.println("Error: Plant not found.");
        }
    }

    private void enterMenu(String menuName) {
        switch (menuName) {
            case "game menu": {
                System.out.println("Entering Game Menu...");
                menuManager.pushMenu(new GameMenu());
                break;
            }

            case "network menu", "settings menu", "profile menu", "main menu", "news menu": {
                System.out.println("Error: You can't access this menu from Collection Menu!");
                break;
            }

            case "collection menu": {
                System.out.println("You are already in collection menu.");
                break;
            }

            default: {
                System.out.println("Invalid menu name!");
                break;
            }
        }
    }
}