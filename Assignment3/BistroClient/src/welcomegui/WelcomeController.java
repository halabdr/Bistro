package welcomegui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the Welcome/Landing screen.
 * This is the first screen users see when launching the Bistro application.
 */
public class WelcomeController {

    @FXML private Button subscriberLoginBtn;
    @FXML private Button walkInBtn;
    @FXML private Button terminalBtn;
    @FXML private Button staffLoginBtn;

    private Stage primaryStage;

    /**
     * Sets the primary stage for scene navigation.
     * 
     * @param stage the primary stage
     */
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Handles Subscriber Login button click.
     */
    @FXML
    private void handleSubscriberLogin() {
        showComingSoon("Subscriber Login");
        //  Will be implemented in next screen
    }

    /**
     * Handles Walk-in Customer button click.
     */
    @FXML
    private void handleWalkInCustomer() {
        showComingSoon("Walk-in Customer Reservation");
        // Will be implemented in next screen
    }

    /**
     * Handles Terminal Check-in button click.
     */
    @FXML
    private void handleTerminalCheckIn() {
        showComingSoon("Terminal Check-in");
        //  Will be implemented in next screen
    }

    /**
     * Handles Staff Login button click.
     */
    @FXML
    private void handleStaffLogin() {
        showComingSoon("Staff Login");
        //  Will be implemented in next screen
    }

    /**
     * Shows a "coming soon" alert (temporary for testing).
     */
    private void showComingSoon(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coming Soon");
        alert.setHeaderText(null);
        alert.setContentText(feature + " - will be implemented soon!");
        alert.showAndWait();
    }
    
    /**
     * Handles Exit button click.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }
}