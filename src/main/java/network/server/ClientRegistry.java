package network.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientRegistry {
    private final Map<String, ClientConnection> online = new ConcurrentHashMap<>();

    public void bind(String username, ClientConnection connection) {
        ClientConnection previous = online.put(username, connection);

        if (previous != null && previous != connection) {
            previous.sendError("Signed in from another device.");
            previous.close();
        }

        connection.setUsername(username);
    }

    public void release(ClientConnection connection) {
        String username = connection.getUsername();

        if (username != null) {
            online.remove(username, connection);
        }
    }

    public boolean isOnline(String username) {
        return username != null && online.containsKey(username);
    }

    public ClientConnection find(String username) {
        return username == null ? null : online.get(username);
    }

    public List<String> onlineUsernames() {
        return new ArrayList<>(online.keySet());
    }

    public int onlineCount() {
        return online.size();
    }
}
