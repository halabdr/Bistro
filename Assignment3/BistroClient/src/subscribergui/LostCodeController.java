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
 * Controller for retrieving a reservation/confirmation code using an identifier
 * such as an email address or phone number.
 */
public class LostCodeController implements MessageListener {

    @FXML private TextField identifierField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    /**
     * Initializes this screen with the shared ClientController instance
     * and registers this controller as the active MessageListener.
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
     * Handles the Find button click.
     * Sends a command LOST_CODE request to the server.
     */
    @FXML
    private void onFind() {
        try {
            String id = identifierField.getText() == null ? "" : identifierField.getText().trim();
            if (id.isEmpty()) {
                statusLabel.setText("Please enter phone or email.");
                return;
            }

            resultLabel.setText("");
            statusLabel.setText("Searching...");
            controller.setListener(this);
            controller.lostCode(id);

        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Receives server responses for command LOST_CODE and updates the UI.
     *
     * @param m message received from the server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.LOST_CODE.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Failed: " + m.getError());
                return;
            }
            statusLabel.setText("Done.");
            resultLabel.setText(String.valueOf(m.getData()));
        });
    }

    /**
     * Navigates back to the customer menu.
     *
     * @throws Exception if navigation fails
     */
    @FXML
    private void onBack() throws Exception {
        ConnectApp.showCustomerMenu();
    }
}