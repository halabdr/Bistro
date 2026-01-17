package client;

/**
 * Constants for client-server command types.
 * Each constant represents a specific operation that can be requested from the server.
 * Commands are used in Message objects to identify the type of request/response.
 */
public final class Commands {
    
    private Commands() {}

    // User
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_BY_SUBSCRIBER_NUMBER = "LOGIN_BY_SUBSCRIBER_NUMBER";
    public static final String REGISTER_SUBSCRIBER = "REGISTER_SUBSCRIBER";
    public static final String GET_USER = "GET_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String LOST_CODE = "LOST_CODE";
    public static final String GET_ALL_SUBSCRIBERS = "GET_ALL_SUBSCRIBERS";
    public static final String GET_SUBSCRIBER_BY_NUMBER = "GET_SUBSCRIBER_BY_NUMBER";

    // Reservation
    public static final String GET_AVAILABLE_SLOTS = "GET_AVAILABLE_SLOTS";
    public static final String CREATE_RESERVATION = "CREATE_RESERVATION";
    public static final String CANCEL_RESERVATION = "CANCEL_RESERVATION";
    public static final String GET_RESERVATIONS = "GET_RESERVATIONS";
    public static final String GET_USER_RESERVATIONS = "GET_USER_RESERVATIONS";
    public static final String GET_ALTERNATIVE_SLOTS = "GET_ALTERNATIVE_SLOTS";

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
    public static final String LOST_CODE_WAITLIST = "LOST_CODE_WAITLIST";

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
    
    // Staff
    public static final String GET_NOTIFICATION_LOG = "GET_NOTIFICATION_LOG";
    public static final String GET_TIME_REPORT = "GET_TIME_REPORT";
    public static final String GET_SUBSCRIBERS_REPORT = "GET_SUBSCRIBERS_REPORT";
    public static final String GET_CURRENT_DINERS = "GET_CURRENT_DINERS";

    // Reports (Manager/Staff)
    public static final String GET_MONTHLY_REPORTS_LIST = "GET_MONTHLY_REPORTS_LIST";
    public static final String GENERATE_REPORTS = "GENERATE_REPORTS";
    
    // Waitlist -> Terminal
    public static final String CHECK_AVAILABILITY_TERMINAL = "CHECK_AVAILABILITY_TERMINAL";
    public static final String GET_RESERVATIONS_BY_CARD = "GET_RESERVATIONS_BY_CARD";
}