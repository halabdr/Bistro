package reservationgui;

import client.ClientController;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateReservationController implements MessageListener {

    @FXML private Label summaryLabel;
    @FXML private RadioButton guestRadio;
    @FXML private RadioButton subscriberRadio;
    @FXML private TextField subscriberNumberField;
    @FXML private Label statusLabel;

    private ClientController controller;
    private LocalDate date;
    private String hhmm;
    private int guests;

    public void init(ClientController controller, LocalDate date, String hhmm, int guests) {
        this.controller = controller;
        this.controller.setListener(this);

        this.date = date;
        this.hhmm = hhmm;
        this.guests = guests;

        summaryLabel.setText("Date: " + date + " | Time: " + hhmm + " | Guests: " + guests);

        // Current server-side implementation expects subscriberNumber (String)
        subscriberRadio.setSelected(true);
        guestRadio.setSelected(false);

        subscriberNumberField.setDisable(false);
        statusLabel.setText("");

        // If you want: disable subscriber field when guest selected (but guest not supported in server)
        guestRadio.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                subscriberNumberField.setDisable(true);
                statusLabel.setText("Guest flow is not supported in current server implementation.");
            }
        });

        subscriberRadio.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                subscriberNumberField.setDisable(false);
                statusLabel.setText("");
            }
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showReservationSearch();
    }

    @FXML
    private void onConfirm() {
        try {
            if (guestRadio.isSelected()) {
                statusLabel.setText("Guest reservations are not supported yet.");
                return;
            }

            String subNum = subscriberNumberField.getText() == null ? "" : subscriberNumberField.getText().trim();
            if (subNum.isEmpty()) {
                statusLabel.setText("Please enter subscriber number.");
                return;
            }

            LocalTime time = LocalTime.parse(hhmm); // hhmm format "HH:mm"
            statusLabel.setText("Sending request...");

            controller.createReservation(date, time, guests, subNum);

        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Object msg) {
        if (!(msg instanceof Message m)) return;
        if (!"CREATE_RESERVATION".equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Error: " + m.getError());
                return;
            }

            // Server likely returns confirmation code in data
            Object data = m.getData();
            statusLabel.setText("Reservation confirmed. Code: " + String.valueOf(data));
        });
    }
}
