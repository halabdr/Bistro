package subscribergui;

import client.ClientController;
import clientgui.ConnectApp;
import entities.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Controller for Subscriber Menu screen.
 * Displays main menu options for logged-in subscribers.
 */
public class SubscriberMenuController {

    @FXML private Label welcomeLabel;
    
    private ClientController controller;
    private Subscriber subscriber;

    public void init(ClientController controller) {
        this.controller = controller;
    }

    public void init(ClientController controller, Subscriber subscriber) {
        this.controller = controller;
        this.subscriber = subscriber;
        
        if (subscriber != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome back, " + subscriber.getName() + "!");
        }
    }

    @FXML
    private void onBook() throws Exception {
        ConnectApp.showReservationSearch();
    }

    @FXML
    private void onViewReservations() throws Exception {
        showComingSoon("My Reservations - View your reservation history");
    }

    @FXML
    private void onCancel() throws Exception {
        ConnectApp.showCancelReservation();
    }

    @FXML
    private void onPayBill() throws Exception {
        ConnectApp.showPayBill();
    }

    @FXML
    private void onUpdateInfo() throws Exception {
        showComingSoon("Update Personal Info - Edit phone and email");
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showWelcome();
    }

    private void showComingSoon(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coming Soon");
        alert.setHeaderText(null);
        alert.setContentText(feature + " will be implemented soon!");
        alert.showAndWait();
    }
}