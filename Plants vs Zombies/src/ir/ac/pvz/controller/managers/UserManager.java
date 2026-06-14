package ir.ac.pvz.controller.managers;

import ir.ac.pvz.model.user.Gender;
import ir.ac.pvz.model.user.User;

import java.util.HashMap;
import java.util.regex.Pattern;

public class UserManager {
    private static UserManager instance;

    private HashMap<String, User> users;

    //Constructor

    private UserManager() {
        users = new HashMap<>();
        loadAll();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public boolean register(String username, String password, String nickname, String email,
                            Gender gender, int securityQuestionId, String securityAnswer) {
        if (isUsernameTaken(username)) return false;

        User newUser = new User(username, password, nickname, email, gender, securityQuestionId, securityAnswer);
        users.put(username, newUser);
        saveAll();
        return true;
    }

    public User authenticate(String username,String password) {
        User user = users.get(username);

        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean forgetPassword(String username, String email) {
        User user = users.get(username);

        return user != null && user.getEmail().equals(email);
    }

    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if (user.getPasswordHash().equals(oldPassword)) {
            user.setPasswordHash(newPassword);
            saveAll();
            return true;
        }
        return false;
    }

    public boolean isUsernameTaken(String username) {
        return users.containsKey(username);
    }

    public boolean validateEmail(String email) {
        String emailRegex = "^(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?@([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false, hasInvalid = false;
        String specialChars = "[]{}()<>+=*&^%$#!?";

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (specialChars.indexOf(c) != -1) hasSpecial = true;
            else hasInvalid = true;
        }

        if (hasInvalid) return "Password contains invalid characters. Only letters, numbers, and specific special characters are allowed.";
        if (!hasLower) return "Password must contain at least one lowercase letter.";
        if (!hasUpper) return "Password must contain at least one uppercase letter.";
        if (!hasDigit) return "Password must contain at least one digit.";
        if (!hasSpecial) return "Password must contain at least one special character (e.g., *&^%$).";

        return "Valid";
    }

    public void saveAll() {}
    public void loadAll() {}
}
