package walkingui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

/**
 * Controller for the Walk-In Customer Menu screen.
 * Provides navigation to guest services: reservations, cancellation, waitlist, and payments.
 */
public class WalkInMenuController {

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
     * Navigates to the reservation booking screen.
     */
    @FXML
    private void onBookReservation() {
        try {
            ConnectApp.showReservationSearch();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Navigates to the cancel reservation screen.
     */
    @FXML
    private void onCancelReservation() {
        try {
            ConnectApp.showWalkInCancelReservation();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Navigates to the leave waitlist screen.
     */
    @FXML
    private void onLeaveWaitlist() {
        try {
            ConnectApp.showWalkInLeaveWaitlist();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Navigates to the pay bill screen.
     */
    @FXML
    private void onPayBill() {
        try {
            ConnectApp.showWalkInPayBill();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Navigates back to the welcome screen.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showWelcome();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
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