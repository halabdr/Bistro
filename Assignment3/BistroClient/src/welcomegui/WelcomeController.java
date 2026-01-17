package welcomegui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * Controller for the Welcome screen.
 * Provides navigation to different parts of the application.
 */
public class WelcomeController {

    @FXML private Button subscriberLoginBtn;
    @FXML private Button walkInBtn;
    @FXML private Button terminalBtn;
    @FXML private Button staffLoginBtn;

    private ClientController controller;

    /**
     * Initializes the controller with the client controller.
     *
     * @param controller the client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
    }

    /**
     * Handles navigation to Subscriber Login screen.
     */
    @FXML
    private void handleSubscriberLogin() {
        try {
            ConnectApp.showSubscriberLogin();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Handles navigation to Walk-In reservation screen.
     */
    @FXML
    private void handleWalkInCustomer() {
        try {
            ConnectApp.showReservationSearch();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Handles navigation to Terminal Menu screen.
     */
    @FXML
    private void handleTerminalCheckIn() {
        try {
            ConnectApp.showTerminalMenu();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.toString());
        }
    }

    /**
     * Handles navigation to Staff Login screen.
     */
    @FXML
    private void handleStaffLogin() {
        try {
            ConnectApp.showStaffLogin();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Handles application exit.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    /**
     * Shows an alert dialog.
     *
     * @param type alert type
     * @param title dialog title
     * @param message dialog message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}