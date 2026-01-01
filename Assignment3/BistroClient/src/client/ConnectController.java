package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ConnectController implements MessageListener {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Button connectBtn;
    @FXML private Button disconnectBtn;
    @FXML private Label statusLabel;

    private ClientController clientController;

    @FXML
    public void initialize() {
        disconnectBtn.setDisable(true);
    }

    @FXML
    public void onConnect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());

            clientController = new ClientController(host, port);
            clientController.setListener(msg -> statusLabel.setText("Connected - server responded"));

            clientController.connect();

            statusLabel.setText("Connected to " + host + ":" + port);
            connectBtn.setDisable(true);
            disconnectBtn.setDisable(false);

        } catch (Exception e) {
            statusLabel.setText("Connection failed");
        }
    }

    @FXML
    public void onDisconnect() {
        try {
            clientController.disconnect();
        } catch (Exception ignored) {}

        statusLabel.setText("Disconnected");
        connectBtn.setDisable(false);
        disconnectBtn.setDisable(true);
    }
    
    @Override
    public void onMessage(Object msg) {
        Platform.runLater(() -> {
            statusLabel.setText("Server: " + msg);
        });
    }
}