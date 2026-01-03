package client;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ConnectController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Button connectBtn;
    @FXML private Button disconnectBtn;
    @FXML private Label statusLabel;

    private ClientController clientController;

    @FXML
    public void initialize() {
        disconnectBtn.setDisable(true);
        statusLabel.setText("");
    }

    @FXML
    public void onConnect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());

            clientController = new ClientController(host, port);

            clientController.setListener(msg -> statusLabel.setText("Server: " + msg));

            clientController.connect();

            statusLabel.setText("Connected to " + host + ":" + port);
            connectBtn.setDisable(true);
            disconnectBtn.setDisable(false);

            ConnectApp.showAvailableSlots(clientController);

        } catch (Exception e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
        }
    }

    @FXML
    public void onDisconnect() {
        try {
            if (clientController != null) clientController.disconnect();
        } catch (Exception ignored) {}

        statusLabel.setText("Disconnected");
        connectBtn.setDisable(false);
        disconnectBtn.setDisable(true);
    }
}