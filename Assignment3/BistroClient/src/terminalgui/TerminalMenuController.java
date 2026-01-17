package terminalgui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class TerminalMenuController {

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
    }

    @FXML
    private void onSeatByCode() {
        try {
            System.out.println("CLICK: Seat by Code");
            ConnectApp.showTerminalSeatByCode();
        } catch (Exception e) {
            showNavError(e);
        }
    }
    
    /**
     * Opens the Cancel Reservation screen.
     */
    @FXML
    private void onCancelReservation() {
        try {
            ConnectApp.showTerminalCancelReservation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCheckAvailability() {
        try {
            System.out.println("CLICK: Check Availability");
            ConnectApp.showTerminalCheckAvailability();
        } catch (Exception e) {
            showNavError(e);
        }
    }

    @FXML
    private void onLeaveWaitlist() {
        try {
            System.out.println("CLICK: Leave Waitlist");
            ConnectApp.showTerminalLeaveWaitlist();
        } catch (Exception e) {
            showNavError(e);
        }
    }

    @FXML
    private void onLostCode() {
        try {
            System.out.println("CLICK: Lost Code");
            ConnectApp.showTerminalLostCode();
        } catch (Exception e) {
            showNavError(e);
        }
    }

    @FXML
    private void onBack() {
        try {
            System.out.println("CLICK: Back");
            ConnectApp.showWelcome();
        } catch (Exception e) {
            showNavError(e);
        }
    }

    private void showNavError(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(e.getClass().getSimpleName());
        alert.setContentText(String.valueOf(e.getMessage()));
        alert.showAndWait();
    }
}