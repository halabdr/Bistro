package reservationgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controller for creating a reservation.
 * Sends CREATE_RESERVATION and shows the returned confirmation code / reservation details.
 */
public class CreateReservationController implements MessageListener {

    @FXML private Label summaryLabel;
    @FXML private TextField subscriberNumberField;
    @FXML private Label statusLabel;

    private ClientController controller;
    private LocalDate date;
    private String hhmm;
    private int guests;

    /**
     * Initializes this screen with the selected slot data.
     *
     * @param controller connected client controller
     * @param date booking date
     * @param hhmm booking time in HH:mm format
     * @param guests number of diners
     */
    public void init(ClientController controller, LocalDate date, String hhmm, int guests) {
        this.controller = controller;
        this.controller.setListener(this);

        this.date = date;
        this.hhmm = hhmm;
        this.guests = guests;

        summaryLabel.setText("Date: " + date + " | Time: " + hhmm + " | Guests: " + guests);
        statusLabel.setText("");
    }

    /**
     * Navigates back to the reservation search screen.
     */
    @FXML
    private void onBack() throws Exception {
        ConnectApp.showReservationSearch();
    }

    /**
     * Sends CREATE_RESERVATION request to the server.
     */
    @FXML
    private void onConfirm() {
        try {
            String subNum = subscriberNumberField.getText() == null ? "" : subscriberNumberField.getText().trim();
            if (subNum.isEmpty()) {
                statusLabel.setText("Please enter subscriber number.");
                return;
            }

            LocalTime time = LocalTime.parse(hhmm); // "HH:mm"
            statusLabel.setText("Sending request...");

            controller.createReservation(date, time, guests, subNum);

        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
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

            statusLabel.setText("Reservation confirmed.");
            statusLabel.setText("Reservation confirmed: " + String.valueOf(m.getData()));
        });
    }
}