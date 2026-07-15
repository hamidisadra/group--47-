package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;

public class ChapterMenu extends Menu {

    public ChapterMenu() {
        super("Chapter Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        if (command.matches("^show\\s+map$")) {
            GameplayManager.getInstance().getBoard().showMap();
        }

        else if (command.matches("^start\\s+zombie\\s+waves$")) {
            boolean started = GameplayManager.getInstance().startZombieWaves();
            if (!started) {
                System.out.println("Error: No active stage found!");
            }
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
}
