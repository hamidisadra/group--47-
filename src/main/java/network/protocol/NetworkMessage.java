package network.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NetworkMessage {
    private final MessageType type;
    private final Map<String, String> fields;
    private final long timestamp;

    public NetworkMessage(MessageType type) {
        this(type, new LinkedHashMap<>(), System.currentTimeMillis());
    }

    public NetworkMessage(MessageType type, Map<String, String> fields,
                          long timestamp) {
        this.type = type;
        this.fields = new LinkedHashMap<>(fields);
        this.timestamp = timestamp;
    }

    public static NetworkMessage of(MessageType type, String... keyValuePairs) {
        NetworkMessage message = new NetworkMessage(type);

        for (int index = 0; index + 1 < keyValuePairs.length; index += 2) {
            message.put(keyValuePairs[index], keyValuePairs[index + 1]);
        }

        return message;
    }

    public static NetworkMessage error(String reason) {
        return of(MessageType.ERROR_RESPONSE, "reason", reason);
    }

    public MessageType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public NetworkMessage put(String key, String value) {
        if (key != null && value != null) {
            fields.put(key, value);
        }

        return this;
    }

    public NetworkMessage put(String key, int value) {
        return put(key, Integer.toString(value));
    }

    public NetworkMessage put(String key, boolean value) {
        return put(key, Boolean.toString(value));
    }

    public String get(String key) {
        return fields.get(key);
    }

    public String get(String key, String fallback) {
        return fields.getOrDefault(key, fallback);
    }

    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(fields.getOrDefault(key, ""));
        }

        catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public float getFloat(String key, float fallback) {
        try {
            return Float.parseFloat(fields.getOrDefault(key, ""));
        }

        catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(fields.getOrDefault(key, "false"));
    }

    public boolean isError() {
        return type == MessageType.ERROR_RESPONSE;
    }

    @Override
    public String toString() {
        return type + fields.toString();
    }
}
