package staffgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for Staff Login screen.
 * Allows staff members (Representative/Manager) to log in using email and password.
 */
public class StaffLoginController implements MessageListener {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginBtn;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        if (this.controller != null) {
            this.controller.setListener(this);
        }
        statusLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        if (!User.isValidEmail(email)) {
            statusLabel.setText("Please enter a valid email address.");
            return;
        }

        try {
            if (controller == null || !controller.isConnected()) {
                statusLabel.setText("Not connected to server. Please try again.");
                return;
            }

            statusLabel.setText("Logging in...");
            loginBtn.setDisable(true);
            controller.setListener(this);
            controller.login(email, password);

        } catch (Exception e) {
            statusLabel.setText("Login error: " + e.getMessage());
            loginBtn.setDisable(false);
        }
    }

    @FXML
    private void handleBack() {
        try {
            ConnectApp.showWelcome();
        } catch (Exception e) {
            statusLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.LOGIN.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            loginBtn.setDisable(false);

            if (!m.isSuccess()) {
                statusLabel.setText("Login failed: " + m.getError());
                return;
            }

            Object data = m.getData();
            if (!(data instanceof User user)) {
                statusLabel.setText("Server returned invalid user data.");
                return;
            }

            if (user.getUserRole() != User.UserRole.REPRESENTATIVE &&
                user.getUserRole() != User.UserRole.MANAGER) {
                statusLabel.setText("Access denied. This login is for staff only.");
                return;
            }

            try {
                // Both Manager and Representative use the same dashboard
                // Manager has all Representative capabilities plus reports
                ConnectApp.showStaffDashboard(user);
            } catch (Exception e) {
                // IMPORTANT: show real root cause
                e.printStackTrace();
                Throwable root = e;
                while (root.getCause() != null) root = root.getCause();

                String msg = root.getMessage();
                if (msg == null || msg.isBlank()) msg = e.toString();

                statusLabel.setText("Failed to open dashboard: " + msg);
            }
        });
    }
}