package client;

public final class commands {
    private commands() {}

    public static final String GET_AVAILABLE_SLOTS = "GET_AVAILABLE_SLOTS";
    public static final String CREATE_RESERVATION  = "CREATE_RESERVATION";
    public static final String CANCEL_RESERVATION  = "CANCEL_RESERVATION";
    public static final String SEAT_BY_CODE        = "SEAT_BY_CODE";
    public static final String LOST_CODE           = "LOST_CODE";
    public static final String PAY_BILL            = "PAY_BILL";
    public static final String GET_AVAILABLE_DATES = "GET_AVAILABLE_DATES";
}
