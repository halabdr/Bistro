package connection;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import common.ChatIF;
import common.Message;
import data_access.*;
import java.util.function.IntConsumer;

/**
 * The BistroServer class represents the server side of the Bistro system.
 * It receives messages from clients and routes them to appropriate Repository handlers.
 * Extends OCSF AbstractServer for network communication.
 */
public class BistroServer extends AbstractServer {

    public static final int DEFAULT_PORT = 5555;

    // Repository instances
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final TableRepository tableRepository;
    private final BillRepository billRepository;
    private final OpeningHoursRepository openingHoursRepository;

    // UI logger (Server GUI or console)
    private ChatIF ui;

    // Connected clients counter for GUI display
    private int clientsCount = 0;
    private IntConsumer clientsCountConsumer;

    /**
     * Creates a new BistroServer.
     * Initializes all Repository instances.
     * 
     * @param port the port number to listen on
     */
    public BistroServer(int port) {
        super(port);
        this.userRepository = new UserRepository();
        this.reservationRepository = new ReservationRepository();
        this.waitlistRepository = new WaitlistRepository();
        this.tableRepository = new TableRepository();
        this.billRepository = new BillRepository();
        this.openingHoursRepository = new OpeningHoursRepository();
    }

    /**
     * Attaches a UI logger and client count callback for the Server GUI.
     * 
     * @param ui the UI interface for displaying messages
     * @param clientsCountConsumer callback to update client count in GUI
     */
    public void setUI(ChatIF ui, IntConsumer clientsCountConsumer) {
        this.ui = ui;
        this.clientsCountConsumer = clientsCountConsumer;
    }

    /**
     * Logs a message to the UI or console.
     * 
     * @param message the message to log
     */
    private void log(String message) {
        if (ui != null) {
            ui.display(message);
        } else {
            System.out.println(message);
        }
    }

    @Override
    protected void serverStarted() {
        log("[Server] BistroServer started and listening on port " + getPort());
        log("[Server] Connection pool initialized.");
    }

    @Override
    protected void serverStopped() {
        log("[Server] BistroServer stopped.");
        MySQLConnectionPool.getInstance().shutdown();
        log("[Server] Connection pool shutdown complete.");
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        clientsCount++;
        if (clientsCountConsumer != null) {
            clientsCountConsumer.accept(clientsCount);
        }
        
        try {
            String clientIP = client.getInetAddress().getHostAddress();
            log("[Server] Client connected - IP: " + clientIP + " | Active clients: " + clientsCount);
        } catch (Exception e) {
            log("[Server] Client connected | Active clients: " + clientsCount);
        }
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        clientsCount = Math.max(0, clientsCount - 1);
        if (clientsCountConsumer != null) {
            clientsCountConsumer.accept(clientsCount);
        }
        log("[Server] Client disconnected. Active clients: " + clientsCount);
    }

    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        log("[Server] Client exception: " + exception.getMessage());
        clientDisconnected(client);
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (!(msg instanceof Message)) {
            log("[Server] Invalid message type received");
            safeSend(client, Message.fail("UNKNOWN", "Invalid message type"));
            return;
        }

        Message request = (Message) msg;
        log("[Server] Request received: " + request.getCommand());

