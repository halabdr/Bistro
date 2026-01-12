package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Terminal screen controller.
 * Allows a terminal worker to "seat" a reservation by entering a confirmation code.
 * Sends command SEAT_BY_CODE request and displays the returned result.
 */
public class SeatByCodeController implements MessageListener {

    /** Input for reservation confirmation code. */
    @FXML private TextField codeField;

    /** Status label for showing progress/errors. */
    @FXML private Label statusLabel;

    /** Result label for showing returned data (table number / message). */
    @FXML private Label resultLabel;

    /** Shared client controller. */
    private ClientController controller;

    /**
     * Initializes this screen with the shared ClientController
     * and registers this controller as the active message listener.
     *
     * @param controller shared client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        statusLabel.setText("");
        resultLabel.setText("");
    }

    /**
     * Handles the "Seat" action.
     * Validates the confirmation code and sends request to server.
     */
    @FXML
    private void onSeat() {
        try {
            String code = codeField.getText() == null ? "" : codeField.getText().trim();
            if (code.isEmpty()) {
                statusLabel.setText("Please enter confirmation code.");
                return;
            }

            resultLabel.setText("");
            statusLabel.setText("Seating...");

            controller.seatByCode(code);

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Receives server messages.
     * Only handles responses for command SEAT_BY_CODE
     *
     * @param m message received from the server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.SEAT_BY_CODE.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Failed: " + m.getError());
                return;
            }

            statusLabel.setText("Done.");

            Object data = m.getData();

            // If server returns Reservation, show table number
            if (data instanceof Reservation r) {
            	int table = r.getTableNumber();
            	if (table > 0) {
            	    resultLabel.setText("Table number: " + table);
            	} else {
            	    resultLabel.setText("Reservation found, but no table assigned yet.");
            	}
            	return;
            }

            // Otherwise show any result
            resultLabel.setText("Result: " + String.valueOf(data));
        });
    }

    /**
     * Navigates back to the home screen.
     *
     * @throws Exception if navigation fails
     */
    @FXML
    private void onBack() throws Exception {
        ConnectApp.showHome();
    }
}
