package server;

import common.AvailableSlot;
import common.CommandType;
import common.GetAvailableSlotsQuery;
import common.Message;
import ocsf.server.ConnectionToClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The ServerController is responsible for handling requests from client.
 * It is responsible for checking the command type and
 * routing the request to the correct handler methods
 *
 * This class separates server communication logic from
 * application business logic.
 */
public class ServerController {

	/**
     * Handles a request sent by a client.
     * The method checks that the message is valid and
     * processes it according to its command type.
     * 
     * @param msg the request object sent by the client
     * @param client the client connection
     */
    public void handleRequest(Object msg, ConnectionToClient client) {
        if (!(msg instanceof Message)) 
        {
            safeSend(client, Message.fail(CommandType.GET_AVAILABLE_SLOTS, "Invalid message type"));
            return;
        }

        Message request = (Message) msg;

        try {
            switch (request.getCommand()) 
            {
                case GET_AVAILABLE_SLOTS -> handleGetAvailableSlots(request, client);
                default -> safeSend(client, Message.fail(request.getCommand(), "Unsupported command"));
            }
        } catch (Exception e) {
            safeSend(client, Message.fail(request.getCommand(), "Server error: " + e.getMessage()));
        }
    }

    /**
     * Handles a request for available reservation time slots.
     * The server creates a list of available slots and
     * sends it back to the client.
     *
     * @param request the request message
     * @param client the client connection
     * @throws Exception if an error occurs while handling the request
     */
    private void handleGetAvailableSlots(Message request, ConnectionToClient client) throws Exception 
    {
        if (!(request.getData() instanceof GetAvailableSlotsQuery)) 
        {
            safeSend(client, Message.fail(CommandType.GET_AVAILABLE_SLOTS, "Payload must be GetAvailableSlotsQuery"));
            return;
        }

        GetAvailableSlotsQuery q = (GetAvailableSlotsQuery) request.getData();

        List<AvailableSlot> slots = new ArrayList<>();
        LocalDateTime base = q.getDate().atTime(18, 0);
        slots.add(new AvailableSlot(base));
        slots.add(new AvailableSlot(base.plusMinutes(30)));
        slots.add(new AvailableSlot(base.plusMinutes(60)));

        safeSend(client, Message.ok(CommandType.GET_AVAILABLE_SLOTS, slots));
    }

    /**
     * Sends a response to the client.
     * Any exception during sending is ignored to
     * prevent the server from crashing.
     *
     * @param client the client connection
     * @param response the response to send
     */
    private void safeSend(ConnectionToClient client, Object response) 
    {
        try 
        {
            client.sendToClient(response);
        } catch (Exception ignored) {}
    }
}