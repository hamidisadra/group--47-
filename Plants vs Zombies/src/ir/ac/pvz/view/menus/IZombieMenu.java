package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.minigame.IZombie;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IZombieMenu extends Menu {
    private IZombie izombie;

    public IZombieMenu() {
        super("I, Zombie Menu");
        this.izombie = new IZombie(1);
        izombie.startGame();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String placeRegex = "^place\\s+zombie\\s+-t\\s+(\\S+)\\s+-r\\s+(\\d+)$";
        Matcher placeMatcher = Pattern.compile(placeRegex).matcher(command);

        if (placeMatcher.matches()) {
            String type = placeMatcher.group(1).toLowerCase();
            int row = Integer.parseInt(placeMatcher.group(2));
            placeZombie(type, row);
        }

        else if (command.matches("^collect\\s+sun$")) {
            izombie.collectSunFromProducers();
            System.out.println("Sun amount: " + izombie.getSunAmount());
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

    private void placeZombie(String type, int row) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        boolean placed = izombie.placeZombie(type, row);
        if (!placed) {
            System.out.println("Error: Not enough sun or invalid zombie type!");
            return;
        }

        System.out.println("Placed " + type + " in row " + row + ".");

        if (izombie.checkWinCondition()) {
            izombie.finishGame(true);
            GameplayManager.getInstance().getLeaderboard().getOrCreateEntry(user.getUsername()).addMinigameCompleted();
            menuManager.popMenu();
        }
    }
}
