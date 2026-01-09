package subscribergui;

import client.ClientController;
import client.commands;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LostCodeController {

    @FXML private TextField identifierField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setMessageListener(this::onMessage);
        statusLabel.setText("");
        resultLabel.setText("");
    }

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
            controller.requestLostCode(id);
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void onMessage(Message m) {
        if (m == null || !commands.LOST_CODE.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Failed: " + m.getError());
                return;
            }
            statusLabel.setText("Done.");
            resultLabel.setText("Your code: " + String.valueOf(m.getData()));
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showCustomerMenu();
    }
}
