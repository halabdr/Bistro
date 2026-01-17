package terminalgui;

import client.ClientController;
import clientgui.ConnectApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

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

    @FXML
    private void onPayBill() {
        try {
            System.out.println("CLICK: Pay Bill");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/subscribergui/PayBill.fxml"));

            Scene scene = new Scene(loader.load());

            subscribergui.PayBillController payController = loader.getController();

            payController.init(ConnectApp.getController(), () -> {
                try {
                    ConnectApp.showTerminalMenu();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });


            Stage stage = (Stage) javafx.stage.Stage.getWindows()
                    .filtered(w -> w.isShowing())
                    .get(0);

            stage.setScene(scene);

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