package terminalgui;

import clientgui.ConnectApp;
import javafx.fxml.FXML;

/**
 * Controller for the main Terminal Menu screen.
 * Provides navigation to terminal actions.
 */
public class TerminalMenuController {

    /**
     * Opens the Check Availability screen.
     */
    @FXML
    private void onCheckAvailability() {
        try {
            ConnectApp.showTerminalCheckAvailability();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the Seat by Code screen.
     */
    @FXML
    private void onSeatByCode() {
        try {
            ConnectApp.showTerminalSeatByCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the Pay Bill screen.
     */
    @FXML
    private void onPayBill() {
        try {
            ConnectApp.showTerminalPayBill();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the More Options screen (cancel, leave waitlist, lost code).
     */
    @FXML
    private void onMoreOptions() {
        try {
            ConnectApp.showTerminalMoreOptions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns to the welcome/home screen.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showWelcome();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}