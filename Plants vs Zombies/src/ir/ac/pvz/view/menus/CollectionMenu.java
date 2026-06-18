package ir.ac.pvz.view.menus;

import ir.ac.pvz.model.user.CollectionStatus;
import ir.ac.pvz.model.user.TransactionStatus;
import ir.ac.pvz.model.user.User;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionMenu extends Menu{

    public CollectionMenu() {
        super("Collection Menu");
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

        else if (command.matches("^menu\\s+collection\\s+show-plants$")) {
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
            System.out.println("Invalid command.");
        }
    }

    private void showAllZombies() {
        System.out.println("========== All Zombies ==========");
        //should be completed
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
        //should be completed
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

        //should be completed
    }

    private void showZombieDetails(String zombieName) {
        System.out.println(zombieName + " details: ");
        //should be completed
    }

    private void showPlantDetails(String plantName) {
        System.out.println(plantName + " details: ");
        //should be completed
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
