package client;

import common.Message;
import entities.OpeningHours;
import entities.SpecialHours;
import entities.Table;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Facade for client -> server requests.
 * Also dispatches server responses to the active UI listener.
 */
public class ClientController {

    private final BistroClient client;
    private MessageListener listener;

    public ClientController(BistroClient client) {
        this.client = client;
        this.client.setController(this);
    }

    public void setListener(MessageListener l) {
        this.listener = l;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void connect() throws IOException {
        client.openConnection();
    }

    public void disconnect() throws IOException {
        client.closeConnection();
    }

    // Called by BistroClient when message arrives
    void deliver(Object msg) {
        if (listener == null) return;
        if (msg instanceof Message m) {
            listener.onMessage(m);
        }
    }

    // ---------------- User ----------------

    public void login(String email, String password) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        client.sendToServer(new Message(commands.LOGIN, data));
    }

    // ---------------- Reservation (customer + staff) ----------------

    public void getAvailableSlots(LocalDate date, int guests) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("date", date.toString());
        data.put("guestCount", guests);
        client.sendToServer(new Message(commands.GET_AVAILABLE_SLOTS, data));
    }

    public void createReservation(LocalDate date, LocalTime time, int guestCount, String subscriberNumber) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("bookingDate", date.toString());
        data.put("bookingTime", time.toString());
        data.put("guestCount", guestCount);
        data.put("subscriberNumber", subscriberNumber);
        client.sendToServer(new Message(commands.CREATE_RESERVATION, data));
    }

    public void cancelReservation(String confirmationCode) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("confirmationCode", confirmationCode);
        client.sendToServer(new Message(commands.CANCEL_RESERVATION, data));
    }

    /** Staff: all reservations */
    public void getAllReservations() throws IOException {
        client.sendToServer(new Message(commands.GET_RESERVATIONS, null));
    }

    /** Staff/customer: reservations of a subscriber */
    public void getUserReservations(String subscriberNumber) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("subscriberNumber", subscriberNumber);
        client.sendToServer(new Message(commands.GET_USER_RESERVATIONS, data));
    }

    // ---------------- Waitlist (staff) ----------------

    public void getWaitlist() throws IOException {
        client.sendToServer(new Message(commands.GET_WAITLIST, null));
    }

    // ---------------- Tables (staff) ----------------

    public void getTables() throws IOException {
        client.sendToServer(new Message(commands.GET_TABLES, null));
    }

    public void addTable(int tableNumber, int seatCapacity, String tableLocation) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("tableNumber", tableNumber);
        data.put("seatCapacity", seatCapacity);
        data.put("tableLocation", tableLocation);
        client.sendToServer(new Message(commands.ADD_TABLE, data));
    }

    public void updateTable(Table table) throws IOException {
        client.sendToServer(new Message(commands.UPDATE_TABLE, table));
    }

    public void deleteTable(int tableNumber) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("tableNumber", tableNumber);
        client.sendToServer(new Message(commands.DELETE_TABLE, data));
    }

    // ---------------- Opening hours (staff) ----------------

    public void getOpeningHours() throws IOException {
        client.sendToServer(new Message(commands.GET_OPENING_HOURS, null));
    }

    public void updateOpeningHours(OpeningHours hours) throws IOException {
        client.sendToServer(new Message(commands.UPDATE_OPENING_HOURS, hours));
    }

    // ---------------- Special hours (staff) ----------------

    public void getSpecialHours() throws IOException {
        client.sendToServer(new Message(commands.GET_SPECIAL_HOURS, null));
    }

    public void addSpecialHours(SpecialHours special) throws IOException {
        client.sendToServer(new Message(commands.ADD_SPECIAL_HOURS, special));
    }

    public void deleteSpecialHours(int specialId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("specialHoursId", specialId);
        client.sendToServer(new Message(commands.DELETE_SPECIAL_HOURS, data));
    }
}