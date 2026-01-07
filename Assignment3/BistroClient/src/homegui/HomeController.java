package homegui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.event.ActionEvent;

public class HomeController {

    private ClientController clientController;

    public void setClientController(ClientController controller) {
        this.clientController = controller;
    }

    public void onCustomerClicked(ActionEvent e) {
        try {
            ConnectApp.showAvailableSlots(clientController);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onTerminalClicked(ActionEvent e) {
        System.out.println("Open terminal screen");
    }

    public void onStaffClicked(ActionEvent e) {
        System.out.println("Open staff login screen");
    }
}
