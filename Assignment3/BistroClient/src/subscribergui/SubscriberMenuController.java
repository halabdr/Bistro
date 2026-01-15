package subscribergui;

import client.ClientController;
import clientgui.ConnectApp;
import entities.Subscriber;
import javafx.fxml.FXML;
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
        ConnectApp.showReservationSearch(subscriber);
    }

    @FXML
    private void onViewReservations() throws Exception {
        if (subscriber != null) {
            ConnectApp.showViewReservations(subscriber);
        }
    }

    @FXML
    private void onPayBill() throws Exception {
        ConnectApp.showPayBill();
    }

    @FXML
    private void onUpdateInfo() throws Exception {
        if (subscriber != null) {
            ConnectApp.showUpdatePersonalInfo(subscriber);
        }
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showWelcome();
    }
}