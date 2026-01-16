package clientgui;

import client.MessageListener;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ConnectController implements MessageListener {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;
    @FXML private Button connectBtn;

    @FXML private StackPane errorOverlay;
    @FXML private Label errorMessageLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("");

        hostField.setText("");
        portField.setText("5555");

        hostField.setOnAction(e -> onConnect());
        portField.setOnAction(e -> onConnect());
    }

    @FXML
    public void onConnect() {
        String host = hostField.getText() == null ? "" : hostField.getText().trim();

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            statusLabel.setText("Invalid port. Please enter a number.");
            return;
        }

        if (host.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            statusLabel.setText("Please enter a host.");
            return;
        }

        if (port <= 0) {
            statusLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            statusLabel.setText("Please enter a valid port.");
            return;
        }

        Stage stage = (Stage) hostField.getScene().getWindow();

        // lock UI
        if (connectBtn != null) connectBtn.setDisable(true);
        hostField.setDisable(true);
        portField.setDisable(true);

        statusLabel.setStyle("-fx-text-fill: #93c5fd; -fx-font-size: 12px;");
        statusLabel.setText("Connecting...");

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                ConnectApp.init(stage, host, port);
                return null;
            }
        };

        task.setOnSucceeded(ev -> {
            statusLabel.setStyle("-fx-text-fill: #86efac; -fx-font-size: 12px;");
            statusLabel.setText("Connected to " + host + ":" + port);

            try {
                ConnectApp.showWelcome();
            } catch (Exception ex) {
                showFancyError("Navigation error", ex.getMessage());
                unlock();
            }
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            String msg = (ex != null && ex.getMessage() != null)
                    ? ex.getMessage()
                    : "Unable to reach the server.";

            String pretty =
                    "We couldn’t connect to the Bistro server.\n" +
                    "Please verify the IP and make sure the server is running.\n\n" +
                    "Details: " + msg;

            showFancyError("Connection Failed", pretty);
            unlock();
        });

        Thread t = new Thread(task, "ocsf-connect-thread");
        t.setDaemon(true);
        t.start();
    }

    private void unlock() {
        if (connectBtn != null) connectBtn.setDisable(false);
        hostField.setDisable(false);
        portField.setDisable(false);
    }

    private void showFancyError(String title, String message) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(message);
        }
        if (errorOverlay != null) {
            errorOverlay.setManaged(true);
            errorOverlay.setVisible(true);
        }
        statusLabel.setText("");
    }

    @FXML
    private void onCloseError() {
        if (errorOverlay != null) {
            errorOverlay.setVisible(false);
            errorOverlay.setManaged(false);
        }
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    @Override
    public void onMessage(Message message) {
        Platform.runLater(() -> {
            if (message == null) return;

            if (!message.isSuccess()) {
                statusLabel.setText(message.getCommand() + ": " + message.getError());
                return;
            }

            statusLabel.setText("OK: " + message.getCommand());
        });
    }
}