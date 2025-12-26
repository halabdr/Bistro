package common;

import java.io.Serializable;

/**
 * Represents a message that is sent between the client and the server.
 * Each message contains a command type and optional data.
 * It can also represent a successful response or an error.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CommandType command;
    private final Object data;

    private final boolean success;
    private final String error;

    /**
     * Creates a new message.
     */
    private Message(CommandType command, Object data, boolean success, String error) 
    {
        this.command = command;
        this.data = data;
        this.success = success;
        this.error = error;
    }

    /**
     * Creates a successful message with data.
     *
     * @param command the command type
     * @param data the data to send
     */
    public Message(CommandType command, Object data) 
    {
        this(command, data, true, null);
    }

    /**
     * Creates a successful response message.
     *
     * @param command the command type
     * @param data the returned data
     * @return a success message
     */
    public static Message ok(CommandType command, Object data) 
    {
        return new Message(command, data, true, null);
    }
    
    /**
     * Creates a failure response message.
     *
     * @param command the command type
     * @param error error description
     * @return a failure message
     */
    public static Message fail(CommandType command, String error) 
    {
        return new Message(command, null, false, error);
    }
   
    /**
     * @return the command type
     */
    public CommandType getCommand() 
    { 
    	    return command; 
    }
    
    /**
     * @return the message data
     */
    public Object getData() 
    { 
    	    return data; 
    	}
    
    /**
     * @return true if the operation succeeded
     */
    public boolean isSuccess() 
    { 
    	    return success; 
    	}
    
    /**
     * @return error message if failed, null otherwise
     */
    public String getError() 
    { 
    	    return error; 
    	}
}