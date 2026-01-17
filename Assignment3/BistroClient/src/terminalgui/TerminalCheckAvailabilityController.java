package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import entities.User;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for Terminal Check Availability screen.
 * Allows customers to check table availability and join waitlist.
 */
public class TerminalCheckAvailabilityController implements MessageListener {

    @FXML private Spinner<Integer> dinersSpinner;

    @FXML private RadioButton subscriberRadio;
    @FXML private RadioButton guestRadio;

    @FXML private VBox subscriberBox;
    @FXML private TextField subscriberField;

    @FXML private VBox guestBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    @FXML private Label statusLabel;
    @FXML private Label codeLabel;
    @FXML private VBox resultBox;
    @FXML private VBox joinWaitlistBox;
    @FXML private Button checkButton;

    private ClientController controller;
    
    // Store data for joining waitlist
    private int pendingDiners;
    private String pendingSubscriberNumber;
    private String pendingPhone;
    private String pendingEmail;

    /**
     * Initializes the controller with the client controller.
     *
     * @param controller the client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        // Diners spinner
        dinersSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2)
        );

        // Toggle group (Subscriber / Guest)
        ToggleGroup tg = new ToggleGroup();
        subscriberRadio.setToggleGroup(tg);
        guestRadio.setToggleGroup(tg);

        subscriberRadio.setSelected(true);
        updateModeUI();

        tg.selectedToggleProperty().addListener((obs, oldT, newT) -> updateModeUI());

        resetResultUI();
    }

    /**
     * Updates the UI based on selected customer type.
     */
    private void updateModeUI() {
        boolean isSubscriber = subscriberRadio.isSelected();

        subscriberBox.setVisible(isSubscriber);
        subscriberBox.setManaged(isSubscriber);

        guestBox.setVisible(!isSubscriber);
        guestBox.setManaged(!isSubscriber);

        resetResultUI();
    }

    /**
     * Resets the result UI to initial state.
     */
    private void resetResultUI() {
        statusLabel.setText("Enter your details and click 'Check Availability'");
        codeLabel.setText("—");
        
        resultBox.setVisible(false);
        resultBox.setManaged(false);
        
        joinWaitlistBox.setVisible(false);
        joinWaitlistBox.setManaged(false);
    }

    /**
     * Handles the check availability button click.
     * Validates input and sends request to server.
     */
    @FXML
    private void onCheckAvailability() {
        resetResultUI();

        Integer diners = dinersSpinner.getValue();
        if (diners == null) {
            statusLabel.setText("Please select the number of guests.");
            return;
        }

        boolean isSubscriber = subscriberRadio.isSelected();

        String membershipCard = null;
        String phone = null;
        String email = null;

        if (isSubscriber) {
            membershipCard = safeTrim(subscriberField.getText());
            if (membershipCard.isEmpty()) {
                statusLabel.setText("Please scan or enter your membership card.");
                return;
            }
        } else {
            phone = safeTrim(phoneField.getText());
            email = safeTrim(emailField.getText());

            if (phone.isEmpty() || email.isEmpty()) {
                statusLabel.setText("Please enter both phone number and email.");
                return;
            }
            if (!User.isValidPhone(phone)) {
                statusLabel.setText("Invalid phone format. Use: 05XXXXXXXX");
                return;
            }
            if (!User.isValidEmail(email)) {
                statusLabel.setText("Invalid email format.");
                return;
            }
        }

        // Store data for potential waitlist join
        pendingDiners = diners;
        pendingSubscriberNumber = membershipCard; // Will be resolved on server
        pendingPhone = phone;
        pendingEmail = email;

        statusLabel.setText("Checking availability...");

        try {
            controller.checkAvailabilityTerminal(diners, membershipCard, phone, email, isSubscriber);
        } catch (IOException e) {
            statusLabel.setText("Failed to send request: " + e.getMessage());
        }
    }

    /**
     * Handles the join waitlist button click.
     */
    @FXML
    private void onJoinWaitlist() {
        statusLabel.setText("Joining waitlist...");
        joinWaitlistBox.setVisible(false);
        joinWaitlistBox.setManaged(false);

        try {
            controller.joinWaitlistTerminal(pendingDiners, pendingSubscriberNumber, pendingPhone, pendingEmail);
        } catch (IOException e) {
            statusLabel.setText("Failed to join waitlist: " + e.getMessage());
            joinWaitlistBox.setVisible(true);
            joinWaitlistBox.setManaged(true);
        }
    }

    /**
     * Handles server response messages.
     *
     * @param message the message from server
     */
    @Override
    public void onMessage(Message message) {
        if (message == null) return;

        Platform.runLater(() -> {
            String command = message.getCommand();
            
            if (Commands.CHECK_AVAILABILITY_TERMINAL.equals(command)) {
                handleCheckAvailabilityResponse(message);
            } else if (Commands.JOIN_WAITLIST.equals(command)) {
                handleJoinWaitlistResponse(message);
            }
        });
    }

    /**
     * Handles the check availability response.
     */
    private void handleCheckAvailabilityResponse(Message message) {
        if (!message.isSuccess()) {
            statusLabel.setText("Error: " + String.valueOf(message.getError()));
            return;
        }

        Object payload = message.getData();

        if (payload instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawMap;

            boolean availableNow = Boolean.TRUE.equals(map.get("availableNow"));
            Integer tableNumber = (map.get("tableNumber") instanceof Integer i) ? i : null;
            String text = (map.get("text") instanceof String t) ? t : "";

            statusLabel.setText(text);

            if (availableNow && tableNumber != null) {
                // Table available - show table number
                codeLabel.setText("Table #" + tableNumber);
                resultBox.setVisible(true);
                resultBox.setManaged(true);
                joinWaitlistBox.setVisible(false);
                joinWaitlistBox.setManaged(false);
            } else {
                // No table available - show "Join Waitlist" button
                resultBox.setVisible(false);
                resultBox.setManaged(false);
                joinWaitlistBox.setVisible(true);
                joinWaitlistBox.setManaged(true);
            }
        } else {
            statusLabel.setText("Unexpected response from server.");
        }
    }

    /**
     * Handles the join waitlist response.
     */
    private void handleJoinWaitlistResponse(Message message) {
        if (!message.isSuccess()) {
            statusLabel.setText("Failed to join waitlist: " + message.getError());
            joinWaitlistBox.setVisible(true);
            joinWaitlistBox.setManaged(true);
            return;
        }

        Object payload = message.getData();

        if (payload instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawMap;

            String entryCode = (map.get("entryCode") instanceof String s) ? s : null;
            String text = (map.get("message") instanceof String t) ? t : "You've been added to the waitlist!";

            statusLabel.setText(text);

            if (entryCode != null && !entryCode.isBlank()) {
                codeLabel.setText(entryCode);
                resultBox.setVisible(true);
                resultBox.setManaged(true);
            }

            joinWaitlistBox.setVisible(false);
            joinWaitlistBox.setManaged(false);
        } else {
            statusLabel.setText("Added to waitlist successfully!");
            joinWaitlistBox.setVisible(false);
            joinWaitlistBox.setManaged(false);
        }
    }

    /**
     * Handles back button click - returns to Terminal Menu.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showTerminalMenu();
        } catch (Exception e) {
            statusLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    /**
     * Safely trims a string, returning empty string if null.
     *
     * @param s the string to trim
     * @return trimmed string or empty string
     */
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}