package reservationgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CreateReservationController implements MessageListener {

    @FXML private Label dateValue;
    @FXML private Label timeValue;
    @FXML private Label guestsValue;
    @FXML private Label subscriberValue;
    @FXML private TextArea notesArea;
    @FXML private Label statusLabel;

    private ClientController controller;

    private LocalDate date;
    private String hhmm;
    private int guests;

    private Subscriber subscriber; // who is booking

    public void init(ClientController controller, LocalDate date, String hhmm, int guests) {
        this.controller = controller;
        this.controller.setListener(this);

        this.date = date;
        this.hhmm = hhmm;
        this.guests = guests;

        // Pull the logged-in subscriber from session (you already have this idea in your app)
        // If you haven't stored it yet, you'll need to store it in ConnectApp when login succeeds.
        this.subscriber = ConnectApp.getCurrentSubscriber();

        // Fill the summary UI
        dateValue.setText(date == null ? "-" : date.toString());
        timeValue.setText(hhmm == null ? "-" : hhmm);
        guestsValue.setText(String.valueOf(guests));

        if (subscriber != null) {
            // adjust getters according to your Subscriber class
            subscriberValue.setText(subscriber.getSubscriberNumber() + "");
        } else {
            subscriberValue.setText("Unknown (not in session)");
        }

        statusLabel.setText("");
    }

    @FXML
    private void onBack() throws Exception {
        // go back to search screen
        ConnectApp.showReservationSearch();
    }

    @FXML
    private void onConfirm() {
        try {
            if (subscriber == null) {
                statusLabel.setText("No subscriber in session. Please login again.");
                return;
            }
            if (date == null || hhmm == null || hhmm.isBlank()) {
                statusLabel.setText("Missing reservation details.");
                return;
            }

            statusLabel.setText("Creating reservation...");

            // Build Reservation object based on your entities.Reservation constructor/fields.
            // Adapt this part to match YOUR Reservation class.
            LocalTime time = LocalTime.parse(hhmm);
            LocalDateTime start = LocalDateTime.of(date, time);

            Reservation r = new Reservation(
                    subscriber.getSubscriberNumber(), // or subscriberId depending on your model
                    start,
                    guests
            );

            // Optional: attach notes if your Reservation supports it
            // r.setNotes(notesArea.getText().trim());

            controller.sendToServer(new Message(Commands.CREATE_RESERVATION, r));

        } catch (Exception e) {
            statusLabel.setText("Failed: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.CREATE_RESERVATION.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Error: " + m.getError());
                return;
            }

            // Server usually returns the created reservation or confirmation code
            statusLabel.setText("Reservation confirmed ✅");

            // Optional: go back to menu, or show a success screen
            try { ConnectApp.showCustomerMenu(); } catch (Exception ignored) {}
        });
    }
}
