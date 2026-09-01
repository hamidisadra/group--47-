package network.service;

import network.protocol.MessageType;
import network.protocol.NetworkMessage;
import network.server.ClientConnection;
import network.server.ClientRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class MatchDirectory {
    private final ClientRegistry registry;
    private final Map<String, MatchSession> byPlayer = new ConcurrentHashMap<>();
    private final AtomicInteger matchCounter = new AtomicInteger();

    public MatchDirectory(ClientRegistry registry) {
        this.registry = registry;
    }

    public MatchSession create(String firstPlayer, String secondPlayer) {
        String matchId = "m" + matchCounter.incrementAndGet();
        MatchSession match = new MatchSession(matchId, firstPlayer,
            secondPlayer);

        byPlayer.put(firstPlayer, match);
        byPlayer.put(secondPlayer, match);

        send(firstPlayer, match.startMessageFor(firstPlayer));
        send(secondPlayer, match.startMessageFor(secondPlayer));

        return match;
    }

    public MatchSession find(String username) {
        return username == null ? null : byPlayer.get(username);
    }

    public boolean isInMatch(String username) {
        MatchSession match = find(username);

        return match != null && !match.isFinished();
    }

    public void relay(String fromUser, NetworkMessage message) {
        MatchSession match = find(fromUser);

        if (match == null || match.isFinished()) {
            return;
        }

        send(match.opponentOf(fromUser), message.put("from", fromUser));
    }

    public void finish(MatchSession match) {
        if (match == null) {
            return;
        }

        send(match.getPlantPlayer(),
            match.endMessageFor(match.getPlantPlayer()));
        send(match.getZombiePlayer(),
            match.endMessageFor(match.getZombiePlayer()));

        byPlayer.remove(match.getPlantPlayer(), match);
        byPlayer.remove(match.getZombiePlayer(), match);
    }

    public void handleDisconnect(String username) {
        MatchSession match = find(username);

        if (match == null) {
            return;
        }

        String opponent = match.opponentOf(username);
        send(opponent, NetworkMessage.of(MessageType.OPPONENT_LEFT,
            "opponent", username));

        match.forfeit(username);
        finish(match);
    }

    public List<MatchSession> activeMatches() {
        List<MatchSession> result = new ArrayList<>();

        for (MatchSession match : byPlayer.values()) {
            if (!result.contains(match)) {
                result.add(match);
            }
        }

        return result;
    }

    private void send(String username, NetworkMessage message) {
        ClientConnection connection = registry.find(username);

        if (connection != null) {
            connection.send(message);
        }
    }
}
