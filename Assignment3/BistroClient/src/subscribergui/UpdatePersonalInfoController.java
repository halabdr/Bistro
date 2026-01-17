package subscribergui;
import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

/**
 * Controller for updating subscriber's personal information.
 * Allows subscribers to update phone number, email address, and password.
 */
public class UpdatePersonalInfoController implements MessageListener {

    @FXML private TextField nameField;
    @FXML private TextField subscriberNumberField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private ClientController controller;
    private Subscriber subscriber;
    
    // Store original values to detect changes
    private String originalPhone;
    private String originalEmail;

    /**
     * Initializes the controller with subscriber information.
     * 
     * @param controller connected client controller
     * @param subscriber logged-in subscriber
     */
    public void init(ClientController controller, Subscriber subscriber) {
        this.controller = controller;
        this.subscriber = subscriber;
        
        // CRITICAL: Always set listener first, before any server communication
        if (this.controller != null) {
            this.controller.setListener(this);
        }

        loadSubscriberInfo();
    }

    /**
     * Loads subscriber information into the form fields.
     */
    private void loadSubscriberInfo() {
        if (subscriber == null) {
            statusLabel.setText("Error: Subscriber information not available");
            return;
        }

        // Read-only fields
        nameField.setText(subscriber.getName());
        subscriberNumberField.setText(subscriber.getSubscriberNumber());

        // Editable fields - store originals
        originalPhone = subscriber.getPhoneNumber();
        originalEmail = subscriber.getEmailAddress();
        
        phoneField.setText(originalPhone);
        emailField.setText(originalEmail);

        statusLabel.setText("");
    }

    /**
     * Validates and saves the updated information.
     */
    @FXML
    private void onSave() {
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Check what changed
        boolean phoneChanged = !phone.equals(originalPhone);
        boolean emailChanged = !email.equals(originalEmail);
        boolean passwordFieldsFilled = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty();

        // Check if anything changed
        if (!phoneChanged && !emailChanged && !passwordFieldsFilled) {
            showError("No changes detected. Please modify at least one field.");
            return;
        }

        // Validation - Phone (if changed)
        if (phoneChanged) {
            if (phone.isEmpty()) {
                showError("Phone number cannot be empty");
                return;
            }
            if (!isValidPhone(phone)) {
                showError("Please enter a valid phone number (10 digits starting with 05)");
                return;
            }
        }

        // Validation - Email (if changed)
        if (emailChanged) {
            if (email.isEmpty()) {
                showError("Email address cannot be empty");
                return;
            }
            if (!isValidEmail(email)) {
                showError("Please enter a valid email address");
                return;
            }
        }

        // Validation - Password Change (if any field filled)
        boolean changingPassword = false;
        if (passwordFieldsFilled) {
            // All password fields must be filled
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showError("To change password, please fill all three password fields");
                return;
            }

            // Verify current password
            if (!currentPassword.equals(subscriber.getUserPassword())) {
                showError("Current password is incorrect");
                return;
            }

            // Validate new password
            if (newPassword.length() < 6) {
                showError("New password must be at least 6 characters long");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("New password and confirmation do not match");
                return;
            }

            if (newPassword.equals(currentPassword)) {
                showError("New password must be different from current password");
                return;
            }

            changingPassword = true;
        }

        // Build summary of changes
        StringBuilder changesSummary = new StringBuilder("Updating: ");
        if (phoneChanged) changesSummary.append("Phone, ");
        if (emailChanged) changesSummary.append("Email, ");
        if (changingPassword) changesSummary.append("Password, ");
        
        // Remove trailing comma
        String summary = changesSummary.toString().replaceAll(", $", "");

        // Update subscriber object with new values
        subscriber.setPhoneNumber(phone);
        subscriber.setEmailAddress(email);
        if (changingPassword) {
            subscriber.setUserPassword(newPassword);
        }

        // Send to server
        try {
        	controller.setListener(this);
            statusLabel.setText(summary + "...");
            statusLabel.setStyle("-fx-text-fill: #4A5568;");
            controller.sendToServer(new common.Message(Commands.UPDATE_USER, subscriber));
        } catch (IOException e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    /**
     * Validates email format.
     * 
     * @param email email address to validate
     * @return true if valid
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validates phone number format (Israeli format: 10 digits starting with 05).
     * 
     * @param phone phone number to validate
     * @return true if valid
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("^05\\d{8}$");
    }

    /**
     * Shows an error message.
     * 
     * @param message error message
     */
    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-weight: bold;");
    }

    /**
     * Shows a success alert dialog.
     * 
     * @param message success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles server responses.
     * 
     * @param message server response message
     */
    @Override
    public void onMessage(Message message) {
        if (message == null) {
            return;
        }

        if (!Commands.UPDATE_USER.equals(message.getCommand())) {
            return;
        }

        Platform.runLater(() -> {
            if (message.isSuccess()) {
                statusLabel.setText("✅ Changes saved successfully!");
                statusLabel.setStyle("-fx-text-fill: #38A169; -fx-font-weight: bold;");
                
                // Update original values
                originalPhone = phoneField.getText().trim();
                originalEmail = emailField.getText().trim();
                
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                
                showSuccess("Your personal information has been updated successfully.");
            } else {
                showError("Failed to save: " + message.getError());
            }
        });
    }

    /**
     * Navigates back to the subscriber menu.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showSubscriberMenu(subscriber);
        } catch (Exception e) {
            statusLabel.setText("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}