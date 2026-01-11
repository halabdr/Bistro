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

/**
 * This controller is responsible for:
 *   Collecting the server host and port from the user
 *   Establishing a connection to the server using OCSF
 *   Updating the user interface according to the connection state
 *   Navigating to the home screen after a successful connection
 * The connection is created using ConnectApp#init(Stage, String, int)
 * to ensure that the entire application shares a single client-server connection.
 */
public class ConnectController implements MessageListener {

    /** Text field for entering the server host address. */
    @FXML private TextField hostField;

    /** Text field for entering the server port number. */
    @FXML private TextField portField;

    /** Label used to display connection and error status messages. */
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("");

        hostField.setText("127.0.0.1");
        portField.setText("5555");

        hostField.setOnAction(e -> onConnect());
        portField.setOnAction(e -> onConnect());
    }


    /**
     * Handles the "Connect" button click.
     * Reads the host and port entered by the user, validates the input.
     */
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

            ConnectApp.showHome();

        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            statusLabel.setText("Invalid port. Please enter a number.");

        } catch (Exception e) {
        	//Connection failed
            statusLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px;");
            statusLabel.setText("Connection failed: " + e.getMessage());
        }
    }

    
    @FXML
    private void onExit() {
        javafx.application.Platform.exit();
    }


    /**
     * Receives messages from the server.
     * 
     * This method is called by the client communication layer and
     * updates the UI according to the received message.
     *
     * @param message the message received from the server
     */
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