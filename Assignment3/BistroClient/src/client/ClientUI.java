package client;

import common.ChatIF;

/**
 * A simple console-based implementation of {@link ChatIF}.
 * Used mainly for logging/debugging client messages.
 * The actual GUI is implemented using JavaFX controllers.
 */

public class ClientUI implements ChatIF {
	 /**
     * Displays a message in the console.
     *
     * @param message the message to be displayed
     */
    @Override
    public void display(String message) {
        System.out.println(message);
    }
}