        try {
            routeRequest(request, client);
        } catch (Exception e) {
            log("[Server] Error handling request: " + e.getMessage());
            e.printStackTrace();
            safeSend(client, Message.fail(request.getCommand(), "Server error: " + e.getMessage()));
        }
    }

    /**
     * Routes incoming requests to appropriate Repository handlers.
     * 
     * @param request the message from client
     * @param client the client connection
     */
    private void routeRequest(Message request, ConnectionToClient client) {
        String command = request.getCommand();
        Message response;

        switch (command) {
            // User Management
            case "LOGIN" -> {
                response = userRepository.login(request);
                safeSend(client, response);
            }
            case "REGISTER_SUBSCRIBER" -> {
                response = userRepository.registerSubscriber(request);
                safeSend(client, response);
            }
            case "GET_USER" -> {
                response = userRepository.getUser(request);
                safeSend(client, response);
            }
            case "UPDATE_USER" -> {
                response = userRepository.updateUser(request);
                safeSend(client, response);
            }

            // Reservation Management
            case "GET_AVAILABLE_SLOTS" -> {
                response = reservationRepository.getAvailableSlots(request);
                safeSend(client, response);
            }
            case "CREATE_RESERVATION" -> {
                response = reservationRepository.createReservation(request);
                safeSend(client, response);
            }
            case "CANCEL_RESERVATION" -> {
                response = reservationRepository.cancelReservation(request);
                safeSend(client, response);
            }
            case "GET_RESERVATIONS" -> {
                response = reservationRepository.getAllReservations(request);
                safeSend(client, response);
            }
            case "GET_USER_RESERVATIONS" -> {
                response = reservationRepository.getUserReservations(request);
                safeSend(client, response);
            }
            case "LOST_CODE" -> {
                response = reservationRepository.retrieveLostCode(request);
                safeSend(client, response);
            }

            // Waitlist Management
            case "JOIN_WAITLIST" -> {
                response = waitlistRepository.joinWaitlist(request);
                safeSend(client, response);
            }
            case "LEAVE_WAITLIST" -> {
                response = waitlistRepository.leaveWaitlist(request);
                safeSend(client, response);
            }
            case "GET_WAITLIST" -> {
                response = waitlistRepository.getWaitlist(request);
                safeSend(client, response);
            }
            case "LOST_CODE_WAITLIST" -> {
                response = waitlistRepository.retrieveLostCode(request);
                safeSend(client, response);
            }

            // Table Management
            case "GET_TABLES" -> {
                response = tableRepository.getAllTables(request);
                safeSend(client, response);
            }
            case "GET_AVAILABLE_TABLES" -> {
                response = tableRepository.getAvailableTables(request);
                safeSend(client, response);
            }
            case "ADD_TABLE" -> {
                response = tableRepository.addTable(request);
                safeSend(client, response);
            }
            case "UPDATE_TABLE" -> {
                response = tableRepository.updateTable(request);
                safeSend(client, response);
            }
            case "DELETE_TABLE" -> {
                response = tableRepository.deleteTable(request);
                safeSend(client, response);
            }
            case "OCCUPY_TABLE" -> {
                response = tableRepository.occupyTable(request);
                safeSend(client, response);
            }
            case "RELEASE_TABLE" -> {
                response = tableRepository.releaseTable(request);
                safeSend(client, response);
            }

            // Bill Management
            case "CREATE_BILL" -> {
                response = billRepository.createBill(request);
                safeSend(client, response);
            }
            case "GET_BILL" -> {
                response = billRepository.getBill(request);
                safeSend(client, response);
            }
            case "PAY_BILL" -> {
                response = billRepository.payBill(request);
                safeSend(client, response);
            }

            // Opening Hours Management
            case "GET_OPENING_HOURS" -> {
                response = openingHoursRepository.getOpeningHours(request);
                safeSend(client, response);
            }
            case "UPDATE_OPENING_HOURS" -> {
                response = openingHoursRepository.updateOpeningHours(request);
                safeSend(client, response);
            }
            case "GET_SPECIAL_HOURS" -> {
                response = openingHoursRepository.getSpecialHours(request);
                safeSend(client, response);
            }
            case "ADD_SPECIAL_HOURS" -> {
                response = openingHoursRepository.addSpecialHours(request);
                safeSend(client, response);
            }
            case "DELETE_SPECIAL_HOURS" -> {
                response = openingHoursRepository.deleteSpecialHours(request);
                safeSend(client, response);
            }

            default -> {
                log("[Server] Unknown command: " + command);
                safeSend(client, Message.fail(command, "Unknown command"));
            }
        }
    }

    /**
     * Safely sends a response to the client.
     * Catches exceptions to prevent server crashes.
     * 
     * @param client the client connection
     * @param response the response to send
     */
    private void safeSend(ConnectionToClient client, Object response) {
        try {
            client.sendToClient(response);
        } catch (Exception e) {
            log("[Server] Failed to send response to client: " + e.getMessage());
        }
    }
}

