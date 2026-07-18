package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.menus.MainMenu;
import ir.ac.pvz.view.menus.Menu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginMenu extends Menu {
    private UserManager userManager;
    private User pendingUser;
    private boolean forgetPasswordProcessing;
    private boolean settingPassword;

    public LoginMenu() {
        super("Login Menu");
        this.userManager = UserManager.getInstance();
        this.pendingUser = null;
        this.forgetPasswordProcessing = false;
        this.settingPassword = false;
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        if (command.matches("^menu\\s+exit$")) {
            menuManager.popMenu();
            return;
        }

        if (forgetPasswordProcessing) {
            if (command.matches("^answer\\s+-a\\s+(.+)$")) {
                answerQuestion(command);
            }
            else {
                System.out.println("Error: Answer the security questions!");
            }
            return;
        }

        if (settingPassword) {
            setPassword(command);
            return;
        }

        String loginRegex = "^login\\s+-u\\s+(\\S+)\\s+-p\\s+(\\S+)(\\s+-stay-logged-in)?$";
        String forgetPasswordRegex = "^forget\\s+password\\s+-u\\s+(\\S+)\\s+-e\\s+(\\S+)$";

        Matcher loginMatcher = Pattern.compile(loginRegex).matcher(command);
        Matcher forgetPasswordMatcher = Pattern.compile(forgetPasswordRegex).matcher(command);

        if (loginMatcher.matches()) {

            String username = loginMatcher.group(1);
            String password = loginMatcher.group(2);
            boolean stayLoggedIn = loginMatcher.group(3) != null;

            loginProcess(username, password, stayLoggedIn);
        }

        else if (forgetPasswordMatcher.matches()) {
            String username = forgetPasswordMatcher.group(1);
            String email = forgetPasswordMatcher.group(2);

            forgetPasswordProcess(username, email);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else {
            System.out.println("Invalid command.");
        }
    }

    private void loginProcess(String username, String password, boolean stayLoggedIn) {

        if (!userManager.validateUsername(username)) {
            System.out.println("Error: Invalid username format!");
            return;
        }

        User user = userManager.findUserByUsername(username);

        if (user == null) {
            System.out.println("Error: There is no user with this username.");
            return;
        }

        String passwordStatus = userManager.validatePassword(password);
        if (!passwordStatus.equals("Valid")) {
            System.out.println("Error: Invalid password format!");
            return;
        }

        String hashedPassword = userManager.hashPassword(password);
        if (!user.getPasswordHash().equals(hashedPassword)) {
            System.out.println("Error: Incorrect password!");
            return;
        }

        System.out.println("Logged in successfully!");

        userManager.clearLoggedIn();

        if (stayLoggedIn) {
            user.setStayLoggedIn(true);
            System.out.println("Stay-logged-in feature is activated.");
        }

        menuManager.loginUser(user);
        userManager.saveAll();
        menuManager.pushMenu(new MainMenu());
    }

    private void forgetPasswordProcess(String username, String email) {

        if (!userManager.validateUsername(username)) {
            System.out.println("Error: Invalid username format!");
            return;
        }

        User user = userManager.findUserByUsername(username);

        if (user == null) {
            System.out.println("Error: There is no user with this username.");
            return;
        }

        if (!userManager.validateEmail(email)) {
            System.out.println("Error: Invalid email format!");
            return;
        }

        if (!user.getEmail().equals(email)) {
            System.out.println("Error: Incorrect email!");
            return;
        }

        this.pendingUser = user;
        this.forgetPasswordProcessing = true;

        System.out.println(Questions.getQuestionsList().get(user.getSecurityQuestionId() - 1));
    }

    private void answerQuestion(String command) {
        String answer = command.replaceFirst("^answer\\s+-a\\s+", "").trim();

        if (!answer.equals(pendingUser.getSecurityAnswer())) {
            System.out.println("Error: Answer is not correct!");
            this.pendingUser = null;
            this.forgetPasswordProcessing = false;
            return;
        }

        System.out.println("Enter your new password : ");
        this.settingPassword = true;
        this.forgetPasswordProcessing = false;
    }


    private void setPassword(String password) {
        String passwordStatus = userManager.validatePassword(password);

        if (!passwordStatus.equals("Valid")) {
            System.out.println(passwordStatus);
            System.out.println("Enter your new password : ");
            return;
        }

        pendingUser.setPasswordHash(userManager.hashPassword(password));
        System.out.println("Password changed successfully");

        this.settingPassword = false;
        this.pendingUser = null;
    }
}