package ir.ac.pvz.controller.managers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ir.ac.pvz.model.user.Gender;
import ir.ac.pvz.model.user.User;

import java.io.*;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.regex.Pattern;

public class UserManager {
    private static UserManager instance;
    private HashMap<String, User> users;
    private static final String DATA_FILE = "users_data.json";

    //Constructor
    private UserManager() {
        users = new HashMap<>();
        loadAll(); // Load data on startup
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

        String hashedPassword = hashPassword(password);

        User newUser = new User(username, hashedPassword, nickname, email, gender, securityQuestionId, securityAnswer);
        users.put(username, newUser);

        saveAll();
        return true;
    }

    public User findUserByUsername(String username) {
        return users.get(username);
    }

    public boolean isUsernameTaken(String username) {
        return users.containsKey(username);
    }

    public boolean validateUsername(String username) {
        return username.matches("^[a-zA-Z0-9-]+$");
    }

    public String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false, hasInvalid = false;
        // Added the missing special characters defined in the doc (\, /, |)
        String specialChars = "[]{}()<>+=*&^%$#!?\\/|";

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

    public boolean validateNickname(String nickName) {
        int nickNameLength = nickName.length();
        return nickNameLength >= 3 && nickNameLength <= 30;
    }

    public boolean validateEmail(String email) {
        String emailRegex = "^(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?@([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: Hashing algorithm not found!", e);
        }
    }

    public void updateUsernameKeyValue(String previousUsername, String currentUsername, User user) {
        users.remove(previousUsername);

        user.setUsername(currentUsername);
        users.put(currentUsername, user);

        saveAll();
    }

    public User findLoggedInUser() {
        for (User user : users.values()) {
            if (user.isStayLoggedIn()) {
                return user;
            }
        }
        return null;
    }

    public void clearLoggedIn() {
        for (User user : users.values()) {
            user.setStayLoggedIn(false);
        }
        saveAll();
    }

    private Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                                -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context)
                                -> LocalDateTime.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();
    }

    public void saveAll() {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            Gson gson = buildGson();
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Could not save user data: " + e.getMessage());
        }
    }

    public void loadAll() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (Reader reader = new FileReader(DATA_FILE)) {
            Gson gson = buildGson();
            Type type = new TypeToken<HashMap<String, User>>() {}.getType();
            HashMap<String, User> loadedUsers = gson.fromJson(reader, type);

            if (loadedUsers != null) {
                users = loadedUsers;
            }
        } catch (IOException e) {
            System.err.println("Could not load user data: " + e.getMessage());
        }
    }
}