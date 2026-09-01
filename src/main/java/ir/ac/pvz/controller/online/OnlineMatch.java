package ir.ac.pvz.controller.online;

import ir.ac.pvz.controller.gui.SessionController;
import ir.ac.pvz.model.support.BoardSnapshot;
import ir.ac.pvz.model.support.GridPosition;

import network.client.GameClient;
import network.protocol.MessageType;
import network.protocol.NetworkMessage;

public final class OnlineMatch {
    public static final String ROLE_PLANT = "plant";
    public static final String ROLE_ZOMBIE = "zombie";

    private static final float SNAPSHOT_INTERVAL_SECONDS = 0.1f;
    private static final float HEARTBEAT_SECONDS = 1f;
    private static final int TOTAL_BRAINS = 5;

    private final GameClient client;
    private final String matchId;
    private final String role;
    private final String opponent;

    private SessionController controller;
    private float snapshotTimer;
    private float elapsedSeconds;
    private int brainsReported;
    private float heartbeatTimer;
    private final java.util.Queue<NetworkMessage> incomingActions =
        new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final java.util.concurrent.atomic.AtomicReference<NetworkMessage>
        latestState = new java.util.concurrent.atomic.AtomicReference<>();
    private final java.util.concurrent.atomic.AtomicReference<Reaction>
        lastReaction = new java.util.concurrent.atomic.AtomicReference<>();
    private volatile boolean finished;
    private String result;

    public OnlineMatch(GameClient client, NetworkMessage start) {
        this.client = client;
        this.matchId = start.get("matchId", "");
        this.role = start.get("role", ROLE_PLANT);
        this.opponent = start.get("opponent", "");

        client.onPush(MessageType.PLAYER_ACTION, incomingActions::add);
        client.onPush(MessageType.GAME_STATE_UPDATE, latestState::set);
        client.onPush(MessageType.GAME_END, this::onMatchEnd);
    }

    public void attach(SessionController controller) {
        this.controller = controller;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getRole() {
        return role;
    }

    public String getOpponent() {
        return opponent;
    }

    public boolean isHost() {
        return ROLE_PLANT.equals(role);
    }

    public boolean isFinished() {
        return finished;
    }

    public String getResult() {
        return result;
    }

    public static final class Reaction {
        public final String kind;
        public final int index;
        public final String value;

        Reaction(String kind, int index, String value) {
            this.kind = kind;
            this.index = index;
            this.value = value;
        }
    }

    public Reaction pollReaction() {
        return lastReaction.getAndSet(null);
    }

    public void requestZombie(String cardType, GridPosition position) {
        if (isHost()) {
            return;
        }

        client.send(NetworkMessage.of(MessageType.PLAYER_ACTION,
            "kind", "placeZombie",
            "card", cardType,
            "col", Integer.toString(position.x),
            "row", Integer.toString(position.y)));
    }

    private void drainRemoteActions() {
        if (controller == null || !isHost()) {
            incomingActions.clear();
            return;
        }

        NetworkMessage message = incomingActions.poll();

        while (message != null) {
            applyRemoteAction(message);
            message = incomingActions.poll();
        }
    }

    private void applyRemoteAction(NetworkMessage message) {
        if (!"placeZombie".equals(message.get("kind", ""))) {
            return;
        }

        controller.placeZombieCard(message.get("card", "basic"),
            new GridPosition(message.getInt("col", 0),
                message.getInt("row", 0)));
    }

    private void applyLatestState() {
        NetworkMessage message = latestState.getAndSet(null);

        if (message == null || controller == null || isHost()) {
            return;
        }

        BoardSnapshot.applyZombies(controller.getBoard(),
            controller.getSession(), message.get("zombies", ""));
        BoardSnapshot.applyPlants(controller.getBoard(),
            message.get("plants", ""));
    }

    private void onMatchEnd(NetworkMessage message) {
        finished = true;
        result = message.get("result", "lose");
    }

    public void update(float delta) {
        if (controller == null || finished) {
            return;
        }

        drainRemoteActions();
        applyLatestState();

        elapsedSeconds += delta;

        if (!isHost()) {
            return;
        }

        reportProgress(delta);
        broadcastState(delta);
    }

    private void reportProgress(float delta) {
        int brains = TOTAL_BRAINS - controller.getRemainingBrains();

        while (brainsReported < brains) {
            brainsReported++;
            client.send(NetworkMessage.of(MessageType.GAME_END,
                "event", "brain"));
        }

        heartbeatTimer += delta;

        if (heartbeatTimer < HEARTBEAT_SECONDS) {
            return;
        }

        heartbeatTimer = 0f;
        client.send(NetworkMessage.of(MessageType.GAME_END, "event", "tick"));
    }

    private void broadcastState(float delta) {
        snapshotTimer += delta;

        if (snapshotTimer < SNAPSHOT_INTERVAL_SECONDS) {
            return;
        }

        snapshotTimer = 0f;

        client.send(NetworkMessage.of(MessageType.GAME_STATE_UPDATE,
            "zombies", BoardSnapshot.encodeZombies(controller.getBoard()),
            "plants", BoardSnapshot.encodePlants(controller.getBoard()),
            "elapsed", Float.toString(elapsedSeconds)));
    }

    public void observeReactions() {
        client.onPush(MessageType.TEXT_REACTION, this::rememberReaction);
        client.onPush(MessageType.EMOJI_REACTION, this::rememberReaction);
        client.onPush(MessageType.STICKER_REACTION, this::rememberReaction);
    }

    private void rememberReaction(NetworkMessage message) {
        lastReaction.set(new Reaction(message.get("kind", "text"),
            message.getInt("index", 0), message.get("value", "")));
    }

    public void forfeit() {
        client.send(NetworkMessage.of(MessageType.GAME_END,
            "event", "forfeit"));
    }
}
