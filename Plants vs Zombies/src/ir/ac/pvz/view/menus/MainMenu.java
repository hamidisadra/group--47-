package ir.ac.pvz.view.menus;

import ir.ac.pvz.view.menus.Menu;
import ir.ac.pvz.view.menus.RegisterMenu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMenu extends Menu {

    public MainMenu() {
        super("Main Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String enterMenuRegex = "^menu\\s+enter\\s+(.+)$";
        Matcher enterMenuMatcher = Pattern.compile(enterMenuRegex).matcher(command);


        if (enterMenuMatcher.matches()) {
            String menuName = enterMenuMatcher.group(1).trim().toLowerCase();

            enterMenu(menuName);
        }

        else if (command.matches("^menu\\s+logout$")) {
            System.out.println("Logging out...");
            menuManager.logoutUser();

            while (menuManager.getActiveMenu() != null) {
                menuManager.popMenu();
            }

            menuManager.pushMenu(new RegisterMenu());
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else {
            System.out.println("Invalid command.");
        }
    }

    private void enterMenu(String menuName) {
        switch (menuName) {
            case "game menu": {
                System.out.println("Entering Game Menu...");
                menuManager.pushMenu(new GameMenu());
                break;
            }

            case "score game menu": {
                System.out.println("Entering Score Game Menu...");
                menuManager.pushMenu(new ScoreGameMenu());
                break;
            }
            case "settings menu": {
                System.out.println("Entering Settings Menu...");
                menuManager.pushMenu(new SettingsMenu());
                break;
            }

            case "network menu": {
                System.out.println("Entering Network Menu...");
                //menuManager.pushMenu(new networkMenu());
                break;
            }

            case "news menu": {
                System.out.println("Entering News Menu...");
                menuManager.pushMenu(new NewsMenu());
                break;
            }

            case "profile menu": {
                System.out.println("Entering Profile Menu...");
                menuManager.pushMenu(new ProfileMenu());
                break;
            }

            case "shop menu", "collection menu": {
                System.out.println("Error: You can't access this menu from Main Menu!");
                break;
            }

            case "login menu", "register menu": {
                System.out.println("Error: You are already logged in!");
                break;
            }

            default: {
                System.out.println("Error: Invalid menu name!");
                break;
            }
        }
    }
}