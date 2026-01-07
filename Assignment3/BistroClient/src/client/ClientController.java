package client;

import java.io.IOException;
import java.time.LocalDate;

import common.CommandType;
import common.GetAvailableSlotsQuery;
import common.Message;

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
    public ClientController(String host, int port) throws IOException{
    	 // Initialize the network client with server address and port
    	this.client = new BistroClient(host, port, new ClientUI());
    }

    /**
     * Sets a message listener that will be notified when
     * messages are received from the server.
     *
     * @param listener an implementation of MessageListener
     */
    public void setListener(MessageListener listener) {
    	// Store the listener inside the network client so it can be notified
        // when messages arrive from the server.
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
    
    /**
     * Indicates whether the underlying OCSF client is currently connected to the server.
     *
     * @return {@code true} if connected, otherwise {@code false}
     */
   
    
    public boolean isConnected() {
    	return client != null && client.isConnected() ;
    }
    
    /**
     * Sends a request to the server to retrieve available reservation slots.
     *
     * @param date the requested reservation date
     * @param numOfDiners number of diners
     * @throws IOException if sending the request fails
     */

    
    public void requestAvailableSlots(LocalDate date, int NumOfDiners) throws IOException {
    	// Build the request message with the required command and parameters
        Message msg = new Message(CommandType.GET_AVAILABLE_SLOTS, new GetAvailableSlotsQuery(date, NumOfDiners));
        send(msg);
    }
}

