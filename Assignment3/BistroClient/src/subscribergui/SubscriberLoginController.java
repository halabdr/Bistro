package subscribergui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Subscriber;
import entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import entities.User;

/**
 * Controller for Subscriber Login screen.
 * Allows subscribers to log in using:
 * 1. Email + Password
 * 2. Subscriber Number
 */
public class SubscriberLoginController implements MessageListener {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField subscriberNumberField;
    @FXML private Button loginBtn;
    @FXML private Button loginByNumberBtn;

    private ClientController controller;

    /**
     * Initializes the controller with the client controller.
     * 
     * @param controller the client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        if (this.controller != null) {
            this.controller.setListener(this);
        }
    }

    /**
     * Handles login with email and password.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", 
                     "Please enter both email and password.");
            return;
        }
        
        if (!User.isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", 
                     "Please enter a valid email address.");
            return;
        }

        try {
            if (controller == null || !controller.isConnected()) {
                showAlert(Alert.AlertType.ERROR, "Not Connected", 
                         "Please connect to the server first.");
                return;
            }

            controller.login(email, password);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Login Error", e.getMessage());
        }
    }

    /**
     * Handles login with subscriber number only.
     */
    @FXML
    private void handleLoginByNumber() {
        String subscriberNumber = subscriberNumberField.getText().trim();

        if (subscriberNumber.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", 
                     "Please enter your subscriber number.");
            return;
        }

        try {
            if (controller == null || !controller.isConnected()) {
                showAlert(Alert.AlertType.ERROR, "Not Connected", 
                         "Please connect to the server first.");
                return;
            }

            controller.loginBySubscriberNumber(subscriberNumber);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Login Error", e.getMessage());
        }
    }

    /**
     * Handles back button click - returns to Welcome screen.
     */
    @FXML
    private void handleBack() {
        try {
            ConnectApp.showWelcome();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    /**
     * Receives messages from the server.
     * Handles LOGIN response and navigates to Subscriber Menu on success.
     * 
     * @param m message received from the server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        if (!Commands.LOGIN.equals(m.getCommand()) && 
            !Commands.LOGIN_BY_SUBSCRIBER_NUMBER.equals(m.getCommand())) {
            return;
        }

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", m.getError());
                return;
            }

            Object data = m.getData();
            if (!(data instanceof User user)) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", 
                         "Server returned invalid user data.");
                return;
            }

            if (user.getUserRole() != User.UserRole.SUBSCRIBER) {
                showAlert(Alert.AlertType.ERROR, "Access Denied", 
                         "This login is for subscribers only.");
                return;
            }

            try {
                Subscriber subscriber = (Subscriber) user;
                ConnectApp.showSubscriberMenu(subscriber);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                         "Failed to open subscriber menu: " + e.getMessage());
            }
        });
    }

    /**
     * Shows an alert dialog.
     * 
     * @param type alert type
     * @param title dialog title
     * @param message dialog message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}