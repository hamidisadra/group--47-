package network.server;

import network.service.AccountService;
import network.service.MatchDirectory;
import network.service.MatchmakingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class Server {
    public static final int DEFAULT_PORT = 7777;

    private final int port;
    private final ClientRegistry registry = new ClientRegistry();
    private final MatchDirectory matches = new MatchDirectory(registry);
    private final MatchmakingService matchmaking =
        new MatchmakingService(registry, matches);
    private final RequestRouter router;

    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;

    public Server(int port) {
        this(port, new AccountService());
    }

    public Server(int port, AccountService accounts) {
        this.port = port;
        this.router = new RequestRouter(accounts, registry, matches,
            matchmaking);
    }

    public ClientRegistry getRegistry() {
        return registry;
    }

    public MatchDirectory getMatches() {
        return matches;
    }

    public MatchmakingService getMatchmaking() {
        return matchmaking;
    }

    public int getPort() {
        return serverSocket == null ? port : serverSocket.getLocalPort();
    }

    public boolean isRunning() {
        return running;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        acceptThread = new Thread(this::acceptLoop, "pvz-server-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();

        System.out.println("Server listening on port " + getPort() + ".");
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientConnection connection = new ClientConnection(socket);
                new ClientHandler(connection, registry, router, matches,
                    matchmaking).start();
            }

            catch (IOException exception) {
                if (running) {
                    System.out.println("Accept failed: " + exception.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }

        catch (IOException ignored) {
            return;
        }
    }
}
