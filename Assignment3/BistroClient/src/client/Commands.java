package client;

/**
 * Centralized list of protocol command names used by the client and server.
 * Keep these values identical to the server-side switch(command) cases.
 */
public final class Commands {
    
	private Commands() {}

    // User
    public static final String LOGIN = "LOGIN";
    public static final String REGISTER_SUBSCRIBER = "REGISTER_SUBSCRIBER";
    public static final String GET_USER = "GET_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String LOST_CODE = "LOST_CODE";


    // Reservation
    public static final String GET_AVAILABLE_SLOTS = "GET_AVAILABLE_SLOTS";
    public static final String CREATE_RESERVATION = "CREATE_RESERVATION";
    public static final String CANCEL_RESERVATION = "CANCEL_RESERVATION";
    public static final String GET_RESERVATIONS = "GET_RESERVATIONS";
    public static final String GET_USER_RESERVATIONS = "GET_USER_RESERVATIONS";

    // Waitlist
    public static final String JOIN_WAITLIST = "JOIN_WAITLIST";
    public static final String LEAVE_WAITLIST = "LEAVE_WAITLIST";
    public static final String GET_WAITLIST = "GET_WAITLIST";

    // Tables
    public static final String GET_TABLES = "GET_TABLES";
    public static final String GET_AVAILABLE_TABLES = "GET_AVAILABLE_TABLES";
    public static final String ADD_TABLE = "ADD_TABLE";
    public static final String UPDATE_TABLE = "UPDATE_TABLE";
    public static final String DELETE_TABLE = "DELETE_TABLE";
    public static final String OCCUPY_TABLE = "OCCUPY_TABLE";
    public static final String RELEASE_TABLE = "RELEASE_TABLE";
    public static final String SEAT_BY_CODE = "SEAT_BY_CODE";

    // Bills
    public static final String CREATE_BILL = "CREATE_BILL";
    public static final String GET_BILL = "GET_BILL";
    public static final String PAY_BILL = "PAY_BILL";

    // Opening Hours
    public static final String GET_OPENING_HOURS = "GET_OPENING_HOURS";
    public static final String UPDATE_OPENING_HOURS = "UPDATE_OPENING_HOURS";
    public static final String GET_SPECIAL_HOURS = "GET_SPECIAL_HOURS";
    public static final String ADD_SPECIAL_HOURS = "ADD_SPECIAL_HOURS";
    public static final String DELETE_SPECIAL_HOURS = "DELETE_SPECIAL_HOURS";
}
