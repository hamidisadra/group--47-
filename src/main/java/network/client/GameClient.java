package network.client;

import network.exception.NetworkException;
import network.protocol.MessageType;
import network.protocol.NetworkMessage;

public final class GameClient {
    private static GameClient instance;

    private final ClientNetworkManager network;

    private String host = "127.0.0.1";
    private int port = 7777;
    private String username;
    private String lastError;

    private GameClient() {
        this(new ClientNetworkManager());
    }

    public GameClient(ClientNetworkManager network) {
        this.network = network;
    }

    public static synchronized GameClient getInstance() {
        if (instance == null) {
            instance = new GameClient();
        }

        return instance;
    }

    public ClientNetworkManager getNetwork() {
        return network;
    }

    public String getUsername() {
        return username;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean isConnected() {
        return network.isConnected();
    }

    public boolean isSignedIn() {
        return username != null && network.isConnected();
    }

    public void configure(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        if (network.isConnected()) {
            return true;
        }

        try {
            network.connect(host, port);
            return true;
        }

        catch (NetworkException exception) {
            lastError = exception.getMessage();
            return false;
        }
    }

    public boolean register(String username, String password, String nickname,
                            String email, String gender, int questionId,
                            String answer) {
        NetworkMessage response = exchange(
            NetworkMessage.of(MessageType.REGISTER_REQUEST,
                "username", username,
                "password", password,
                "nickname", nickname,
                "email", email,
                "gender", gender,
                "answer", answer).put("questionId", questionId));

        return response != null && !response.isError();
    }

    public NetworkMessage login(String username, String password) {
        NetworkMessage response = exchange(
            NetworkMessage.of(MessageType.LOGIN_REQUEST,
                "username", username, "password", password));

        if (response == null || response.isError()) {
            return null;
        }

        this.username = response.get("username");

        return response;
    }

    public void logout() {
        send(NetworkMessage.of(MessageType.LOGOUT_REQUEST));
        username = null;
    }

    public void pushProfile(String profileJson) {
        send(NetworkMessage.of(MessageType.PROFILE_SYNC,
            "profile", profileJson));
    }

    public NetworkMessage fetchProfile() {
        return exchange(NetworkMessage.of(MessageType.PROFILE_REQUEST));
    }

    public NetworkMessage fetchLeaderboard() {
        return exchange(NetworkMessage.of(MessageType.LEADERBOARD_REQUEST));
    }

    public NetworkMessage submitScore(int score) {
        return exchange(NetworkMessage.of(MessageType.SCORE_SUBMIT,
            "score", Integer.toString(score)));
    }

    public void joinRandomQueue() {
        send(NetworkMessage.of(MessageType.JOIN_QUEUE_REQUEST));
    }

    public void leaveQueue() {
        send(NetworkMessage.of(MessageType.LEAVE_QUEUE_REQUEST));
    }

    public void challenge(String target) {
        send(NetworkMessage.of(MessageType.CHALLENGE_PLAYER_REQUEST,
            "target", target));
    }

    public void acceptInvite() {
        send(NetworkMessage.of(MessageType.ACCEPT_MATCH_REQUEST));
    }

    public void rejectInvite() {
        send(NetworkMessage.of(MessageType.REJECT_MATCH_REQUEST));
    }

    public void sendReaction(String kind, int index) {
        MessageType type = MessageType.TEXT_REACTION;

        if ("emoji".equals(kind)) {
            type = MessageType.EMOJI_REACTION;
        }

        else if ("sticker".equals(kind)) {
            type = MessageType.STICKER_REACTION;
        }

        send(NetworkMessage.of(type, "index", Integer.toString(index)));
    }

    public void onPush(MessageType type,
                       ClientNetworkManager.PushListener listener) {
        network.onPush(type, listener);
    }

    public void send(NetworkMessage message) {
        try {
            network.send(message);
        }

        catch (NetworkException exception) {
            lastError = exception.getMessage();
        }
    }

    private NetworkMessage exchange(NetworkMessage message) {
        try {
            NetworkMessage response = network.request(message);

            if (response.isError()) {
                lastError = response.get("reason", "Request failed.");
            }

            return response;
        }

        catch (NetworkException exception) {
            lastError = exception.getMessage();
            return null;
        }
    }

    public void disconnect() {
        username = null;
        network.disconnect();
    }

    public static synchronized void reset() {
        if (instance != null) {
            instance.disconnect();
        }

        instance = null;
    }
}
