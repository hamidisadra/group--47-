package network.server;

import java.io.IOException;

public final class ServerMain {
    private ServerMain() {
    }

    public static void main(String[] args) throws IOException {
        int port = Server.DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            }

            catch (NumberFormatException exception) {
                System.out.println("Invalid port, using " + port + ".");
            }
        }

        Server server = new Server(port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            Thread.currentThread().join();
        }

        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            server.stop();
        }
    }
}
