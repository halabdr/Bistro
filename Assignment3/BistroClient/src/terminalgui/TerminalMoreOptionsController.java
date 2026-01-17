package terminalgui;

import clientgui.ConnectApp;
import javafx.fxml.FXML;

/**
 * Controller for the Terminal More Options screen.
 * Provides secondary actions: cancel reservation, leave waitlist, lost code.
 */
public class TerminalMoreOptionsController {

    /**
     * Opens the cancel reservation screen.
     */
    @FXML
    private void onCancelReservation() {
        try {
            ConnectApp.showTerminalCancelReservation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the leave waitlist screen.
     */
    @FXML
    private void onLeaveWaitlist() {
        try {
            ConnectApp.showTerminalLeaveWaitlist();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the lost code screen.
     */
    @FXML
    private void onLostCode() {
        try {
            ConnectApp.showTerminalLostCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns to the main terminal menu.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showTerminalMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}