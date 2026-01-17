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

/**
 * Controller for walk-in customers to leave the waitlist.
 * Uses entry code for identification.
 */
public class WalkInLeaveWaitlistController implements MessageListener {

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
     * Handles the leave button click.
     */
    @FXML
    private void onLeave() {
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            setStatus("Please enter your waitlist entry code.", StatusType.ERROR);
            return;
        }

        try {
            setStatus("Processing...", StatusType.INFO);
            controller.leaveWaitlist(code);
        } catch (Exception e) {
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
        if (!Commands.LEAVE_WAITLIST.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (m.isSuccess()) {
                setStatus("Successfully removed from waitlist!", StatusType.SUCCESS);
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