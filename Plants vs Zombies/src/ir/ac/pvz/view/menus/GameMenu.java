package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.user.TransactionStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameMenu extends Menu{

    public GameMenu() {
        super("Game Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String enterChapterRegex = "^menu\\s+enter\\s+chapter\\s+-c\\s+(.+)$";
        String enterMenuRegex = "^menu\\s+enter\\s+(.+)$";
        String cheatRegex = "^menu\\s+cheat\\s+add\\s+(\\d+)\\s+(coin|diamond)$";

        Matcher enterChapterMatcher = Pattern.compile(enterChapterRegex).matcher(command);
        Matcher enterMenuMatcher = Pattern.compile(enterMenuRegex).matcher(command);
        Matcher cheatMatcher = Pattern.compile(cheatRegex).matcher(command);

        if (enterChapterMatcher.matches()) {
            String chapterName = enterChapterMatcher.group(1).trim().toLowerCase();
            boolean entered = GameplayManager.getInstance().enterChapter(chapterName);

            if (entered) {
                menuManager.pushMenu(new PlantSelectionMenu());
            } else {
                System.out.println("Invalid chapter name!");
            }
        }

        else if (enterMenuMatcher.matches()) {
            String menuName = enterMenuMatcher.group(1).trim().toLowerCase();

            enterMenu(menuName);
        }

        else if (cheatMatcher.matches()) {
            int amount = Integer.parseInt(cheatMatcher.group(1));
            String type = cheatMatcher.group(2).toLowerCase();

            cheat(amount, type);
        }

        else if (command.matches("^menu\\s+greenhouse$")) {
            System.out.println("Entering Greenhouse...");
            menuManager.pushMenu(new GreenhouseMenu());
        }

        else if (command.matches("^menu\\s+travel-log$")) {
            System.out.println("Entering Travel Log...");
            menuManager.pushMenu(new TravelLogMenu());
        }

        else if (command.matches("^menu\\s+leaderboard$")) {
            System.out.println("Entering Leaderboard...");
            menuManager.pushMenu(new LeaderboardMenu());
        }

        else if (command.matches("^menu\\s+coin-wallet$")) {
            int coins = menuManager.getActiveUser().getWallet().getCoins();
            System.out.println("Showing Coin Wallet: " + coins);
        }

        else if (command.matches("^menu\\s+gem-wallet$")) {
            int gems = menuManager.getActiveUser().getWallet().getGems();
            System.out.println("Showing Gem Wallet: " + gems);
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

    private void enterMenu(String menuName) {
        switch (menuName) {
            case "collection menu": {
                System.out.println("Entering Collection Menu...");
                menuManager.pushMenu(new CollectionMenu());
                break;
            }

            case "settings menu", "network menu", "news menu", "profile menu", "main menu": {
                System.out.println("Error: You can't access this menu from Game Menu!");
                break;
            }

            case "game menu": {
                System.out.println("You are already in game menu.");
                break;
            }

            default: {
                System.out.println("Invalid menu name!");
                break;
            }
        }
    }

    private void cheat(int amount, String type) {
        if (menuManager.getActiveUser() == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        TransactionStatus status;

        if (type.equals("coin")) {
            status = menuManager.getActiveUser().getWallet().addCoins(amount);
        }
        else {
            status = menuManager.getActiveUser().getWallet().addGems(amount);
        }

        if (status == TransactionStatus.SUCCESS) {
            System.out.println("Added " + amount + " " + type + "(s) successfully.");
        }
        else if (status == TransactionStatus.INVALID_AMOUNT) {
            System.out.println("Error: The amount must be grater than 0!");
        }
    }
}