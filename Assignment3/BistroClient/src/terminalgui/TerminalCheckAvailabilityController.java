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

    private ClientController controller;

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

        statusLabel.setText("Enter your details and click 'Check Availability'");
        codeLabel.setText("—");
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

        statusLabel.setText("Enter your details and click 'Check Availability'");
        codeLabel.setText("—");
    }

    /**
     * Handles the check availability button click.
     * Validates input and sends request to server.
     */
    @FXML
    private void onCheckAvailability() {
        statusLabel.setText("");
        codeLabel.setText("—");

        Integer diners = dinersSpinner.getValue();
        if (diners == null) {
            statusLabel.setText("Please select the number of guests.");
            return;
        }

        boolean isSubscriber = subscriberRadio.isSelected();

        String subscriberNumber = null;
        String phone = null;
        String email = null;

        if (isSubscriber) {
            subscriberNumber = safeTrim(subscriberField.getText());
            if (subscriberNumber.isEmpty()) {
                statusLabel.setText("Please enter subscriber number.");
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
                statusLabel.setText("Invalid phone format. Use: 05X-XXXXXXX");
                return;
            }
            if (!User.isValidEmail(email)) {
                statusLabel.setText("Invalid email format.");
                return;
            }
        }

        statusLabel.setText("Checking availability...");

        try {
            controller.checkAvailabilityTerminal(diners, subscriberNumber, phone, email);

        } catch (IOException e) {
            statusLabel.setText("Failed to send request: " + e.getMessage());
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

        if (!Commands.CHECK_AVAILABILITY_TERMINAL.equals(message.getCommand())) return;

        Platform.runLater(() -> {
            try {
                if (!message.isSuccess()) {
                    statusLabel.setText("Failed: " + String.valueOf(message.getError()));
                    codeLabel.setText("—");
                    return;
                }

                Object payload = message.getData();

                if (payload instanceof Map<?, ?> rawMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) rawMap;

                    boolean availableNow = Boolean.TRUE.equals(map.get("availableNow"));
                    Integer tableNumber = (map.get("tableNumber") instanceof Integer i) ? i : null;
                    String entryCode = (map.get("entryCode") instanceof String s) ? s : null;
                    String text = (map.get("text") instanceof String t) ? t : "";

                    statusLabel.setText(text);

                    if (availableNow && tableNumber != null) {
                        codeLabel.setText("Table #" + tableNumber);
                    } else if (entryCode != null && !entryCode.isBlank()) {
                        codeLabel.setText(entryCode);
                    } else {
                        codeLabel.setText("—");
                    }
                } else {
                    statusLabel.setText("Received response.");
                    codeLabel.setText(String.valueOf(payload));
                }
            } catch (Exception ex) {
                statusLabel.setText("Failed to parse server response: " + ex.getMessage());
                codeLabel.setText("—");
            }
        });
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