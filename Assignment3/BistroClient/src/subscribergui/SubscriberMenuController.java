package subscribergui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.fxml.FXML;

public class SubscriberMenuController {

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
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
        ConnectApp.showHome();
    }
}
