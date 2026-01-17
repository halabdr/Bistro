package client;

import common.Message;

/**
 * Interface for handling incoming messages from the server.
 * Controllers implement this interface to receive and process server responses.
 */
public interface MessageListener {

    /**
     * Called when a message is received from the server.
     * Implementations should check the message command and handle accordingly.
     * 
     * @param message the message received from the server
     */
    void onMessage(Message message);
}