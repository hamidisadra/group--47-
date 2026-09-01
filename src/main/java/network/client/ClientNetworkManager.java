package network.client;

import network.exception.NetworkException;
import network.protocol.MessageCodec;
import network.protocol.MessageType;
import network.protocol.NetworkMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class ClientNetworkManager {
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int RESPONSE_TIMEOUT_MS = 6000;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private Thread readerThread;
    private volatile boolean connected;

    private final BlockingQueue<NetworkMessage> responses =
        new ArrayBlockingQueue<>(64);
    private final Map<MessageType, PushListener> pushListeners =
        new EnumMap<>(MessageType.class);

    public interface PushListener {
        void onMessage(NetworkMessage message);
    }

    public boolean isConnected() {
        return connected;
    }

    public void onPush(MessageType type, PushListener listener) {
        pushListeners.put(type, listener);
    }

    public void connect(String host, int port) throws NetworkException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            connected = true;

            readerThread = new Thread(this::readLoop, "pvz-client-reader");
            readerThread.setDaemon(true);
            readerThread.start();
        }

        catch (IOException exception) {
            throw new NetworkException("Could not reach the server.", exception);
        }
    }

    private void readLoop() {
        while (connected) {
            try {
                NetworkMessage message = MessageCodec.read(input);

                if (message == null) {
                    break;
                }

                dispatch(message);
            }

            catch (NetworkException exception) {
                break;
            }
        }

        connected = false;
    }

    private void dispatch(NetworkMessage message) {
        PushListener listener = pushListeners.get(message.getType());

        if (listener != null) {
            listener.onMessage(message);
            return;
        }

        responses.offer(message);
    }

    public void send(NetworkMessage message) throws NetworkException {
        if (!connected) {
            throw new NetworkException("Not connected to the server.");
        }

        MessageCodec.write(output, message);
    }

    public NetworkMessage request(NetworkMessage message)
            throws NetworkException {
        responses.clear();
        send(message);

        try {
            NetworkMessage response = responses.poll(RESPONSE_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);

            if (response == null) {
                throw new NetworkException("The server did not respond.");
            }

            return response;
        }

        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted while waiting.", exception);
        }
    }

    public void disconnect() {
        connected = false;

        try {
            if (socket != null) {
                socket.close();
            }
        }

        catch (IOException ignored) {
            return;
        }
    }
}
