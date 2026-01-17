package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for cancelling a reservation at the terminal.
 */
public class TerminalCancelReservationController implements MessageListener {

    @FXML private TextField codeField;
    @FXML private Label statusLabel;

    private ClientController controller;

    /**
     * Initializes the controller with a connected ClientController.
     *
     * @param controller connected client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        
        if (statusLabel != null) {
            statusLabel.setText("");
        }
    }

    /**
     * Handles the Cancel button click.
     */
    @FXML
    private void onCancel() {
        String code = codeField != null ? codeField.getText().trim() : "";
        
        if (code.isEmpty()) {
            setStatus("Please enter your confirmation code.", "status-error");
            return;
        }

        try {
            setStatus("Cancelling reservation...", "status-info");
            controller.cancelReservation(code);
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "status-error");
        }
    }

    /**
     * Navigates back to the terminal menu.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showTerminalMenu();
        } catch (Exception e) {
            setStatus("Navigation error: " + e.getMessage(), "status-error");
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.CANCEL_RESERVATION.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (m.isSuccess()) {
                setStatus("Reservation cancelled successfully!", "status-success");
                if (codeField != null) {
                    codeField.clear();
                }
            } else {
                setStatus("Failed: " + m.getError(), "status-error");
            }
        });
    }

    private void setStatus(String text, String styleClass) {
        if (statusLabel != null) {
            statusLabel.setText(text);
            statusLabel.getStyleClass().removeAll("status-error", "status-success", "status-info");
            statusLabel.getStyleClass().add(styleClass);
        }
    }
}