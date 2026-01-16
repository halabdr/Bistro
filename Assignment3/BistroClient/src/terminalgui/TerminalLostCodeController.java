package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TerminalLostCodeController implements MessageListener {

    @FXML private TextField identifierField;
    @FXML private ChoiceBox<String> typeChoice;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        resultLabel.setText("");
        setStatus("", null);

        typeChoice.getItems().setAll("Reservation", "Waitlist");
        typeChoice.setValue("Reservation");
    }

    private void setStatus(String text, String styleClass) {
        statusLabel.setText(text == null ? "" : text);
        statusLabel.getStyleClass().removeAll("status-ok","status-bad","status-error");
        if (styleClass != null && !styleClass.isBlank()) {
            statusLabel.getStyleClass().add(styleClass);
        }
    }

    @FXML
    private void onSend() {
        try {
            String identifier = identifierField.getText() == null ? "" : identifierField.getText().trim();
            if (identifier.isEmpty()) {
                setStatus("Please enter phone or email.", "status-bad");
                return;
            }

            resultLabel.setText("");
            setStatus("Searching...", "status-bad");

            if ("Waitlist".equals(typeChoice.getValue())) {
                controller.lostCodeWaitlist(identifier);
            } else {
                controller.lostCode(identifier);
            }
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "status-error");
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        String cmd = m.getCommand();
        if (!(Commands.LOST_CODE.equals(cmd) || Commands.LOST_CODE_WAITLIST.equals(cmd))) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                setStatus("Failed: " + m.getError(), "status-error");
                return;
            }

            setStatus("Done.", "status-ok");
            resultLabel.setText("Code: " + String.valueOf(m.getData()));
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showWelcome();
    }
}
