package client;

import ocsf.client.AbstractClient;
import java.io.IOException;
import javafx.application.Platform;


/**
 * BistroClient – responsible for network communication with the server.
 * It is responsible for:
 *  Opening and closing the connection to the server
 *  Sending messages to the server
 *  Receiving messages from the server
 *
 * The class extends AbstractClient from the OCSF framework,
 * which provides the low-level socket communication.
 */

public class BistroClient extends AbstractClient{
	/**
     * Listener used to forward messages received from the server
     * to higher-level client logic (like controller or UI).
     */
	private MessageListener listener;
	
	/**
	 * Flag used to ensure that disconnection handling logic is executed only once.
	 */
	private volatile boolean disconnectedHandled = false;
	
    /**
     * Constructs a BistroClient object.
     *
     * @param host the server address
     * @param port the server port number
     */
    public BistroClient(String host, int port) {
        super(host, port);
    }
    
    /**
     * Sets a listener that will be notified whenever
     * a message is received from the server.
     *
     * @param listener an implementation of MessageListener
     */
    public void setListener(MessageListener listener) {
        this.listener = listener;
    }
    
    /**
     * Opens a connection to the server if not already connected.
     *
     * @throws IOException if an error occurs while opening the connection
     */
    public void connect() throws IOException {
        if (!isConnected()) {
            openConnection();
        }
    }
    /**
     * Handles a disconnection safely, even if it happens more than once
     *
     * This method checks whether the client is currently connected before
     * attempting to close the connection in order to prevent unnecessary
     * operations or exceptions.
     * 
     * @throws IOException if an error occurs while closing the connection
     */
    public void disconnect() throws IOException {
        if (isConnected()) {
            closeConnection();
        }
    }
    
    /**
     * Sends a message to the server.
     *
     * @param msg the message object to send
     * @throws IOException if an error occurs while sending the message
     */
    public void send(Object msg) throws IOException {
        sendToServer(msg);
    }
    
    /**
     * This method is automatically called by the OCSF framework
     * whenever a message arrives from the server.
     *
     * @param msg the message received from the server
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        System.out.println("SERVER -> " + msg);
        
        if (listener != null) {
        	javafx.application.Platform.runLater(() -> listener.onMessage(msg));
        }
    }
    
    /**
     * This callback is typically triggered when the server explicitly
     * closes the connection or stops listening for incoming clients.
     */
    @Override
    protected void connectionClosed() {
        handleDisconnectOnce("connectionClosed");
    }

    /**
     * This callback is usually triggered when the server crashes,
     * the network connection is lost, or an I/O error occurs.
     * 
     * @param exception the exception that caused the connection failure
     */
    @Override
    protected void connectionException(Exception exception) {
        handleDisconnectOnce("connectionException: " + exception.getMessage());
    }
    
    /**
     * This method ensures that disconnection handling logic is executed
     * only once, even if multiple disconnection-related callbacks.
     *
     * @param reason a textual description of the disconnection reason
     */
    private void handleDisconnectOnce(String reason) {
        if (disconnectedHandled) return;
        disconnectedHandled = true;
        
        if (listener != null) {
        	Platform.runLater(() -> listener.onMessage("DISCONNECTED: " + reason));
        }
    }
}