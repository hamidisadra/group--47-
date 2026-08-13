package ir.ac.pvz.model.travel;

public class ScoreEvent {
    private ScoreEventType type;
    private int timestamp;
    private int value;

    public ScoreEvent(ScoreEventType type, int timestamp, int value) {
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
    }

    public ScoreEventType getType() {
        return type;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }

    public int getPoints() {
        switch (type) {
            case MULTI_HIT:
                return value * 20;
            case SPEED_KILL:
                return 15;
            case SIMULTANEOUS_KILLS:
                return value * 25;
            case CHAIN_KILL:
                return value * 10;
            case FLAWLESS_DEFENSE:
                return value * 30;
            default:
                return 0;
        }
    }
}