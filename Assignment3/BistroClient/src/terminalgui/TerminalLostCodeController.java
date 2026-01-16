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

public class TerminalLostCodeController implements MessageListener {

    @FXML private TextField identifierField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    /**
     * Controls where the Back button returns.
     * MENU = TerminalMenu, SEAT_BY_CODE = SeatByCode screen.
     */
    private BackTarget backTarget = BackTarget.MENU;

    public enum BackTarget {
        MENU,
        SEAT_BY_CODE
    }

    // state: first try reservation, if not found -> try waitlist
    private boolean waitingForWaitlist = false;

    public void init(ClientController controller) {
        init(controller, BackTarget.MENU);
    }

    public void init(ClientController controller, BackTarget backTarget) {
        this.controller = controller;
        this.controller.setListener(this);

        this.backTarget = (backTarget == null) ? BackTarget.MENU : backTarget;

        resultLabel.setText("");
        setStatus("", null);
        waitingForWaitlist = false;

        // Live validation while typing
        identifierField.textProperty().addListener((obs, oldV, newV) -> {
            String v = newV == null ? "" : newV.trim();

            if (v.isEmpty()) {
                setStatus("", null);
                return;
            }

            if (isEmail(v)) {
                setStatus("Email detected.", "status-ok");
            } else if (isPhone(v)) {
                setStatus("Phone detected.", "status-ok");
            } else {
                setStatus("Enter a valid email or phone.", "status-bad");
            }
        });
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
            String identifier = identifierField.getText() == null
                    ? ""
                    : identifierField.getText().trim();

            if (identifier.isEmpty()) {
                setStatus("Please enter phone or email.", "status-bad");
                return;
            }

            boolean email = isEmail(identifier);
            boolean phone = isPhone(identifier);

            if (!email && !phone) {
                setStatus(
                    "Invalid input. Enter a valid email or phone number.",
                    "status-error"
                );
                return;
            }

            if (controller == null || !controller.isConnected()) {
                setStatus("Not connected to server.", "status-error");
                return;
            }

            // normalize phone before sending
            if (phone) {
                identifier = normalizePhone(identifier);
                identifierField.setText(identifier);
                setStatus("Phone detected. Searching...", "status-bad");
            } else {
                setStatus("Email detected. Searching...", "status-bad");
            }

            resultLabel.setText("");
            waitingForWaitlist = false;

            // 1) try reservation first
            controller.lostCode(identifier);

        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "status-error");
        }
    }


    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        String cmd = m.getCommand();
        // both reservation & waitlist repos return command "LOST_CODE" in your server code
        if (!Commands.LOST_CODE.equals(cmd)) return;

        Platform.runLater(() -> {
            if (m.isSuccess()) {
                setStatus("Done.", "status-ok");
                resultLabel.setText("Code: " + String.valueOf(m.getData()));
                waitingForWaitlist = false;
                return;
            }

            // reservation not found -> try waitlist once
            if (!waitingForWaitlist) {
                waitingForWaitlist = true;
                try {
                    controller.lostCodeWaitlist(identifierField.getText().trim());
                } catch (Exception e) {
                    setStatus("Error: " + e.getMessage(), "status-error");
                }
                return;
            }

            // both failed
            setStatus("Not found for this identifier.", "status-error");
            waitingForWaitlist = false;
        });
    }

    @FXML
    private void onBack() throws Exception {
        if (backTarget == BackTarget.SEAT_BY_CODE) {
            ConnectApp.showTerminalSeatByCode();
        } else {
            ConnectApp.showTerminalMenu();
        }
    }

    private boolean isEmail(String s) {
        return s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isPhone(String s) {
        String cleaned = s.replaceAll("[\\s\\-()]", "");
        if (cleaned.startsWith("+")) cleaned = cleaned.substring(1);

        if (cleaned.startsWith("972")) {
            String rest = cleaned.substring(3);
            return rest.matches("^\\d{8,9}$");
        }

        return cleaned.matches("^05\\d{8}$")
                || (cleaned.startsWith("972") && cleaned.substring(3).matches("^5\\d{8}$"));
    }

    private String normalizePhone(String s) {
        String trimmed = s.trim();
        boolean plus = trimmed.startsWith("+");
        String digits = trimmed.replaceAll("\\D", "");
        return plus ? ("+" + digits) : digits;
    }
}
