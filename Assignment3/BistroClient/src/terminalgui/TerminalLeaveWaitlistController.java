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

public class TerminalLeaveWaitlistController implements MessageListener {

    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        resultLabel.setText("");
        setStatus("", null);
    }

    private void setStatus(String text, String styleClass) {
        statusLabel.setText(text == null ? "" : text);
        statusLabel.getStyleClass().removeAll("status-ok","status-bad","status-error");
        if (styleClass != null && !styleClass.isBlank()) {
            statusLabel.getStyleClass().add(styleClass);
        }
    }

    @FXML
    private void onLeave() {
        try {
            String code = codeField.getText() == null ? "" : codeField.getText().trim();
            if (code.isEmpty()) {
                setStatus("Please enter waitlist code.", "status-bad");
                return;
            }

            resultLabel.setText("");
            setStatus("Leaving waitlist...", "status-bad");

            controller.leaveWaitlist(code);
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "status-error");
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.LEAVE_WAITLIST.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                setStatus("Failed: " + m.getError(), "status-error");
                return;
            }

            setStatus("Done.", "status-ok");
            resultLabel.setText(String.valueOf(m.getData()));
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showTerminalMenu();
    }
}
