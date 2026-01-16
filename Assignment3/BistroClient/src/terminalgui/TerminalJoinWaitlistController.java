package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.WaitlistEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

public class TerminalJoinWaitlistController implements MessageListener {

    @FXML private Spinner<Integer> dinersSpinner;
    @FXML private TextField subscriberField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        dinersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
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
    private void onJoin() {
        try {
            int diners = dinersSpinner.getValue();
            String sub = subscriberField.getText() == null ? null : subscriberField.getText().trim();
            if (sub != null && sub.isEmpty()) sub = null;

            resultLabel.setText("");
            setStatus("Joining waitlist...", "status-bad");

            controller.joinWaitlist(diners, sub);
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "status-error");
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.JOIN_WAITLIST.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                setStatus("Failed: " + m.getError(), "status-error");
                return;
            }

            setStatus("Done.", "status-ok");
            Object data = m.getData();

            if (data instanceof WaitlistEntry w) {
                resultLabel.setText("Your waitlist code: " + w.getEntryCode());
            } else {
                resultLabel.setText(String.valueOf(data));
            }
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showTerminalMenu();
    }
}
