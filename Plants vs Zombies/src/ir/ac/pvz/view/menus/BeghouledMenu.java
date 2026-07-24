package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.minigame.Beghouled;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.model.zombies.BeghouledZombie;

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
        String advanceRegex = "^advance\\s+time\\s+-t\\s+(\\d+)\\s+ticks$";

        Matcher swapMatcher = Pattern.compile(swapRegex).matcher(command);
        Matcher upgradeMatcher = Pattern.compile(upgradeRegex).matcher(command);
        Matcher advanceMatcher = Pattern.compile(advanceRegex).matcher(command);

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

        else if (advanceMatcher.matches()) {
            advance(Integer.parseInt(advanceMatcher.group(1)));
        }

        else if (command.matches("^show\\s+grid$")) {
            showGrid();
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

        if (!isInside(x1, y1) || !isInside(x2, y2)) {
            System.out.println("Error: Those coordinates are outside the garden!");
            return;
        }

        int sunBefore = beghouled.getSunAmount();
        boolean success = beghouled.swapPlants(x1, y1, x2, y2);

        if (!success) {
            System.out.println("Error: That swap does not create a match!");
            return;
        }

        System.out.println("Matched! You earned "
                + (beghouled.getSunAmount() - sunBefore) + " sun. Sun amount: "
                + beghouled.getSunAmount());
        System.out.println("Matches: " + beghouled.getMatchCount() + " / "
                + beghouled.getTargetMatches());

        if (beghouled.checkWinCondition()) {
            System.out.println("All zombies vanish. You cleared this Beghouled stage!");
            beghouled.finishGame(true);
            GameplayManager.getInstance().getLeaderboard()
                    .getOrCreateEntry(user.getUsername()).addMinigameCompleted();
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

    private void advance(int ticks) {
        beghouled.advanceTime(ticks);
        if (beghouled.isLost()) {
            menuManager.popMenu();
        }
    }

    private void showGrid() {
        System.out.println("sun: " + beghouled.getSunAmount()
                + " | matches: " + beghouled.getMatchCount() + " / "
                + beghouled.getTargetMatches());

        String[][] grid = beghouled.getGrid();

        for (int y = 1; y <= grid.length; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 1; x <= grid[y - 1].length; x++) {
                if (beghouled.isCrater(x, y)) {
                    line.append("[  crater  ]");
                }

                else {
                    line.append("[").append(pad(grid[y - 1][x - 1])).append("]");
                }
            }
            line.append(zombiesInRow(y));
            System.out.println(line.toString());
        }
    }

    private String zombiesInRow(int row) {
        StringBuilder zombiesText = new StringBuilder();

        for (BeghouledZombie zombie : beghouled.getZombies()) {
            if (zombie.getLane() == row) {
                zombiesText.append(" Z(").append(zombie.getColumn()).append(")");
            }
        }
        return zombiesText.toString();
    }

    private String pad(String text) {
        String value = text == null ? "" : text;

        while (value.length() < 10) {
            value = value + " ";
        }
        return value.substring(0, 10);
    }

    private boolean isInside(int x, int y) {
        return x >= 1 && x <= 5 && y >= 1 && y <= 5;
    }
}