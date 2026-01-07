package server;

import common.ChatIF;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import java.util.function.IntConsumer;

/**
 * The BistroServer class represents the server side of the Bistro system.
 * It receives messages from clients and routes them to the ServerController.
 */
public class BistroServer extends AbstractServer {

    public static final int DEFAULT_PORT = 5555;

    private final ServerController serverController;

    // UI logger (Server GUI or console)
    private ChatIF ui;

    // connected clients counter (optional, for GUI)
    private int clientsCount = 0;
    private IntConsumer clientsCountConsumer;

    public BistroServer(int port) {
        super(port);
        this.serverController = new ServerController();
    }

    /**
     * Optional: attach a UI logger and a client-count callback (for Server GUI).
     */
    public void setUI(ChatIF ui, IntConsumer clientsCountConsumer) {
        this.ui = ui;
        this.clientsCountConsumer = clientsCountConsumer;
    }

    private void log(String s) {
        if (ui != null) ui.display(s);
        else System.out.println(s);
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        clientsCount++;
        if (clientsCountConsumer != null) clientsCountConsumer.accept(clientsCount);
        log("Client connected: " + client);
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        clientsCount = Math.max(0, clientsCount - 1);
        if (clientsCountConsumer != null) clientsCountConsumer.accept(clientsCount);
        log("Client disconnected: " + client);
    }

    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        log("Client exception: " + client + " (" + exception.getMessage() + ")");

        if (client != null && client.getInfo("Disconnected") == null) {
            client.setInfo("Disconnected", true);
            log("Handling unexpected client disconnection.");
        }
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        log("Received message: " + msg);
        serverController.handleRequest(msg, client);
    }

    @Override
    protected void serverStarted() {
        log("BistroServer started and is listening on port " + getPort());
    }

    @Override
    protected void serverStopped() {
        log("BistroServer stopped.");
    }
}
