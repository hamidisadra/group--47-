package network.protocol;

import network.exception.NetworkException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MessageCodec {
    private static final int MAX_FRAME_BYTES = 4 * 1024 * 1024;
    private static final char FIELD_SEPARATOR = '\u001f';
    private static final char PAIR_SEPARATOR = '\u001e';

    private MessageCodec() {
    }

    public static String encodeBody(NetworkMessage message) {
        StringBuilder builder = new StringBuilder(message.getType().name());
        builder.append(FIELD_SEPARATOR).append(message.getTimestamp());

        for (Map.Entry<String, String> entry : message.getFields().entrySet()) {
            builder.append(FIELD_SEPARATOR).append(escape(entry.getKey()))
                .append(PAIR_SEPARATOR).append(escape(entry.getValue()));
        }

        return builder.toString();
    }

    public static NetworkMessage decodeBody(String body) throws NetworkException {
        String[] parts = body.split(String.valueOf(FIELD_SEPARATOR), -1);

        if (parts.length < 2) {
            throw new NetworkException("Malformed message frame.");
        }

        MessageType type = parseType(parts[0]);
        long timestamp = parseTimestamp(parts[1]);
        Map<String, String> fields = new LinkedHashMap<>();

        for (int index = 2; index < parts.length; index++) {
            String[] pair = parts[index].split(String.valueOf(PAIR_SEPARATOR), -1);

            if (pair.length == 2) {
                fields.put(unescape(pair[0]), unescape(pair[1]));
            }
        }

        return new NetworkMessage(type, fields, timestamp);
    }

    private static MessageType parseType(String raw) throws NetworkException {
        try {
            return MessageType.valueOf(raw);
        }

        catch (IllegalArgumentException exception) {
            throw new NetworkException("Unknown message type: " + raw);
        }
    }

    private static long parseTimestamp(String raw) {
        try {
            return Long.parseLong(raw);
        }

        catch (NumberFormatException exception) {
            return System.currentTimeMillis();
        }
    }

    public static void write(DataOutputStream output, NetworkMessage message)
            throws NetworkException {
        byte[] payload = encodeBody(message).getBytes(StandardCharsets.UTF_8);

        if (payload.length > MAX_FRAME_BYTES) {
            throw new NetworkException("Message exceeds the frame limit.");
        }

        try {
            synchronized (output) {
                output.writeInt(payload.length);
                output.write(payload);
                output.flush();
            }
        }

        catch (IOException exception) {
            throw new NetworkException("Failed to send message.", exception);
        }
    }

    public static NetworkMessage read(DataInputStream input)
            throws NetworkException {
        try {
            int length = input.readInt();

            if (length < 0 || length > MAX_FRAME_BYTES) {
                throw new NetworkException("Invalid frame length: " + length);
            }

            byte[] payload = new byte[length];
            input.readFully(payload);

            return decodeBody(new String(payload, StandardCharsets.UTF_8));
        }

        catch (EOFException exception) {
            return null;
        }

        catch (IOException exception) {
            throw new NetworkException("Failed to read message.", exception);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
            .replace(String.valueOf(FIELD_SEPARATOR), "\\u001f")
            .replace(String.valueOf(PAIR_SEPARATOR), "\\u001e");
    }

    private static String unescape(String value) {
        return value.replace("\\u001e", String.valueOf(PAIR_SEPARATOR))
            .replace("\\u001f", String.valueOf(FIELD_SEPARATOR))
            .replace("\\\\", "\\");
    }
}
