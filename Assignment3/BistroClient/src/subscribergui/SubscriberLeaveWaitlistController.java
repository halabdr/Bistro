package subscribergui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for leaving the waitlist remotely (subscriber).
 */
public class SubscriberLeaveWaitlistController implements MessageListener {

    @FXML private TextField codeField;
    @FXML private Label statusLabel;

    private ClientController controller;
    private Subscriber subscriber;

    /**
     * Initializes the controller.
     *
     * @param controller connected client controller
     * @param subscriber logged-in subscriber
     */
    public void init(ClientController controller, Subscriber subscriber) {
        this.controller = controller;
        this.subscriber = subscriber;
        this.controller.setListener(this);
        
        if (statusLabel != null) {
            statusLabel.setText("");
        }
    }

    /**
     * Handles the Leave button click.
     */
    @FXML
    private void onLeave() {
        String code = codeField != null ? codeField.getText().trim() : "";
        
        if (code.isEmpty()) {
            setStatus("Please enter your waitlist entry code.", "-fx-text-fill: #E53E3E;");
            return;
        }

        try {
        	controller.setListener(this);
            setStatus("Processing...", "-fx-text-fill: #718096;");
            controller.leaveWaitlist(code);
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "-fx-text-fill: #E53E3E;");
        }
    }

    @FXML
    private void onBack() throws Exception {
        if (subscriber != null) {
            ConnectApp.showSubscriberMenu(subscriber);
        } else {
            ConnectApp.showCustomerMenu();
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.LEAVE_WAITLIST.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (m.isSuccess()) {
                setStatus("Successfully removed from waitlist!", "-fx-text-fill: #38A169;");
                if (codeField != null) {
                    codeField.clear();
                }
            } else {
                setStatus("Failed: " + m.getError(), "-fx-text-fill: #E53E3E;");
            }
        });
    }

    private void setStatus(String text, String style) {
        if (statusLabel != null) {
            statusLabel.setText(text);
            statusLabel.setStyle(style + " -fx-font-size: 13px;");
        }
    }
}