package client;

import java.io.IOException;
import javafx.application.Platform;

public class ClientUI implements MessageListener {

    private static BistroClient client;
    private static MessageListener uiListener; 

    /**
     * UI determines which Listener receives messages from the server.
     */
    public static void setUiListener(MessageListener listener) {
        uiListener = listener;
    }

    /**
     * Creates a client and connects to the server.
     */
    public static void connect(String host, int port) throws IOException {
        client = new BistroClient(host, port);
        client.setListener(new ClientUI()); 
        client.connect();
    }

    /**
     * Orderly disconnection.     
     */
    public static void disconnect() throws IOException {
        if (client != null) {
            client.disconnect();
        }
    }

    /**
     * Send message to the server.
     */
    public static void send(Object msg) throws IOException {
        if (client == null) {
            throw new IllegalStateException("Client is not initialized. Call connect() first.");
        }
        client.send(msg);
    }

    /**
    * Every message from the server goes here.
    * From here we pass to the Controller via uiListener.
    */
    @Override
    public void onMessage(Object msg) {
        System.out.println("ClientUI received: " + msg);

        if (uiListener != null) {
            Platform.runLater(() -> uiListener.onMessage(msg));
        }
    }
}