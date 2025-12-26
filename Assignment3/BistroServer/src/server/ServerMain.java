package server;

/**
 * Entry point of the server application.
 * This class starts the Bistro server and makes it listen
 * for incoming client connections.
 */
public class ServerMain {
	
	 /**
     * Starts the server.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        int port = BistroServer.DEFAULT_PORT;
        BistroServer server = new BistroServer(port);

        try {
            server.listen();
            System.out.println("Server is listening on port " + port);
        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());
        }
    }
}