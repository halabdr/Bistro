package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * The BistroServer class represents the server side of the Bistro system.
 * It is responsible for receiving messages from clients and handling
 *
 */
public class BistroServer extends AbstractServer {
	/**
     * The server controller responsible for routing client requests.
     */
    private ServerController serverController;

    /**
    /**
     * Constructs a new BistroServer with the specified port number
     *
     * @param port the port number on which the server listens for connections
     */
    public BistroServer(int port) {
        super(port);
        this.serverController = new ServerController();
    }

    /**
     * Handles messages received from connected clients
     * 
     * This method is automatically called by the OCSF framework
     * whenever a message is sent from a client to the server
     *
     * @param msg the message received from the client
     * @param client the client that sent the message
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("Client connected: " + client);
    }


    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    	serverController.handleRequest(msg, client);
    }

    /**
     * This method is called when the server starts listening for connections
     */
    @Override
    protected void serverStarted() {
        System.out.println("BistroServer started and is listening for clients.");
    }

    /**
     * This method is called when the server stops listening for connections
     */
    @Override
    protected void serverStopped() {
        System.out.println("BistroServer stopped.");
    }
}
