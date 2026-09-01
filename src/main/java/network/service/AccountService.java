package network.service;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.Gender;
import ir.ac.pvz.model.user.User;

import network.protocol.MessageType;
import network.protocol.NetworkMessage;

public final class AccountService {
    private final UserManager users;

    public AccountService() {
        this(UserManager.getInstance());
    }

    public AccountService(UserManager users) {
        this.users = users;
    }

    public synchronized NetworkMessage register(NetworkMessage request) {
        String username = request.get("username", "");
        String password = request.get("password", "");
        String nickname = request.get("nickname", "");
        String email = request.get("email", "");

        String problem = validate(username, password, nickname, email);

        if (problem != null) {
            return NetworkMessage.error(problem);
        }

        Gender gender = parseGender(request.get("gender", "MALE"));
        boolean created = users.register(username, password, nickname, email,
            gender, request.getInt("questionId", 0),
            request.get("answer", ""));

        if (!created) {
            return NetworkMessage.error("Could not create the account.");
        }

        users.saveAll();

        return NetworkMessage.of(MessageType.REGISTER_RESPONSE,
            "username", username);
    }

    private String validate(String username, String password, String nickname,
                            String email) {
        if (users.isUsernameTaken(username)) {
            return "This username is already taken.";
        }

        if (!users.validateUsername(username)) {
            return "Username may only contain letters, digits and dashes.";
        }

        String passwordProblem = users.validatePassword(password);

        if (!"Valid".equals(passwordProblem)) {
            return passwordProblem;
        }

        if (!users.validateNickname(nickname)) {
            return "Nickname must be between 3 and 30 characters.";
        }

        if (!users.validateEmail(email)) {
            return "Email address is not valid.";
        }

        return null;
    }

    private Gender parseGender(String raw) {
        try {
            return Gender.valueOf(raw.toUpperCase());
        }

        catch (IllegalArgumentException exception) {
            return Gender.MALE;
        }
    }

    public synchronized NetworkMessage login(NetworkMessage request) {
        String username = request.get("username", "");
        User user = users.findUserByUsername(username);

        if (user == null) {
            return NetworkMessage.error("No account exists with that username.");
        }

        String hashed = users.hashPassword(request.get("password", ""));

        if (!hashed.equals(user.getPasswordHash())) {
            return NetworkMessage.error("The password is incorrect.");
        }

        users.ensurePlayable(user);

        return profileOf(user, MessageType.LOGIN_RESPONSE);
    }

    public synchronized NetworkMessage profile(String username) {
        User user = users.findUserByUsername(username);

        if (user == null) {
            return NetworkMessage.error("Unknown account.");
        }

        return profileOf(user, MessageType.PROFILE_RESPONSE);
    }

    public synchronized NetworkMessage syncProfile(String username,
                                                   String json) {
        return syncProfile(username, users.fromJson(json));
    }

    public synchronized NetworkMessage syncProfile(String username,
                                                   User incoming) {
        User stored = users.findUserByUsername(username);

        if (stored == null || incoming == null) {
            return NetworkMessage.error("Could not read the profile.");
        }

        mergeIntoStored(stored, incoming);
        users.saveAll();

        return NetworkMessage.of(MessageType.PROFILE_SYNC_ACK,
            "username", username);
    }

    private void mergeIntoStored(User stored, User incoming) {
        incoming.setUsername(stored.getUsername());
        incoming.setPasswordHash(stored.getPasswordHash());
        incoming.setSecurityQuestionId(stored.getSecurityQuestionId());
        incoming.setSecurityAnswer(stored.getSecurityAnswer());

        if (stored.hasRankedScore()) {
            incoming.markRankedScoreRecorded();
        }

        users.replace(incoming);
    }

    private NetworkMessage profileOf(User user, MessageType type) {
        return new NetworkMessage(type)
            .put("profile", users.toJson(user))
            .put("username", user.getUsername())
            .put("nickname", user.getNickName())
            .put("email", user.getEmail())
            .put("coins", user.getWallet().getCoins())
            .put("gems", user.getWallet().getGems())
            .put("games", user.getGamesCount())
            .put("progress", user.getGameProgress())
            .put("maxMuPoint", user.getMaxMuPoint())
            .put("ranked", user.hasRankedScore())
            .put("difficulty", user.getDifficultyLevel())
            .put("minigames", String.join(",", user.getCompletedMinigames()));
    }

    public synchronized NetworkMessage recordScore(String username, int score) {
        User user = users.findUserByUsername(username);

        if (user == null) {
            return NetworkMessage.error("Unknown account.");
        }

        if (score > user.getMaxMuPoint()) {
            user.setMaxMuPoint(score);
        }

        user.markRankedScoreRecorded();
        users.saveAll();

        return NetworkMessage.of(MessageType.SCORE_ACCEPTED,
            "maxMuPoint", Integer.toString(user.getMaxMuPoint()))
            .put("ranked", true);
    }

    public UserManager getUsers() {
        return users;
    }
}
