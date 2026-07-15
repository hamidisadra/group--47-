package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.minigame.Vase;
import ir.ac.pvz.model.minigame.Vasebreaker;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VasebreakerMenu extends Menu {
    private Vasebreaker vasebreaker;

    public VasebreakerMenu() {
        super("Vasebreaker Menu");
        this.vasebreaker = new Vasebreaker(1, 4, 5);
        vasebreaker.startGame();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String breakRegex = "^break\\s+vase\\s+-l\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$";
        Matcher breakMatcher = Pattern.compile(breakRegex).matcher(command);

        if (breakMatcher.matches()) {
            int x = Integer.parseInt(breakMatcher.group(1));
            int y = Integer.parseInt(breakMatcher.group(2));
            breakVase(x, y);
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

    private void breakVase(int x, int y) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        Vase vase = vasebreaker.getVase(x, y);
        if (vase == null) {
            System.out.println("Error: Invalid vase position!");
            return;
        }
        if (vase.isBroken()) {
            System.out.println("Error: This vase is already broken!");
            return;
        }

        String content = vasebreaker.breakVase(vase, user.getCollection().getUnlockedPlants());
        System.out.println("The vase breaks open: " + content);

        if (vasebreaker.allVasesBroken()) {
            vasebreaker.finishGame(true);
            GameplayManager.getInstance().getLeaderboard().getOrCreateEntry(user.getUsername()).addMinigameCompleted();
            menuManager.popMenu();
        }
    }
}
