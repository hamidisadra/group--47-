package network.server;

import network.exception.NetworkException;
import network.protocol.NetworkMessage;
import network.service.MatchDirectory;
import network.service.MatchmakingService;

public final class ClientHandler extends Thread {
    private final ClientConnection connection;
    private final ClientRegistry registry;
    private final RequestRouter router;
    private final MatchDirectory matches;
    private final MatchmakingService matchmaking;

    public ClientHandler(ClientConnection connection, ClientRegistry registry,
                         RequestRouter router, MatchDirectory matches,
                         MatchmakingService matchmaking) {
        this.connection = connection;
        this.registry = registry;
        this.router = router;
        this.matches = matches;
        this.matchmaking = matchmaking;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            readLoop();
        }

        catch (NetworkException exception) {
            connection.sendError(exception.getMessage());
        }

        finally {
            String username = connection.getUsername();

            if (username != null) {
                matchmaking.forget(username);
                matches.handleDisconnect(username);
            }

            registry.release(connection);
            connection.close();
        }
    }

    private void readLoop() throws NetworkException {
        while (!isInterrupted()) {
            NetworkMessage request = connection.receive();

            if (request == null) {
                return;
            }

            router.handle(connection, request);
        }
    }
}
