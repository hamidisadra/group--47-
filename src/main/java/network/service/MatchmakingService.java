package network.service;

import network.protocol.MessageType;
import network.protocol.NetworkMessage;
import network.server.ClientConnection;
import network.server.ClientRegistry;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchmakingService {
    private final ClientRegistry registry;
    private final MatchDirectory matches;
    private final Deque<String> waitingQueue = new ArrayDeque<>();
    private final Map<String, String> pendingInvites = new ConcurrentHashMap<>();

    public MatchmakingService(ClientRegistry registry, MatchDirectory matches) {
        this.registry = registry;
        this.matches = matches;
    }

    public synchronized void joinQueue(ClientConnection connection) {
        String username = connection.getUsername();

        if (matches.isInMatch(username)) {
            connection.sendError("You are already in a match.");
            return;
        }

        waitingQueue.remove(username);

        String opponent = pollAvailableOpponent(username);

        if (opponent == null) {
            waitingQueue.addLast(username);
            connection.send(NetworkMessage.of(MessageType.JOIN_QUEUE_REQUEST,
                "status", "waiting"));
            return;
        }

        matches.create(opponent, username);
    }

    private String pollAvailableOpponent(String username) {
        while (!waitingQueue.isEmpty()) {
            String candidate = waitingQueue.pollFirst();

            if (candidate.equals(username) || !registry.isOnline(candidate)
                || matches.isInMatch(candidate)) {
                continue;
            }

            return candidate;
        }

        return null;
    }

    public synchronized void leaveQueue(ClientConnection connection) {
        waitingQueue.remove(connection.getUsername());
        pendingInvites.remove(connection.getUsername());
    }

    public synchronized void challenge(ClientConnection connection,
                                       String targetName) {
        String challenger = connection.getUsername();

        String problem = describeChallengeProblem(challenger, targetName);

        if (problem != null) {
            connection.sendError(problem);
            return;
        }

        pendingInvites.put(targetName, challenger);
        registry.find(targetName).send(NetworkMessage.of(MessageType.MATCH_INVITE,
            "from", challenger));
    }

    private String describeChallengeProblem(String challenger,
                                            String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return "Enter a username to challenge.";
        }

        if (targetName.equals(challenger)) {
            return "You cannot challenge yourself.";
        }

        if (!registry.isOnline(targetName)) {
            return "That player is not online.";
        }

        if (matches.isInMatch(targetName)) {
            return "That player is already in a match.";
        }

        if (pendingInvites.containsKey(targetName)) {
            return "That player already has a pending invite.";
        }

        return null;
    }

    public synchronized void respondToInvite(ClientConnection connection,
                                             boolean accepted) {
        String invited = connection.getUsername();
        String challenger = pendingInvites.remove(invited);

        if (challenger == null) {
            connection.sendError("There is no invite to answer.");
            return;
        }

        ClientConnection challengerConnection = registry.find(challenger);

        if (challengerConnection == null) {
            connection.sendError("The challenger went offline.");
            return;
        }

        if (!accepted) {
            challengerConnection.send(NetworkMessage.of(
                MessageType.MATCH_DECLINED, "from", invited));
            return;
        }

        waitingQueue.remove(challenger);
        waitingQueue.remove(invited);
        matches.create(challenger, invited);
    }

    public synchronized void forget(String username) {
        waitingQueue.remove(username);
        pendingInvites.remove(username);
        pendingInvites.values().remove(username);
    }

    public synchronized int getQueueSize() {
        return waitingQueue.size();
    }

    public synchronized boolean hasPendingInvite(String username) {
        return pendingInvites.containsKey(username);
    }
}
