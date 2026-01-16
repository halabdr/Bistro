package welcomegui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class WelcomeController {

    @FXML private Button subscriberLoginBtn;
    @FXML private Button walkInBtn;
    @FXML private Button terminalBtn;
    @FXML private Button staffLoginBtn;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
    }

    @FXML
    private void handleSubscriberLogin() {
        try {
            ConnectApp.showSubscriberLogin();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void handleWalkInCustomer() {
        try {
            ConnectApp.showReservationSearch();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void handleTerminalCheckIn() {
        try {
            ConnectApp.showTerminalMenu();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }


    @FXML
    private void handleStaffLogin() {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Staff Login - will be implemented soon!");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}