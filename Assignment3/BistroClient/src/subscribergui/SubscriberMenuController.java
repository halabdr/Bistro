package subscribergui;

import client.ClientController;
import clientgui.ConnectApp;
import entities.Subscriber;
import javafx.fxml.FXML;

public class SubscriberMenuController {

    private ClientController controller;
    private Subscriber subscriber;

    public void init(ClientController controller) {
        this.controller = controller;
    }

    public void init(ClientController controller, Subscriber subscriber) {
        this.controller = controller;
        this.subscriber = subscriber;
    }

    @FXML
    private void onBook() throws Exception {
        ConnectApp.showReservationSearch();
    }

    @FXML
    private void onCancel() throws Exception {
        ConnectApp.showCancelReservation();
    }

    @FXML
    private void onLostCode() throws Exception {
        ConnectApp.showLostCode();
    }

    @FXML
    private void onPayBill() throws Exception {
        ConnectApp.showPayBill();
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showWelcome();
    }
}