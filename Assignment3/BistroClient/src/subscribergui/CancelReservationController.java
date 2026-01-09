package customergui;

import client.ClientController;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class CancelReservationController implements MessageListener {

    @FXML private TextField codeField;
    @FXML private Label resultLabel;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        resultLabel.setText("");
    }

    @FXML
    public void onCancel() {
        String code = codeField.getText().trim();
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

    @FXML
    public void onBack() {
        try {
            ConnectApp.showCustomerMenu();
        } catch (Exception e) {
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Object msg) {
        Platform.runLater(() -> {
            if (!(msg instanceof Message m)) return;
            if (!"CANCEL_RESERVATION".equals(m.getCommand())) return;

            resultLabel.setText(m.isSuccess() ? String.valueOf(m.getData()) : ("Error: " + m.getError()));
        });
    }
}
