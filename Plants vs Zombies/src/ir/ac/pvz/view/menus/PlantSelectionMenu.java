package ir.ac.pvz.view.menus;

import ir.ac.pvz.model.user.CollectionStatus;
import ir.ac.pvz.model.user.TransactionStatus;
import ir.ac.pvz.model.user.User;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlantSelectionMenu extends Menu{

    public PlantSelectionMenu() {
        super("Plant Selection Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String enterMenuRegex = "^menu\\s+enter\\s+(.+)$";
        String addPlantRegex = "^add\\s+plant\\s+-t\\s+(.+)$";
        String removePlantRegex = "^remove\\s+plant\\s+-t\\s+(.+)$";
        String boostRegex = "^boost\\s+plant\\s+-t\\s+(.+)$";

        Matcher enterMenuMatcher = Pattern.compile(enterMenuRegex).matcher(command);
        Matcher addPlantMatcher = Pattern.compile(addPlantRegex).matcher(command);
        Matcher removePlantMatcher = Pattern.compile(removePlantRegex).matcher(command);
        Matcher boostMatcher = Pattern.compile(boostRegex).matcher(command);

        if (addPlantMatcher.matches()) {
            String plantType = addPlantMatcher.group(1);

            addPlant(plantType);
        }

        else if (removePlantMatcher.matches()) {
            String plantType = removePlantMatcher.group(1);

            removePlant(plantType);
        }

        else if (boostMatcher.matches()) {
            String plantType = boostMatcher.group(1);

            boostPlant(plantType);
        }

        else if (command.matches("^show\\s+all\\s+plants$")) {
            showAllPlants();
        }

        else if (command.matches("^show\\s+available\\s+plants$")) {
            showAvailablePlants();
        }

        else if (command.matches("^start\\s+game$")) {
            startGame();
        }

        else if (enterMenuMatcher.matches()) {
            String menuName = enterMenuMatcher.group(1).trim().toLowerCase();
            enterMenu(menuName);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else if (command.matches("^menu\\s+exit$")) {
            exitMenu();
        }

        else {
            System.out.println("Invalid command.");
        }
    }

    private void exitMenu() {
        User user = menuManager.getActiveUser();
        
        if (user != null) {
            user.getCollection().clearSelection();
        }
        menuManager.popMenu();
    }

    private void startGame() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (user.getCollection().getSelectedPlants().isEmpty()) {
            System.out.println("Error: You must select at least one plant to start the game!");
            return;
        }

        System.out.println("Starting the game...");
        //should be completed
    }

    private void showAvailablePlants() {
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

    private void showAllPlants() {
        System.out.println("========== All Plants ==========");
        //should be completed
    }

    private void boostPlant(String plantType) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (!user.getCollection().getUnlockedPlants().contains(plantType)) {
            System.out.println("Error: You must unlock this plant first!");
            return;
        }

        if (user.getCollection().getBoostedPlants().contains(plantType)) {
            System.out.println("Error: This plant is already boosted!");
            return;
        }

        TransactionStatus purchaseStatus = user.getWallet().spendGems(2);

        if (purchaseStatus == TransactionStatus.INSUFFICIENT_FUND) {
            System.out.println("Error: You don't have enough gems to boost!");
            return;
        }

        user.getCollection().boostPlant(plantType);

        System.out.println("Plant " + plantType + " boosted successfully.");
    }

    private void removePlant(String plantType) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        CollectionStatus removeStatus = user.getCollection().removePlant(plantType);

        switch (removeStatus) {
            case PLANT_NOT_SELECTED: {
                System.out.println("Error: You have not selected this plant!");
                break;
            }

            case SUCCESS: {
                System.out.println("Plant " + plantType + " removed successfully.");
                break;
            }

            default: {
                System.out.println("An unknown error occurred!");
                break;
            }
        }
    }

    private void addPlant(String plantType) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        CollectionStatus addStatus = user.getCollection().selectPlant(plantType);

        switch (addStatus) {
            case PLANT_NOT_UNLOCKED: {
                System.out.println("Error: You have not unlocked this plant!");
                break;
            }

            case PLANT_ALREADY_SELECTED: {
                System.out.println("Error: You already selected this plant!");
                break;
            }

            case CAPACITY_IS_FULL: {
                System.out.println("Error: Your selection capacity is full!");
                break;
            }

            case SUCCESS: {
                System.out.println("Plant " + plantType + " selected successfully.");
                break;
            }

            default: {
                System.out.println("An unknown error occurred!");
                break;
            }
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
                System.out.println("Error: You can't access this menu from Plant Selection Menu!");
                break;
            }

            case "plant selection menu": {
                System.out.println("You are already in plant selection menu.");
                break;
            }

            default: {
                System.out.println("Invalid menu name!");
                break;
            }
        }
    }
}
