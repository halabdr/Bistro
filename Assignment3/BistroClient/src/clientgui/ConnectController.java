package clientgui;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.*;


/**
 * JavaFX controller for the connection screen.
 *
 * 
 * This controller is responsible for:
 * 
 *   Collecting connection parameters (host and port)
 *   Establishing and closing a connection to the server
 *   Updating the user interface based on the connection state
 *   Navigating to the next screen after a successful connection
 * 
 */

public class ConnectController {
	 /** Text field for entering the server host address */
    @FXML private TextField hostField;
    
    /** Text field for entering the server port number */
    @FXML private TextField portField;
    
    /** Button used to initiate the connection to the server */
    @FXML private Button connectBtn;
    
    /** Button used to disconnect from the server */
    @FXML private Button disconnectBtn;
    
    /** Label used to display connection status messages */
    @FXML private Label statusLabel;

    
    /** Controller responsible for managing client-server communication */
    private ClientController clientController;
    
    /**
     * Initializes the controller after the FXML elements are loaded.
     * Sets the initial UI state.
     */
    @FXML
    public void initialize() {
    	// Disable the disconnect button until a connection is established
        disconnectBtn.setDisable(true);
        // Clear any previous status messages
        statusLabel.setText("");
    }
    
    /**
     * Handles the "Connect" button click.
     *
     * Reads the host and port values entered by the user,
     * creates a {@link ClientController}, establishes a connection
     * to the server, and updates the UI accordingly.
     * 
     */

    @FXML
    public void onConnect() {
        try {
        	// Read and validate connection parameters
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            
            // Create a new client controller for server communication
            clientController = new ClientController(host, port);
            
            
            // Register a listener to display server messages in the status label
            clientController.setListener(msg ->statusLabel.setText("Server: " + String.valueOf(msg)));

            // Establish the connection to the server
            clientController.connect();
            
            // Update UI to reflect successful connection
            statusLabel.setText("Connected to " + host + ":" + port);
            connectBtn.setDisable(true);
            disconnectBtn.setDisable(false);
            
            // Navigate to the available slots screen
            ConnectApp.showHome();

        } catch (Exception e) {
        	// Handle invalid input or connection failur
            statusLabel.setText("Connection failed: " + e.getMessage());
        }
    }
    
    /**
     * Handles the "Disconnect" button click.
     *
     * 
     * Closes the connection to the server (if exists)
     * and updates the UI state.
     * 
     */

    @FXML
    public void onDisconnect() {
        try {
        	// Disconnect from the server if a connection exists
            if (clientController != null) clientController.disconnect();
        } 
        catch (Exception ignored) {
        	 // Ignore exceptions during disconnection
        }
        
        // Update UI to reflect disconnection
        statusLabel.setText("Disconnected");
        connectBtn.setDisable(false);
        disconnectBtn.setDisable(true);
    }
}