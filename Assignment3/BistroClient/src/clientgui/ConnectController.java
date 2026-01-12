package clientgui;

import client.MessageListener;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectController implements MessageListener {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

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
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());

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

            ConnectApp.init(stage, host, port);

            statusLabel.setStyle("-fx-text-fill: #86efac; -fx-font-size: 12px;");
            statusLabel.setText("Connected to " + host + ":" + port);

            ConnectApp.showWelcome();

        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            statusLabel.setText("Invalid port. Please enter a number.");

        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            statusLabel.setText("Connection failed: " + e.getMessage());
        }
    }

    @FXML
    private void onExit() {
        javafx.application.Platform.exit();
    }

    @Override
    public void onMessage(Message message) {
        Platform.runLater(() -> {
            if (message == null) return;

            if (!message.isSuccess()) {
                statusLabel.setText(message.getCommand() + ": " + message.getError());

                if ("DISCONNECTED".equals(message.getCommand())) {
                }
                return;
            }

            statusLabel.setText("OK: " + message.getCommand());
        });
    }
}