package ir.ac.pvz.view;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.Gender;

public class RegisterMenu extends Menu{

    private UserManager userManager;
    private boolean isUserRegistered;

    private String pendingUsername, pendingPassword, pendingNickname, pendingEmail;
    private Gender pendingGender;

    public RegisterMenu() {
        super("Register Menu");
        this.userManager = UserManager.getInstance();
        this.isUserRegistered = false;
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        if (command.matches("^menu\\s+exit$")) {
            menuManager.popMenu();
            return;
        }

        String registerRegex = "^register\\s+-u\\s+(\\S+)\\s+-p\\s+(\\S+)\\s+(\\S+)\\s+-n\\s+(\\S+)\\s+-e\\s+(\\S+)\\s+-g\\s+(\\S+)$";
        String pickQuestionRegex = "^pick\\s+question\\s+-q\\s+(\\d+)\\s+-a\\s+(\\S+)\\s+-c\\s+(\\S+)$";

        if (command.matches(registerRegex)) {
            String[] tokens = command.split("\\s+");

            String username = tokens[2];
            String password = tokens[4];
            String passwordConfirm = tokens[5];
            String nickName = tokens[7];
            String email = tokens[9];
            String gender = tokens[11];

            processRegisterCommand(username, password, passwordConfirm, nickName, email, gender);
        }

        else if (command.matches(pickQuestionRegex)) {
            String[] tokens = command.split("\\s+");

            String questionIdString = tokens[3];
            String answer = tokens[5];
            String answerConfirm = tokens[7];

            processPickQuestionCommand(questionIdString, answer, answerConfirm);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else if (command.matches("^menu\\s+enter\\s+login\\s+menu$")) {
            menuManager.pushMenu(new LoginMenu());
        }

        else System.out.println("Invalid command.");

    }

    private void processRegisterCommand(String username, String password, String passwordConfirm, String nickName, String email, String gender) {
        if (!userManager.validateUsername(username)) {
            System.out.println("Error: Invalid username.");
            return;
        }

        if (userManager.isUsernameTaken(username)) {
            System.out.println("Error: Username is already taken!");
            return;
        }

        if (!password.equals(passwordConfirm)) {
            System.out.println("ERROR: password do not match!");
            return;
        }

        String passwordStatus = userManager.validatePassword(password);
        if (!passwordStatus.equals("Valid")) {
            System.out.println(passwordStatus);
            return;
        }

        if (!userManager.validateNickname(nickName)) {
            System.out.println("Error: Nickname length is not valid!");
            return;
        }

        if (!userManager.validateEmail(email)) {
            System.out.println("Error: Invalid email format!");
            return;
        }

        Gender gender1;
        try {
            gender1 = Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Gender is not valid!");
            return;
        }

        isUserRegistered = true;

        pendingUsername = username;
        pendingPassword = password;
        pendingNickname = nickName;
        pendingEmail = email;
        pendingGender = gender1;

        showQuestions();
    }

    private void showQuestions() {
        int i = 1;
        for (String s : Questions.getQuestionsList()) {
            System.out.println((i++) + " . " + s);
        }
    }

    private void processPickQuestionCommand(String questionIdString, String answer, String answerConfirm) {
        if (!this.isUserRegistered) {
            System.out.println("Error: you must execute 'register' command successfully first.");
            return;
        }

        int questionId;

        try {
            questionId = Integer.parseInt(questionIdString);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid question number format.");
            return;
        }

        int questionsCnt = Questions.getQuestionsList().size();

        if (questionId > questionsCnt || questionId < 1) {
            System.out.println("Error: Invalid question number.");
            return;
        }

        if (!answer.equals(answerConfirm)) {
            System.out.println("Error: Answers do not match!");
            return;
        }

        boolean success = UserManager.getInstance().register(pendingUsername, pendingPassword, pendingNickname, pendingEmail,
                pendingGender, questionId, answer);

        if (success) {
            System.out.println("Account created successfully!");
            System.out.println("Returning to Login Menu...");

            menuManager.pushMenu(new LoginMenu());
        }
        else System.out.println("An unexpected error occurred.");

        this.isUserRegistered = false;
    }
}
