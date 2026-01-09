package homegui;

import clientgui.ConnectApp;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void onCustomerClicked() throws Exception {
        ConnectApp.showCustomerMenu();
    }

    @FXML
    private void onTerminalClicked() throws Exception {
        ConnectApp.showTerminalSeatByCode();
    }

    @FXML
    private void onStaffClicked() {
    	ConnectApp.showStaffLogin();
    }
}
