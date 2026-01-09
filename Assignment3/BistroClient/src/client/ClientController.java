package client;

import common.Message;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

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

    void deliver(Object msg) {
        if (listener != null) listener.onMessage(msg);
    }

    

    public void requestAvailableSlots(LocalDate date, int guestCount) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("date", date.toString());       
        data.put("guestCount", guestCount);        

        client.sendToServer(new Message("GET_AVAILABLE_SLOTS", data));
    }

    public void createReservation(LocalDate date, LocalTime time, int guestCount, String subscriberNumber) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("bookingDate", date.toString());
        data.put("bookingTime", time.toString());  
        data.put("guestCount", guestCount);
        data.put("subscriberNumber", subscriberNumber); 

        client.sendToServer(new Message("CREATE_RESERVATION", data));
    }

    public void cancelReservation(String confirmationCode) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("confirmationCode", confirmationCode);

        client.sendToServer(new Message("CANCEL_RESERVATION", data));
    }
}
