package walkingui;

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
 * Controller for walk-in customers to cancel their reservation.
 * Uses confirmation code for identification.
 */
public class WalkInCancelReservationController implements MessageListener {

    @FXML private TextField codeField;
    @FXML private Label statusLabel;

    private ClientController controller;

    /**
     * Initializes the controller.
     *
     * @param controller connected client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        statusLabel.setText("");
    }

    /**
     * Handles the cancel button click.
     */
    @FXML
    private void onCancel() {
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            setStatus("Please enter your confirmation code.", StatusType.ERROR);
            return;
        }

        try {
            setStatus("Processing cancellation...", StatusType.INFO);
            controller.cancelReservation(code);
        } catch (IOException e) {
            setStatus("Failed: " + e.getMessage(), StatusType.ERROR);
        }
    }

    /**
     * Navigates back to the walk-in menu.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showWalkInMenu();
        } catch (Exception e) {
            setStatus("Navigation error: " + e.getMessage(), StatusType.ERROR);
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.CANCEL_RESERVATION.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (m.isSuccess()) {
                setStatus("Reservation cancelled successfully!", StatusType.SUCCESS);
                codeField.clear();
            } else {
                setStatus("Failed: " + m.getError(), StatusType.ERROR);
            }
        });
    }

    private enum StatusType { INFO, SUCCESS, ERROR }

    private void setStatus(String message, StatusType type) {
        statusLabel.setText(message);
        switch (type) {
            case SUCCESS:
                statusLabel.setStyle("-fx-text-fill: #38A169; -fx-font-size: 14px; -fx-font-weight: 600;");
                break;
            case ERROR:
                statusLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 14px; -fx-font-weight: 600;");
                break;
            case INFO:
            default:
                statusLabel.setStyle("-fx-text-fill: #3182CE; -fx-font-size: 14px;");
                break;
        }
    }
}