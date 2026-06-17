package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.PlayerWallet;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileMenu extends Menu{

    private UserManager userManager;

    public ProfileMenu() {
        super("Profile Menu");
        userManager = UserManager.getInstance();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String enterMenuRegex = "^menu\\s+enter\\s+(.+)$";
        String changeUsernameRegex = "^menu\\s+profile\\s+change-username\\s+-u\\s+(\\S+)$";
        String changeNickNameRegex = "^menu\\s+profile\\s+change-nickname\\s+-u\\s+(\\S+)$";
        String changeEmailRegex = "^menu\\s+profile\\s+change-email\\s+-e\\s+(\\S+)$";
        String changePasswordRegex = "^menu\\s+profile\\s+change-password\\s+-p\\s+(\\S+)\\s+-o\\s+(\\S+)$";

        Matcher enterMenuMatcher = Pattern.compile(enterMenuRegex).matcher(command);
        Matcher changeUsernameMatcher = Pattern.compile(changeUsernameRegex).matcher(command);
        Matcher changeNicknameMatcher = Pattern.compile(changeNickNameRegex).matcher(command);
        Matcher changeEmailMatcher = Pattern.compile(changeEmailRegex).matcher(command);
        Matcher changePasswordMatcher = Pattern.compile(changePasswordRegex).matcher(command);

        if (changeUsernameMatcher.matches()) {
            String username = changeUsernameMatcher.group(1);

            changeUsername(username);
        }

        else if (changeNicknameMatcher.matches()) {
            String nickname = changeNicknameMatcher.group(1);

            changeNickname(nickname);
        }

        else if (changeEmailMatcher.matches()) {
            String email = changeEmailMatcher.group(1);

            changeEmail(email);
        }

        else if (changePasswordMatcher.matches()) {
            String newPassword = changePasswordMatcher.group(1);
            String oldPassword = changePasswordMatcher.group(2);

            changePassword(newPassword, oldPassword);
        }

        else if (command.matches("^menu\\s+profile\\s+show-info$")) {
            showInfo();
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

    private void enterMenu(String menuName) {
        switch (menuName) {
            case "game menu": {
                System.out.println("Entering Game Menu...");
                menuManager.pushMenu(new GameMenu());
                break;
            }

            case "network menu", "settings menu", "news menu", "main menu": {
                System.out.println("Error: You can't access this menu from Profile Menu!");
                break;
            }

            case "profile menu": {
                System.out.println("You are already in profile menu.");
                break;
            }

            default: {
                System.out.println("Invalid menu name!");
                break;
            }
        }
    }

    private void changeUsername(String username) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("No active user found!");
            return;
        }

        if (!userManager.validateUsername(username)) {
            System.out.println("Error: Invalid username!");
            return;
        }

        if (user.getUsername().equals(username)) {
            System.out.println("Error: You entered your current username!");
            return;
        }

        user.setUsername(username);
        System.out.println("Username changed successfully.");
    }

    private void changeNickname(String nickname) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("No active user found!");
            return;
        }

        if (!userManager.validateNickname(nickname)) {
            System.out.println("Error: Invalid nickname!");
            return;
        }

        if (user.getNickName().equals(nickname)) {
            System.out.println("Error: You entered your current nickname!");
            return;
        }

        user.setNickName(nickname);
        System.out.println("Nickname changed successfully.");
    }

    private void changeEmail(String email) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("No active user found!");
            return;
        }

        if (!userManager.validateEmail(email)) {
            System.out.println("Error: Invalid email!");
            return;
        }

        if (user.getEmail().equals(email)) {
            System.out.println("Error: You entered your current email!");
            return;
        }

        user.setEmail(email);
        System.out.println("Email changed successfully.");
    }

    private void changePassword(String newPassword, String oldPassword) {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("No active user found!");
            return;
        }

        String oldPasswordStatus = userManager.validatePassword(oldPassword);

        if (!oldPasswordStatus.equals("Valid")) {
            System.out.println("[Old Password] Error: " + oldPasswordStatus);
            return;
        }

        String oldPasswordHash = userManager.hashPassword(oldPassword);

        if (!user.getPasswordHash().equals(oldPasswordHash)) {
            System.out.println("Error: You entered your current password wrong!");
            return;
        }

        if (oldPassword.equals(newPassword)) {
            System.out.println("Error: New password cannot be the same as the old password!");
            return;
        }

        String newPasswordStatus = userManager.validatePassword(newPassword);

        if (!newPasswordStatus.equals("Valid")) {
            System.out.println("[New Password] Error: " + newPasswordStatus);
            return;
        }

        String newPasswordHash = userManager.hashPassword(newPassword);

        user.setPasswordHash(newPasswordHash);
        System.out.println("Password changed successfully.");
    }

    private void showInfo() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("No active user found!");
            return;
        }

        PlayerWallet wallet = user.getWallet();

        System.out.println("========== Your Info ==========");
        System.out.println("Username:   " + user.getUsername());
        System.out.println("Nickname:   " + user.getNickName());
        System.out.println("Total game number:  " + user.getGamesCount());
        System.out.println("Coins:     " + wallet.getCoins());
        System.out.println("Gems:       " + wallet.getGems());
        System.out.println("Game Progress:  " + user.getGameProgress());
        System.out.println("Max MUPoint:    " + user.getMaxMuPoint());
    }
}
