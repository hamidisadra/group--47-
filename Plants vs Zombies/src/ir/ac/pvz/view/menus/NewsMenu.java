package ir.ac.pvz.view.menus;

import ir.ac.pvz.model.user.News;
import ir.ac.pvz.model.user.User;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsMenu extends Menu{

    public NewsMenu() {
        super("News Menu");
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

        else if (command.matches("^menu\\s+news\\s+show-unread$")) {
            showUnread();
        }

        else if (command.matches("^menu\\s+news\\s+show-all$")) {
            showAll();
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
            case "game menu": {
                System.out.println("Entering Game Menu...");
                menuManager.pushMenu(new GameMenu());
                break;
            }

            case "network menu", "settings menu", "profile menu", "main menu": {
                System.out.println("Error: You can't access this menu from News Menu!");
                break;
            }

            case "news menu": {
                System.out.println("You are already in news menu.");
                break;
            }

            default: {
                System.out.println("Invalid menu name!");
                break;
            }
        }
    }

    private void showUnread() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        List<News> unreadNews = user.getUnreadNews();

        if (unreadNews.isEmpty()) {
            System.out.println("You have no unread news.");
        }

        else {
            System.out.println("========== Unread News ==========");
            for (News news : unreadNews) {
                System.out.println("[" + news.getType() + "]   " + news.getMessage() + "    (" + news.getFormattedDate() + ")");
                news.markAsRead();
            }
        }
    }

    private void showAll() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        List<News> allNews = user.getNewsList();

        if (allNews.isEmpty()) {
            System.out.println("You have no news.");
        }

        else {
            System.out.println("========== All News ==========");
            for (News news : allNews) {
                if (news.isRead()) {
                    System.out.println("(NEW) [" + news.getType() + "]   " + news.getMessage() + "    (" + news.getFormattedDate() + ")");
                }
                else {
                    System.out.println("[" + news.getType() + "]   " + news.getMessage() + "    (" + news.getFormattedDate() + ")");
                }
            }
        }
    }
}
