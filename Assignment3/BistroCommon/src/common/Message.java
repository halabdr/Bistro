package common;

import java.io.Serializable;

/**
 * Represents a message that is sent between the client and the server.
 * Each message contains a command string and optional data.
 * It can also represent a successful response or an error.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String command;
    private final Object data;
    private final boolean success;
    private final String error;

    /**
     * Creates a new message.
     */
    private Message(String command, Object data, boolean success, String error) {
        this.command = command;
        this.data = data;
        this.success = success;
        this.error = error;
    }

    /**
     * Creates a successful message with data.
     *
     * @param command the command string
     * @param data the data to send
     */
    public Message(String command, Object data) {
        this(command, data, true, null);
    }

    /**
     * Creates a successful response message.
     *
     * @param command the command string
     * @param data the returned data
     * @return a success message
     */
    public static Message ok(String command, Object data) {
        return new Message(command, data, true, null);
    }
    
    /**
     * Creates a failure response message.
     *
     * @param command the command string
     * @param error error description
     * @return a failure message
     */
    public static Message fail(String command, String error) {
        return new Message(command, null, false, error);
    }
   
    /**
     * Gets the command string.
     * 
     * @return the command string
     */
    public String getCommand() {
        return command;
    }
    
    /**
     * Gets the message data.
     * 
     * @return the message data
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Checks if the operation succeeded.
     * 
     * @return true if the operation succeeded
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Gets the error message if failed.
     * 
     * @return error message if failed, null otherwise
     */
    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "Message{" +
                "command='" + command + '\'' +
                ", success=" + success +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}