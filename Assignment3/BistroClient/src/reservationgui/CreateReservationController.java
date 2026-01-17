package reservationgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controller for creating a reservation.
 * Supports both subscribers and walk-in customers.
 */
public class CreateReservationController implements MessageListener {

    // Summary labels
    @FXML private Label dateValue;
    @FXML private Label timeValue;
    @FXML private Label guestsValue;
    @FXML private Label subscriberValue;
    @FXML private TextArea notesArea;
    @FXML private Label statusLabel;

    // Walk-in customer fields
    @FXML private VBox guestInfoBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    // Success overlay (popup)
    @FXML private StackPane successOverlay;
    @FXML private Label confirmationCodeLabel;
    @FXML private Label reservationIdLabel;
    @FXML private Label copiedLabel;

    // Buttons
    @FXML private Button confirmBtn;

    private String lastConfirmationCode = null;

    private entities.Subscriber subscriber;
    private ClientController controller;
    private LocalDate date;
    private String hhmm;
    private int guests;

    /**
     * Initializes the controller with reservation details.
     *
     * @param controller shared client controller
     * @param subscriber logged-in subscriber (null for walk-in)
     * @param date reservation date
     * @param hhmm reservation time in HH:mm format
     * @param guests number of guests
     */
    public void init(ClientController controller, entities.Subscriber subscriber,
                     LocalDate date, String hhmm, int guests) {

        this.controller = controller;
        this.controller.setListener(this);

        this.subscriber = subscriber;
        this.date = date;
        this.hhmm = hhmm;
        this.guests = guests;

        dateValue.setText(String.valueOf(date));
        timeValue.setText(hhmm);
        guestsValue.setText(String.valueOf(guests));

        // Show/hide fields based on customer type
        if (subscriber != null) {
            subscriberValue.setText(subscriber.getSubscriberNumber());
            // Hide guest info box for subscribers
            if (guestInfoBox != null) {
                guestInfoBox.setVisible(false);
                guestInfoBox.setManaged(false);
            }
        } else {
            subscriberValue.setText("Walk-in Customer");
            // Show guest info box for walk-in customers
            if (guestInfoBox != null) {
                guestInfoBox.setVisible(true);
                guestInfoBox.setManaged(true);
            }
        }

        statusLabel.setText("");
        hideSuccessOverlay();
    }

    /**
     * Navigates back to the reservation search screen.
     */
    @FXML
    private void onBack() throws Exception {
        ConnectApp.showReservationSearch(subscriber);
    }

    /**
     * Sends CREATE_RESERVATION request.
     */
    @FXML
    private void onConfirm() {
        try {
            String subNumber = null;
            String guestPhone = null;
            String guestEmail = null;

            if (subscriber != null) {
                // Subscriber reservation
                subNumber = subscriber.getSubscriberNumber();
                if (subNumber == null || subNumber.isBlank()) {
                    statusLabel.setText("Missing subscriber number.");
                    return;
                }
            } else {
                // Walk-in customer - need phone or email
                if (phoneField != null) {
                    guestPhone = phoneField.getText() != null ? phoneField.getText().trim() : "";
                }
                if (emailField != null) {
                    guestEmail = emailField.getText() != null ? emailField.getText().trim() : "";
                }

                if (guestPhone.isEmpty() && guestEmail.isEmpty()) {
                    statusLabel.setText("Please enter your phone number or email.");
                    return;
                }

                // Validate phone format if provided
                if (!guestPhone.isEmpty() && !User.isValidPhone(guestPhone)) {
                    statusLabel.setText("Invalid phone format. Use: 05X-XXXXXXX");
                    return;
                }

                // Validate email format if provided
                if (!guestEmail.isEmpty() && !User.isValidEmail(guestEmail)) {
                    statusLabel.setText("Invalid email format.");
                    return;
                }
            }

            if (confirmBtn != null) confirmBtn.setDisable(true);
            statusLabel.setText("Confirming reservation...");

            LocalTime time = LocalTime.parse(hhmm);

            // Send reservation request
            controller.createReservation(date, time, guests, subNumber, guestPhone, guestEmail);

        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
            if (confirmBtn != null) confirmBtn.setDisable(false);

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            if (confirmBtn != null) confirmBtn.setDisable(false);
        }
    }

    /**
     * Handles server response for reservation creation.
     *
     * @param m message from server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.CREATE_RESERVATION.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (confirmBtn != null) confirmBtn.setDisable(false);

            if (!m.isSuccess()) {
                statusLabel.setText("Error: " + m.getError());
                return;
            }

            Object data = m.getData();
            if (data instanceof Reservation r) {
                lastConfirmationCode = r.getConfirmationCode();

                confirmationCodeLabel.setText(r.getConfirmationCode());
                reservationIdLabel.setText("Reservation ID: #" + r.getReservationId());

                copiedLabel.setText("");
                showSuccessOverlay();

                statusLabel.setText("");
            } else {
                statusLabel.setText("Reservation confirmed.");
            }
        });
    }

    // ===== Popup actions =====

    /**
     * Copies the confirmation code to clipboard.
     */
    @FXML
    private void onCopyCode() {
        if (lastConfirmationCode == null || lastConfirmationCode.isBlank()) return;

        ClipboardContent content = new ClipboardContent();
        content.putString(lastConfirmationCode);
        Clipboard.getSystemClipboard().setContent(content);

        if (copiedLabel != null) copiedLabel.setText("Copied ✅");
    }

    /**
     * Closes the success popup and navigates appropriately.
     */
    @FXML
    private void onCloseSuccess() {
        try {
            hideSuccessOverlay();

            if (subscriber != null) {
                ConnectApp.showViewReservations(subscriber);
            } else {
                // Walk-in customer - go back to welcome
                ConnectApp.showWelcome();
            }
        } catch (Exception e) {
            statusLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    private void showSuccessOverlay() {
        if (successOverlay == null) return;
        successOverlay.setManaged(true);
        successOverlay.setVisible(true);
    }

    private void hideSuccessOverlay() {
        if (successOverlay == null) return;
        successOverlay.setVisible(false);
        successOverlay.setManaged(false);
    }
}