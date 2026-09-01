package network.server;

import network.exception.NetworkException;
import network.protocol.MessageCodec;
import network.protocol.MessageType;
import network.protocol.NetworkMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public final class ClientConnection {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;

    private String username;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAuthenticated() {
        return username != null;
    }

    public NetworkMessage receive() throws NetworkException {
        return MessageCodec.read(input);
    }

    public void send(NetworkMessage message) {
        try {
            MessageCodec.write(output, message);
        }

        catch (NetworkException exception) {
            close();
        }
    }

    public void sendError(String reason) {
        send(NetworkMessage.of(MessageType.ERROR_RESPONSE, "reason", reason));
    }

    public void close() {
        try {
            socket.close();
        }

        catch (IOException ignored) {
            return;
        }
    }
}
