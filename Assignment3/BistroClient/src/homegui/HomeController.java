package homegui;

import client.ClientController;
import client.MessageListener;
import client.Commands;
import clientgui.ConnectApp;
import common.Message;
import entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Controller for the application's home screen.
 * Responsibilities:
 *   Navigate to customer/terminal/staff areas
 *   Perform staff login (Manager/Representative only)
 *   Receive LOGIN response from the server and open the staff dashboard
 */
public class HomeController implements MessageListener {

    private ClientController controller;

    @FXML
    private void onCustomerClicked() throws Exception {
        ConnectApp.showCustomerMenu();
    }

    @FXML
    private void onTerminalClicked() throws Exception {
        ConnectApp.showTerminalSeatByCode();
    }
    
    @FXML
    private void onTerminalLostCode() throws Exception {
        ConnectApp.showTerminalLostCode();
    }

    /**
     * Opens a login dialog and sends a LOGIN request to the server.
     */
    @FXML
    private void onStaffClicked() {
        try {
            if (controller == null || !controller.isConnected()) {
                showAlert(Alert.AlertType.ERROR, "Not connected", "Please connect to the server first.");
                return;
            }

            Dialog<LoginData> dialog = buildLoginDialog();
            dialog.showAndWait().ifPresent(creds -> {
                try {
                    controller.login(creds.email(), creds.password());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Login error", e.getMessage());
                }
            });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    
    public void init(ClientController controller) {
        this.controller = controller;
        if (this.controller != null) {
            this.controller.setListener(this);
        }
    }


    /**
     * Receives messages from the server.
     * Handles staff LOGIN response and opens the staff dashboard if authorized.
     *
     * @param m message received from the server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        if (!Commands.LOGIN.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                showAlert(Alert.AlertType.ERROR, "Login failed", m.getError());
                return;
            }

            Object data = m.getData();
            if (!(data instanceof User u)) {
                showAlert(Alert.AlertType.ERROR, "Login failed", "Server returned an invalid user object.");
                return;
            }

            // Staff only
            User.UserRole role = u.getUserRole();
            if (role != User.UserRole.MANAGER && role != User.UserRole.REPRESENTATIVE) {
                showAlert(Alert.AlertType.ERROR, "Access denied", "Role is not staff: " + role);
                return;
            }

            try {
                ConnectApp.showStaffDashboard(u);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to open staff dashboard", e.getMessage());
            }
        });
    }

    /**
     * Simple record for holding login credentials from the dialog.
     *
     * @param email staff email
     * @param password staff password
     */
    private record LoginData(String email, String password) {}

    /**
     * Builds a modal dialog that collects staff email and password.
     *
     * @return dialog that returns LoginData on Login, or empty on cancel
     */
    private Dialog<LoginData> buildLoginDialog() {
        Dialog<LoginData> dialog = new Dialog<>();
        dialog.setTitle("Staff Login");

        ButtonType loginBtn = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Email:"), emailField);
        grid.addRow(1, new Label("Password:"), passField);

        dialog.getDialogPane().setContent(grid);

        // Disable login if fields empty
        dialog.getDialogPane().lookupButton(loginBtn).disableProperty()
                .bind(emailField.textProperty().isEmpty().or(passField.textProperty().isEmpty()));

        dialog.setResultConverter(btn -> {
            if (btn == loginBtn) {
                return new LoginData(emailField.getText().trim(), passField.getText());
            }
            return null;
        });

        return dialog;
    }

    /**
     * Shows a simple JavaFX alert dialog.
     *
     * @param type alert type
     * @param title dialog title
     * @param msg dialog message
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}