package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.minigame.Beghouled;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BeghouledMenu extends Menu {
    private Beghouled beghouled;

    public BeghouledMenu() {
        super("Beghouled Menu");
        this.beghouled = new Beghouled(1, 5);
        beghouled.startGame();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String swapRegex = "^swap\\s+plants\\s+-a\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s+-b\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$";
        String upgradeRegex = "^upgrade\\s+plant\\s+-t\\s+(\\S+)$";

        Matcher swapMatcher = Pattern.compile(swapRegex).matcher(command);
        Matcher upgradeMatcher = Pattern.compile(upgradeRegex).matcher(command);

        if (swapMatcher.matches()) {
            int x1 = Integer.parseInt(swapMatcher.group(1));
            int y1 = Integer.parseInt(swapMatcher.group(2));
            int x2 = Integer.parseInt(swapMatcher.group(3));
            int y2 = Integer.parseInt(swapMatcher.group(4));
            swap(x1, y1, x2, y2);
        }

        else if (upgradeMatcher.matches()) {
            String plantType = upgradeMatcher.group(1).toLowerCase();
            upgrade(plantType);
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

    private void swap(int x1, int y1, int x2, int y2) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        boolean success = beghouled.swapPlants(x1, y1, x2, y2);
        if (!success) {
            System.out.println("Error: That swap does not create a match!");
            return;
        }

        System.out.println("Matched! Sun amount: " + beghouled.getSunAmount());

        if (beghouled.checkWinCondition()) {
            beghouled.finishGame(true);
            GameplayManager.getInstance().getLeaderboard().getOrCreateEntry(user.getUsername()).addMinigameCompleted();
            menuManager.popMenu();
        }
    }

    private void upgrade(String plantType) {
        boolean upgraded = beghouled.upgradePlant(plantType);
        if (!upgraded) {
            System.out.println("Error: This plant cannot be upgraded right now!");
            return;
        }
        System.out.println("Upgraded all " + plantType + " plants.");
    }
}
