package terminalgui;

import client.ClientController;
import client.commands;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SeatByCodeController {

    @FXML private TextField codeField;
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

    private void onMessage(Message m) {
        if (m == null || !commands.SEAT_BY_CODE.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Failed: " + m.getError());
                return;
            }

            statusLabel.setText("Done.");

            Object data = m.getData();

            // If server returns Reservation, show tableNumber from entity
            if (data instanceof Reservation r) {
                resultLabel.setText("Table number: " + r.getTableNumber());
                return;
            }

            // If server returns just a number or string
            resultLabel.setText("Result: " + String.valueOf(data));
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showHome();
    }
}
