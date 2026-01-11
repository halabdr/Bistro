package subscribergui;

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

/**
 * Controller for cancelling an existing reservation using its confirmation code.
 */
public class CancelReservationController implements MessageListener {

    @FXML private TextField codeField;
    @FXML private Label resultLabel;

    private ClientController controller;

    /**
     * Initializes the screen with a connected {@link ClientController}.
     *
     * @param controller connected client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        resultLabel.setText("");
    }

    /**
     * Sends CANCEL_RESERVATION request to the server.
     */
    @FXML
    public void onCancel() {
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            resultLabel.setText("Enter confirmation code");
            return;
        }

        try {
            resultLabel.setText("Sending...");
            controller.cancelReservation(code);
        } catch (IOException e) {
            resultLabel.setText("Failed: " + e.getMessage());
        }
    }

    /**
     * Navigates back to the customer menu.
     */
    @FXML
    public void onBack() {
        try {
            ConnectApp.showCustomerMenu();
        } catch (Exception e) {
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.CANCEL_RESERVATION.equals(m.getCommand())) return;

        Platform.runLater(() ->
                resultLabel.setText(m.isSuccess()
                        ? String.valueOf(m.getData())
                        : ("Error: " + m.getError()))
        );
    }
}