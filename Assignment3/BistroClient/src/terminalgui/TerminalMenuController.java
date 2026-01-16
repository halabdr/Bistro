package terminalgui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.fxml.FXML;

public class TerminalMenuController {

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
    }

    @FXML private void onSeatByCode() throws Exception {
        ConnectApp.showTerminalSeatByCode();
    }

    @FXML private void onJoinWaitlist() throws Exception {
        ConnectApp.showTerminalJoinWaitlist();
    }

    @FXML private void onLeaveWaitlist() throws Exception {
        ConnectApp.showTerminalLeaveWaitlist();
    }

    @FXML private void onLostCode() throws Exception {
        ConnectApp.showTerminalLostCode();
    }

    @FXML private void onBack() throws Exception {
        ConnectApp.showWelcome();
    }
}
