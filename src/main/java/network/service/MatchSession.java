package network.service;

import network.protocol.MessageType;
import network.protocol.NetworkMessage;
import network.server.ClientConnection;

import java.util.concurrent.atomic.AtomicBoolean;

public final class MatchSession {
    public static final String ROLE_PLANT = "plant";
    public static final String ROLE_ZOMBIE = "zombie";
    public static final float DEFENCE_TIME_LIMIT_SECONDS = 120f;

    private final String matchId;
    private final String plantPlayer;
    private final String zombiePlayer;
    private final AtomicBoolean finished = new AtomicBoolean();

    private final long startedAtMillis = System.currentTimeMillis();
    private int brainsRemaining = 5;
    private String winner;
    private String outcomeReason;

    private final float timeLimitSeconds;

    public MatchSession(String matchId, String plantPlayer,
                        String zombiePlayer) {
        this(matchId, plantPlayer, zombiePlayer, DEFENCE_TIME_LIMIT_SECONDS);
    }

    public MatchSession(String matchId, String plantPlayer,
                        String zombiePlayer, float timeLimitSeconds) {
        this.matchId = matchId;
        this.plantPlayer = plantPlayer;
        this.zombiePlayer = zombiePlayer;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public float getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getPlantPlayer() {
        return plantPlayer;
    }

    public String getZombiePlayer() {
        return zombiePlayer;
    }

    public boolean isFinished() {
        return finished.get();
    }

    public String getWinner() {
        return winner;
    }

    public String getOutcomeReason() {
        return outcomeReason;
    }

    public float getElapsedSeconds() {
        return (System.currentTimeMillis() - startedAtMillis) / 1000f;
    }

    public int getBrainsRemaining() {
        return brainsRemaining;
    }

    public boolean contains(String username) {
        return plantPlayer.equals(username) || zombiePlayer.equals(username);
    }

    public String roleOf(String username) {
        if (plantPlayer.equals(username)) {
            return ROLE_PLANT;
        }

        if (zombiePlayer.equals(username)) {
            return ROLE_ZOMBIE;
        }

        return null;
    }

    public String opponentOf(String username) {
        if (plantPlayer.equals(username)) {
            return zombiePlayer;
        }

        if (zombiePlayer.equals(username)) {
            return plantPlayer;
        }

        return null;
    }

    public NetworkMessage startMessageFor(String username) {
        return NetworkMessage.of(MessageType.MATCH_STARTED,
            "matchId", matchId,
            "role", roleOf(username),
            "opponent", opponentOf(username),
            "timeLimit", Float.toString(timeLimitSeconds));
    }

    public synchronized void checkTimeLimit() {
        if (finished.get()) {
            return;
        }

        if (getElapsedSeconds() >= timeLimitSeconds) {
            finish(plantPlayer, "time");
        }
    }

    public synchronized void reportBrainEaten() {
        if (finished.get()) {
            return;
        }

        brainsRemaining = Math.max(0, brainsRemaining - 1);

        if (brainsRemaining == 0) {
            finish(zombiePlayer, "brains");
        }
    }

    public synchronized void reportLawnCleared() {
        if (!finished.get()) {
            finish(plantPlayer, "defended");
        }
    }

    public synchronized void forfeit(String username) {
        if (!finished.get()) {
            finish(opponentOf(username), "forfeit");
        }
    }

    private void finish(String winnerName, String reason) {
        if (finished.compareAndSet(false, true)) {
            winner = winnerName;
            outcomeReason = reason;
        }
    }

    public NetworkMessage endMessageFor(String username) {
        return NetworkMessage.of(MessageType.GAME_END,
            "matchId", matchId,
            "winner", winner == null ? "" : winner,
            "reason", outcomeReason == null ? "" : outcomeReason,
            "result", username.equals(winner) ? "win" : "lose");
    }

    public void broadcast(ClientConnection plantSide,
                          ClientConnection zombieSide,
                          NetworkMessage message) {
        if (plantSide != null) {
            plantSide.send(message);
        }

        if (zombieSide != null) {
            zombieSide.send(message);
        }
    }
}
