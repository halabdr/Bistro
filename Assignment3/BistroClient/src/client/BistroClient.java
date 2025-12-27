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
     * Constructs a BistroClient object.
     *
     * @param host the server address (like "localhost")
     * @param port the server port number
     */


    public BistroClient(String host, int port) {
        super(host, port);// Initialize the AbstractClient with host and port
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
     * This method is automatically called by the OCSF framework
     * whenever a message arrives from the server.
     *
     * @param msg the message received from the server
     */

    @Override
    protected void handleMessageFromServer(Object msg) {
    	// Print the message for debugging purposes
        System.out.println("SERVER -> " + msg);
        
     // Forward the message to the registered listener (if exists)
        if (listener != null) {
        	javafx.application.Platform.runLater(() -> listener.onMessage(msg));
        }
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
     * Closes the connection to the server if currently connected.
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
    //h

	

	


