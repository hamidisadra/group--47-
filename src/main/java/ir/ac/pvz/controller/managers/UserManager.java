package ir.ac.pvz.controller.managers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ir.ac.pvz.model.user.Gender;
import ir.ac.pvz.model.user.NewsType;
import ir.ac.pvz.model.user.User;

import java.io.*;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.regex.Pattern;

public class UserManager {

    private static final String PASSWORD_SPECIALS = "!#$%^&*()=+}{[]|/\\:;'\",><?";
    private static final int STARTER_COINS = 2500;
    private static final int STARTER_GEMS = 25;
    private static final int STARTER_PLANT_LEVEL = 3;

    private static final String[] STARTER_PLANTS = {
        "Sunflower", "Peashooter", "Wall-nut", "Cherry Bomb", "Potato Mine",
        "Snow Pea", "Cabbage-pult", "Bonk Choy"
    };

    private static UserManager instance;
    private HashMap<String, User> users;
    private final java.util.Set<String> serverManaged = new java.util.HashSet<>();
    private static final String DATA_FILE = "users_data.json";

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

        String hashedPassword = hashPassword(password);

        User newUser = new User(username, hashedPassword, nickname, email, gender, securityQuestionId, securityAnswer);
        users.put(username, newUser);
        grantStarterPack(newUser);

        saveAll();
        return true;
    }

    public void markServerManaged(String username) {
        if (username != null) {
            serverManaged.add(username);
        }
    }

    public void replace(User user) {
        if (user != null && user.getUsername() != null) {
            users.put(user.getUsername(), user);
        }
    }

    public User adopt(User user) {
        if (user == null || user.getUsername() == null) {
            return null;
        }

        User local = users.get(user.getUsername());

        if (local != null) {
            return local;
        }

        serverManaged.add(user.getUsername());
        users.put(user.getUsername(), user);

        return user;
    }

    public boolean isServerManaged(String username) {
        return serverManaged.contains(username);
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

        if (containsForbiddenCharacter(password)) {
            return "Password contains invalid characters. Only letters, digits and the "
                + "special characters listed in the guide are allowed.";
        }

        return describeMissingClass(password);
    }

    private boolean containsForbiddenCharacter(String password) {
        for (char character : password.toCharArray()) {
            boolean allowed = Character.isLetterOrDigit(character)
                || PASSWORD_SPECIALS.indexOf(character) != -1;
            if (!allowed) {
                return true;
            }
        }

        return false;
    }

    private String describeMissingClass(String password) {
        if (!matches(password, Character::isLowerCase)) {
            return "Password must contain at least one lowercase letter.";
        }

        if (!matches(password, Character::isUpperCase)) {
            return "Password must contain at least one uppercase letter.";
        }

        if (!matches(password, Character::isDigit)) {
            return "Password must contain at least one digit.";
        }

        if (!matches(password, character -> PASSWORD_SPECIALS.indexOf(character) != -1)) {
            return "Password must contain at least one special character (e.g., *&^%$).";
        }

        return "Valid";
    }

    private boolean matches(String password, CharacterRule rule) {
        for (char character : password.toCharArray()) {
            if (rule.test(character)) {
                return true;
            }
        }

        return false;
    }

    @FunctionalInterface
    private interface CharacterRule {
        boolean test(char character);
    }

    public boolean validateNickname(String nickName) {
        int nickNameLength = nickName.length();
        return nickNameLength >= 3 && nickNameLength <= 30;
    }

    public boolean validateEmail(String email) {
        String emailRegex = "^(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?"
            + "@([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";
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

    public java.util.Collection<User> getAllUsers() {
        return java.util.Collections.unmodifiableCollection(users.values());
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

    public interface ProfileSyncHook {
        void onProfileSaved(User user);
    }

    private ProfileSyncHook profileSyncHook;

    public void setProfileSyncHook(ProfileSyncHook hook) {
        this.profileSyncHook = hook;
    }

    public String toJson(User user) {
        return buildGson().toJson(user);
    }

    public User fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        return buildGson().fromJson(json, User.class);
    }

    private Gson buildGson() {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                    -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context)
                    -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(ir.ac.pvz.model.travel.Quest.class,
                new ir.ac.pvz.model.travel.QuestTypeAdapter())
            .setPrettyPrinting()
            .create();
    }

    public void saveAll() {
        HashMap<String, User> persistable = new HashMap<>(users);
        persistable.keySet().removeAll(serverManaged);

        try (Writer writer = new FileWriter(DATA_FILE)) {
            Gson gson = buildGson();
            gson.toJson(persistable, writer);
        } catch (IOException e) {
            System.err.println("Could not save user data: " + e.getMessage());
        }

        notifyProfileSaved();
    }

    private void notifyProfileSaved() {
        if (profileSyncHook == null) {
            return;
        }

        User active = MenuManager.getInstance().getActiveUser();

        if (active != null) {
            profileSyncHook.onProfileSaved(active);
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

    public void ensurePlayable(User user) {
        if (user == null) {
            return;
        }

        ir.ac.pvz.controller.gui.QuestSeeder.seed(user);

        if (!user.getCollection().getUnlockedPlants().isEmpty()) {
            saveAll();
            return;
        }

        grantStarterPack(user);
        saveAll();
    }

    private void grantStarterPack(User user) {
        user.getWallet().addCoins(STARTER_COINS);
        user.getWallet().addGems(STARTER_GEMS);

        for (String plant : STARTER_PLANTS) {
            user.getCollection().unlockPlant(plant);
            user.getCollection().setPlantLevel(plant, STARTER_PLANT_LEVEL);
        }

        user.getInventory().addPlantFood();
        user.addNews("Welcome to the lawn! We packed you a few seeds and some coins "
            + "to get started.", NewsType.SYSTEM_UPDATES);
    }
}
