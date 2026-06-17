package ir.ac.pvz.view.menus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsMenu extends Menu{

    public SettingsMenu() {
        super("Settings Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String difficultyRegex = "^menu\\s+settings\\s+change-difficulty\\s+-l\\s+(\\d+)$";
        String enterMenuRegex = "^menu\\s+enter\\s+(.+)$";

        Matcher difficultyMatcher = Pattern.compile(difficultyRegex).matcher(command);
        Matcher enterMenuMatcher = Pattern.compile(enterMenuRegex).matcher(command);

        if (difficultyMatcher.matches()) {
            int level = Integer.parseInt(difficultyMatcher.group(1));

            changeDifficulty(level);
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

    private void changeDifficulty(int level) {
        if (menuManager.getActiveUser() == null) {
            System.out.println("Error: No active user found!");
            return;
        }
        
        boolean success = menuManager.getActiveUser().setDifficultyLevel(level);

        if (!success) {
            System.out.println("Error: Invalid difficulty level!");
        }
        else {
            System.out.println("Difficulty level successfully changed to " + level + ".");
        }
    }

    private void enterMenu(String menuName) {
        switch (menuName) {
            case "game menu": {
                System.out.println("Entering Collection Menu...");
                menuManager.pushMenu(new GameMenu());
                break;
            }

            case "network menu", "news menu", "profile menu", "main menu": {
                System.out.println("Error: You can't access this menu from Game Menu!");
                break;
            }

            case "settings menu": {
                System.out.println("You are already in setting menu.");
                break;
            }

            default: {
                System.out.println("Invalid menu name!");
                break;
            }
        }
    }
}
