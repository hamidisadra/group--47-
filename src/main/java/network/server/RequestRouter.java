package network.server;

import network.protocol.MessageType;
import network.protocol.NetworkMessage;
import network.service.AccountService;
import network.service.LeaderboardService;
import network.service.MatchDirectory;
import network.service.MatchSession;
import network.service.MatchmakingService;
import network.service.ReactionCatalog;

public final class RequestRouter {
    private final AccountService accounts;
    private final ClientRegistry registry;
    private final MatchDirectory matches;
    private final MatchmakingService matchmaking;
    private final LeaderboardService leaderboard = new LeaderboardService();

    public RequestRouter(AccountService accounts, ClientRegistry registry,
                         MatchDirectory matches,
                         MatchmakingService matchmaking) {
        this.accounts = accounts;
        this.registry = registry;
        this.matches = matches;
        this.matchmaking = matchmaking;
    }

    public void handle(ClientConnection connection, NetworkMessage request) {
        if (request.getType() == MessageType.REGISTER_REQUEST) {
            connection.send(accounts.register(request));
            return;
        }

        if (request.getType() == MessageType.LOGIN_REQUEST) {
            handleLogin(connection, request);
            return;
        }

        if (!connection.isAuthenticated()) {
            connection.sendError("You must sign in first.");
            return;
        }

        if (!handleAccount(connection, request)
            && !handleMatchmaking(connection, request)
            && !handleInMatch(connection, request)) {
            connection.sendError("Unsupported request: " + request.getType());
        }
    }

    private void handleLogin(ClientConnection connection,
                             NetworkMessage request) {
        NetworkMessage response = accounts.login(request);

        if (!response.isError()) {
            registry.bind(response.get("username"), connection);
        }

        connection.send(response);
    }

    private boolean handleAccount(ClientConnection connection,
                                  NetworkMessage request) {
        switch (request.getType()) {
            case PROFILE_REQUEST:
                connection.send(accounts.profile(connection.getUsername()));
                return true;

            case SCORE_SUBMIT:
                connection.send(accounts.recordScore(connection.getUsername(),
                    request.getInt("score", 0)));
                return true;

            case PROFILE_SYNC:
                connection.send(accounts.syncProfile(connection.getUsername(),
                    request.get("profile", "")));
                return true;

            case LEADERBOARD_REQUEST:
                connection.send(leaderboard.buildResponse());
                return true;

            case ONLINE_PLAYERS_REQUEST:
                connection.send(onlinePlayers(connection));
                return true;

            case LOGOUT_REQUEST:
                releasePlayer(connection);
                return true;

            default:
                return false;
        }
    }

    private boolean handleMatchmaking(ClientConnection connection,
                                      NetworkMessage request) {
        switch (request.getType()) {
            case JOIN_QUEUE_REQUEST:
                matchmaking.joinQueue(connection);
                return true;

            case LEAVE_QUEUE_REQUEST:
                matchmaking.leaveQueue(connection);
                return true;

            case CHALLENGE_PLAYER_REQUEST:
                matchmaking.challenge(connection, request.get("target"));
                return true;

            case ACCEPT_MATCH_REQUEST:
                matchmaking.respondToInvite(connection, true);
                return true;

            case REJECT_MATCH_REQUEST:
                matchmaking.respondToInvite(connection, false);
                return true;

            default:
                return false;
        }
    }

    private boolean handleInMatch(ClientConnection connection,
                                  NetworkMessage request) {
        switch (request.getType()) {
            case PLAYER_ACTION:
            case GAME_STATE_UPDATE:
                matches.relay(connection.getUsername(), request);
                return true;

            case TEXT_REACTION:
            case EMOJI_REACTION:
            case STICKER_REACTION:
                relayReaction(connection, request);
                return true;

            case GAME_END:
                reportOutcome(connection, request);
                return true;

            default:
                return false;
        }
    }

    private void relayReaction(ClientConnection connection,
                               NetworkMessage request) {
        String kind = kindOf(request.getType());
        int index = request.getInt("index", -1);

        if (!ReactionCatalog.isValid(kind, index)) {
            connection.sendError("Unknown reaction.");
            return;
        }

        matches.relay(connection.getUsername(), request
            .put("kind", kind)
            .put("value", ReactionCatalog.valueOf(kind, index)));
    }

    private String kindOf(MessageType type) {
        if (type == MessageType.TEXT_REACTION) {
            return "text";
        }

        if (type == MessageType.EMOJI_REACTION) {
            return "emoji";
        }

        return "sticker";
    }

    private void reportOutcome(ClientConnection connection,
                               NetworkMessage request) {
        MatchSession match = matches.find(connection.getUsername());

        if (match == null) {
            connection.sendError("You are not in a match.");
            return;
        }

        String event = request.get("event", "");

        match.checkTimeLimit();

        if (event.equals("brain")) {
            match.reportBrainEaten();
        }

        else if (event.equals("cleared")) {
            match.reportLawnCleared();
        }

        else if (event.equals("forfeit")) {
            match.forfeit(connection.getUsername());
        }

        else {
            match.checkTimeLimit();
        }

        if (match.isFinished()) {
            matches.finish(match);
        }
    }

    private void releasePlayer(ClientConnection connection) {
        matchmaking.forget(connection.getUsername());
        matches.handleDisconnect(connection.getUsername());
        registry.release(connection);
        connection.setUsername(null);
    }

    private NetworkMessage onlinePlayers(ClientConnection connection) {
        StringBuilder names = new StringBuilder();

        for (String name : registry.onlineUsernames()) {
            if (name.equals(connection.getUsername())) {
                continue;
            }

            if (names.length() > 0) {
                names.append(',');
            }

            names.append(name);
        }

        return NetworkMessage.of(MessageType.ONLINE_PLAYERS_RESPONSE,
            "players", names.toString());
    }
}
