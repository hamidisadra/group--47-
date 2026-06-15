package ir.ac.pvz.view;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.User;

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

        String loginRegex = "^login\\s+-u\\s+(\\S+)\\s+-p\\s+(\\S+)(?:\\s+-stay-logged-in)?$";
        String forgetPasswordRegex = "^forget\\s+password\\s+-u\\s+(\\S+)\\s+-e\\s+(\\S+)$";

        if (command.matches(loginRegex)) {
            loginProcess(command);
        }

        else if (command.matches(forgetPasswordRegex)) {
            forgetPasswordProcess(command);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else {
            System.out.println("Invalid command.");
        }
    }

    private void loginProcess(String command) {
        String[] tokens = command.split("\\s+");

        String username = tokens[2];
        String password = tokens[4];

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

        menuManager.loginUser(user);
        userManager.saveAll();
    }

    private void forgetPasswordProcess(String command) {
        String[] tokens = command.split("\\s+");

        String username = tokens[3];
        String email = tokens[5];

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
        String answer = command.substring(command.indexOf("-a") + 1).trim();

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
