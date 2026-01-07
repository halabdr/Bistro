package slotsgui;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import client.ClientController;
import client.MessageListener;
import clientgui.ConnectApp;
import common.AvailableSlot;
import common.CommandType;
import common.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AvailableSlotsController implements MessageListener {
	/** Date picker for selecting the reservation date */
    @FXML private DatePicker datePicker;
    
    /** Spinner for selecting the number of diners */
    @FXML private Spinner<Integer> dinersSpinner;
    
    /** Button used to send the availability request to the server */
    @FXML private Button checkBtn;
    
    /** Button used to navigate back to the connection screen */
    @FXML private Button backBtn;
    
    /** Label used to display status and result messages */
    @FXML private Label resultLabel;
    
    /** List view used to display available reservation slots */
    @FXML private ListView<String> slotsList;
    
    
    /** Reference to the client controller handling communication with the server */
    private ClientController clientController;
    
    /**
     * Injects the {@link ClientController} into this controller and
     * initializes UI components.
     *
     * @param controller the client controller responsible for server communication
     */

    public void setClientController(ClientController controller) {
        this.clientController = controller;
        
        // Register this controller as a listener for server messages
        this.clientController.setListener(this);
        
        // Initialize default UI values
        if (datePicker != null) datePicker.setValue(LocalDate.now());
        if (dinersSpinner != null) {
            dinersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        
            
        // Enable the check button when the screen is ready
        if (checkBtn != null) checkBtn.setDisable(false);
        }
    }
    
    /**
     * Initializes the controller after the FXML elements are loaded.
     */
    @FXML
    public void initialize() {
        resultLabel.setText("");
    }
    
    /**
     * Handles the "Check Availability" button click.
     *
     * Validates the connection status, collects user input,
     * sends a request to the server, and updates the UI state.
     */

    @FXML
    public void onCheckAvailability() {
    	
    	// Ensure the client is connected before sending a request
    	if (clientController == null || !clientController.isConnected()) {
    	    resultLabel.setText("Not connected to server.");
    	    return;
    	}

        try {
        	// Read user input
            LocalDate date = datePicker.getValue();
            int diners = dinersSpinner.getValue();
            
         // Update UI state before sending the request
            resultLabel.setText("Request sent...");
            slotsList.getItems().clear();
            
         // Send request to the server
            clientController.requestAvailableSlots(date, diners);

        } catch (IOException e) {
        	// Network-related error
            resultLabel.setText("Failed to send request: " + e.getMessage());
        } catch (Exception e) {
        	// Invalid input or unexpected error
            resultLabel.setText("Invalid input: " + e.getMessage());
        }
    }
    
    /**
     * Handles the "Back" button click and navigates
     * to the connection screen.
     */
    @FXML
    public void onBack() {
        try {
            ConnectApp.showConnect();
        } catch (Exception e) {
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }
    
    /**
     * Called when a message is received from the server.
     *
     * @param msg the message received from the server
     */

    @Override
    public void onMessage(Object msg) {
    	handleServerMessage(msg);
    }
    
    /**
     * Processes messages received from the server and updates the UI.
     *
     * @param msg the received message object
     */

    private void handleServerMessage(Object msg) {
    	// Handle server disconnection
        if (msg instanceof String s && s.startsWith("DISCONNECTED:")) {
            resultLabel.setText("Disconnected from server");
            slotsList.getItems().clear();
            checkBtn.setDisable(true);
            return;
        }
        
        // Ensure the message is of the expected type
        if (!(msg instanceof Message)) {
            resultLabel.setText("Unexpected response type");
            return;
        }

        Message response = (Message) msg;
        
        // Ignore messages that are not related to available slots
        if (response.getCommand() != CommandType.GET_AVAILABLE_SLOTS) {
            return;
        }
        
        // Handle server-side error
        if (!response.isSuccess()) {
            resultLabel.setText("Error: " + response.getError());
            return;
        }

        Object data = response.getData();
        
        // Validate returned data
        if (!(data instanceof List<?> list)) {
            resultLabel.setText("Invalid data returned");
            return;
        }
        
        // Display the available slots in the list view
        slotsList.setItems(FXCollections.observableArrayList(
                list.stream()
                    .map(o -> (o instanceof AvailableSlot slot)
                            ? slot.toString()
                            : String.valueOf(o))
                    .toList()
        ));
        
        
        // Update result label
        if (list.isEmpty()) {
            resultLabel.setText("No available slots found");
        } else {
            resultLabel.setText("Found " + list.size() + " slots");
        }

    }
}