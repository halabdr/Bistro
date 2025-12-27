package client;

import java.io.IOException;

/**
 * ClientController
 * This class acts as a middle layer between the client UI (or other client logic)
 * and the network communication layer (BistroClient).
 *
 * Its responsibility is to:
 *   Manage the connection lifecycle (connect / disconnect)
 *   Forward messages from the UI to the server
 *   Register listeners for incoming server messages
 *
 * This separation allows the UI code to remain independent
 * of low-level networking details.
 */
public class ClientController {

    /**
     * The actual client responsible for communication with the server.
     */
    private final BistroClient client;

    /**
     * Constructs a ClientController and initializes the underlying BistroClient.
     *
     * @param host the server address (e.g., "localhost")
     * @param port the server port number
     * @throws IOException if an error occurs while creating the client
     */
    public ClientController(String host, int port) throws IOException {
        this.client = new BistroClient(host, port);
    }

    /**
     * Sets a message listener that will be notified when
     * messages are received from the server.
     *
     * @param listener an implementation of MessageListener
     */
    public void setListener(MessageListener listener) {
        client.setListener(listener);
    }

    /**
     * Opens a connection to the server.
     *
     * @throws IOException if the connection fails
     */
    public void connect() throws IOException {
        client.connect();
    }

    /**
     * Closes the connection to the server.
     *
     * @throws IOException if disconnection fails
     */
    public void disconnect() throws IOException {
        client.disconnect();
    }

    /**
     * Sends a message to the server.
     *
     * @param msg the message object to be sent
     * @throws IOException if sending fails
     */
    public void send(Object msg) throws IOException {
        client.send(msg);
    }
}

