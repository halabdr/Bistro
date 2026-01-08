package server;

import common.CreateReservationResponse;

import common.AvailableSlotsResponse;
import common.CommandType;
import common.CreateReservationRequest;
import common.GetAvailableSlotsQuery;
import common.Message;
import database.InmemoryStore;
import entities.Reservation;
import ocsf.server.ConnectionToClient;

import java.time.LocalDateTime;

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
                case CREATE_RESERVATION -> handleCreateReservation(request, client);
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
   
    private void handleCreateReservation(Message request,ConnectionToClient client) {
           
    	CreateReservationRequest req = (CreateReservationRequest) request.getData();
       // int tableId = availabilityService.allocateTableForSeating(null);    //allocateTable(req.getDateTime(), req.getNumOfDiners());
     //   if (tableId == -1) {
       //    safeSend(client,Message.fail(CommandType.CREATE_RESERVATION,"No available table"));
         //  return;
        //}

        Reservation reservation;

        if (req.isSubscriber()) {
           reservation = Reservation.createForSubscriber(
                         req.getDateTime(),
                         req.getNumOfDiners(),
                         req.getSubscriberId()
                         );
        } 
        else {
             reservation = Reservation.createForGuest(
                           req.getDateTime(),
                           req.getNumOfDiners(),
                           req.getGuestPhone(),
                           req.getGuestEmail()
                           );
        }
        
        reservation.setAssignedTableNumber(0);

        store.addReservation(reservation);

        LocalDateTime startTime = reservation.getStartDateTime();
        LocalDateTime endTime = reservation.getEndDateTime();

        CreateReservationResponse response = new CreateReservationResponse(reservation.getConfirmationCode(),
                                                                           startTime,endTime);
        safeSend(client,Message.ok(CommandType.CREATE_RESERVATION, response));
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