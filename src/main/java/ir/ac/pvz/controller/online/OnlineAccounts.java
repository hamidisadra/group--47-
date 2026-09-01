package ir.ac.pvz.controller.online;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.Gender;
import ir.ac.pvz.model.user.User;

import network.client.GameClient;
import network.protocol.NetworkMessage;

public final class OnlineAccounts {
    private static boolean onlineMode = true;
    private static final java.util.concurrent.atomic.AtomicBoolean SYNCING =
        new java.util.concurrent.atomic.AtomicBoolean();

    private OnlineAccounts() {
    }

    public static void setOnlineMode(boolean enabled) {
        onlineMode = enabled;
    }

    public static boolean isOnlineMode() {
        return onlineMode;
    }

    public static boolean isServerReachable() {
        return onlineMode && GameClient.getInstance().connect();
    }

    public static String register(String username, String password,
                                  String nickname, String email,
                                  String gender, int questionId,
                                  String answer) {
        if (!isServerReachable()) {
            return offlineFallback();
        }

        GameClient client = GameClient.getInstance();
        boolean created = client.register(username, password, nickname, email,
            gender, questionId, answer);

        if (!created) {
            return client.getLastError();
        }

        return null;
    }

    public static String login(String username, String password) {
        if (!isServerReachable()) {
            return offlineFallback();
        }

        GameClient client = GameClient.getInstance();
        NetworkMessage profile = client.login(username, password);

        if (profile == null) {
            return client.getLastError();
        }

        adoptProfile(profile);
        InviteHub.install(client);

        return null;
    }

    private static void adoptProfile(NetworkMessage profile) {
        UserManager users = UserManager.getInstance();
        String username = profile.get("username");
        User user = users.fromJson(profile.get("profile", ""));

        if (user != null) {
            users.replace(user);
            users.markServerManaged(username);
        }

        else {
            user = fallbackUser(users, username, profile);
        }

        users.ensurePlayable(user);
        MenuManager.getInstance().loginUser(user);
        installSyncHook(users);
    }

    private static User fallbackUser(UserManager users, String username,
                                     NetworkMessage profile) {
        User user = users.findUserByUsername(username);

        if (user == null) {
            user = users.adopt(new User(username, "",
                profile.get("nickname", username), profile.get("email", ""),
                Gender.MALE, 1, ""));
        }

        applyProfileValues(user, profile);

        return user;
    }

    private static void installSyncHook(UserManager users) {
        users.setProfileSyncHook(OnlineAccounts::pushProfile);
    }

    private static void pushProfile(User user) {
        if (!onlineMode || !GameClient.getInstance().isSignedIn()) {
            return;
        }

        if (!SYNCING.compareAndSet(false, true)) {
            return;
        }

        try {
            GameClient.getInstance().pushProfile(
                UserManager.getInstance().toJson(user));
        }

        finally {
            SYNCING.set(false);
        }
    }

    private static void applyProfileValues(User user, NetworkMessage profile) {
        user.setGameProgress(profile.getInt("progress", user.getGameProgress()));
        user.setMaxMuPoint(profile.getInt("maxMuPoint", user.getMaxMuPoint()));
        user.setDifficultyLevel(profile.getInt("difficulty", 3));

        syncWallet(user, profile.getInt("coins", 0), profile.getInt("gems", 0));

        for (String name : profile.get("minigames", "").split(",")) {
            if (!name.isBlank()) {
                user.completeMinigame(name);
            }
        }
    }

    private static void syncWallet(User user, int coins, int gems) {
        int coinDelta = coins - user.getWallet().getCoins();
        int gemDelta = gems - user.getWallet().getGems();

        if (coinDelta > 0) {
            user.getWallet().addCoins(coinDelta);
        }

        if (gemDelta > 0) {
            user.getWallet().addGems(gemDelta);
        }
    }

    public static void submitScore(int score) {
        if (onlineMode && GameClient.getInstance().isSignedIn()) {
            GameClient.getInstance().submitScore(score);
        }
    }

    public static void logout() {
        if (onlineMode && GameClient.getInstance().isSignedIn()) {
            GameClient.getInstance().logout();
        }
    }

    private static String offlineFallback() {
        return "Cannot reach the game server. Check that it is running.";
    }
}
