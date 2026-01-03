package server;

import common.AvailableSlot;
import common.AvailableSlotsResponse;
import common.CommandType;
import common.GetAvailableSlotsQuery;
import common.Message;
import database.InmemoryStore;
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

	private final InmemoryStore store = new InmemoryStore();
	private final AvailabilityService availabilityService = new AvailabilityService(store);
	
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
    private void handleGetAvailableSlots(Message request, ConnectionToClient client) 
    {
        GetAvailableSlotsQuery q = (GetAvailableSlotsQuery) request.getData();
        var slots = availabilityService.getAvailableSlots(q.getDate(), q.getNumOfDiners());
        safeSend(client, Message.ok(CommandType.GET_AVAILABLE_SLOTS, new AvailableSlotsResponse(slots)));

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