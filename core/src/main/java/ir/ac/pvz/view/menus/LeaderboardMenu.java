package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.travel.Leaderboard;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeaderboardMenu extends Menu {

    public LeaderboardMenu() {
        super("Leaderboard Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String sortRegex = "^leaderboard\\s+sort\\s+-c\\s+(\\S+)\\s+-o\\s+(asc|desc)$";
        Matcher sortMatcher = Pattern.compile(sortRegex).matcher(command);

        if (command.matches("^leaderboard\\s+show$")) {
            show();
        }

        else if (sortMatcher.matches()) {
            String column = sortMatcher.group(1);
            boolean ascending = sortMatcher.group(2).equals("asc");
            sortBy(column, ascending);
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

    private void show() {
        Leaderboard leaderboard = GameplayManager.getInstance().getLeaderboard();
        registerActiveUser(leaderboard);
        leaderboard.display();
    }

    private void sortBy(String column, boolean ascending) {
        Leaderboard leaderboard = GameplayManager.getInstance().getLeaderboard();
        registerActiveUser(leaderboard);
        leaderboard.sortBy(column, ascending);
        leaderboard.display();
    }

    private void registerActiveUser(Leaderboard leaderboard) {
        User user = menuManager.getActiveUser();
        if (user != null) {
            leaderboard.getOrCreateEntry(user.getUsername());
        }
    }
}
