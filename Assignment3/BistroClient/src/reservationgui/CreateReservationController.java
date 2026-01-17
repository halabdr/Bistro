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
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controller for creating a reservation confirmation screen.
 * Supports both subscribers and walk-in customers.
 *
 * Walk-in policy:
 * - MUST provide BOTH phone and email
 * - phone and email formats are validated
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

    // ⚠ icons + inline errors
    @FXML private Label phoneWarnIcon;
    @FXML private Label emailWarnIcon;
    @FXML private Label phoneErrorLabel;
    @FXML private Label emailErrorLabel;

    // Success overlay
    @FXML private StackPane successOverlay;
    @FXML private Label confirmationCodeLabel;
    @FXML private Label reservationIdLabel;
    @FXML private Label copiedLabel;

    // Button
    @FXML private Button confirmBtn;

    private String lastConfirmationCode;

    private entities.Subscriber subscriber;
    private ClientController controller;
    private LocalDate date;
    private String hhmm;
    private int guests;

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

        if (subscriber != null) {
            subscriberValue.setText(subscriber.getSubscriberNumber());
            if (guestInfoBox != null) {
                guestInfoBox.setVisible(false);
                guestInfoBox.setManaged(false);
            }
        } else {
            subscriberValue.setText("Walk-in Customer");
            if (guestInfoBox != null) {
                guestInfoBox.setVisible(true);
                guestInfoBox.setManaged(true);
            }
        }

        statusLabel.setText("");
        hideSuccessOverlay();

        clearGuestErrors();

        // Remove error UI when user types
        if (phoneField != null) {
            phoneField.textProperty().addListener((obs, o, n) -> clearPhoneErrorUI());
        }
        if (emailField != null) {
            emailField.textProperty().addListener((obs, o, n) -> clearEmailErrorUI());
        }
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showReservationSearch(subscriber);
    }

    @FXML
    private void onConfirm() {
        try {
            // Always clear previous global status
            statusLabel.setText("");

            String subNumber = null;
            String guestPhone = null;
            String guestEmail = null;

            if (subscriber != null) {
                subNumber = subscriber.getSubscriberNumber();
                if (subNumber == null || subNumber.isBlank()) {
                    statusLabel.setText("Missing subscriber number.");
                    return;
                }

                // subscribers shouldn't show errors for walk-in fields
                clearGuestErrors();

            } else {
                // Walk-in: MUST provide BOTH phone and email
                guestPhone = (phoneField != null && phoneField.getText() != null)
                        ? phoneField.getText().trim()
                        : "";
                guestEmail = (emailField != null && emailField.getText() != null)
                        ? emailField.getText().trim()
                        : "";

                clearGuestErrors();

                boolean phoneMissing = guestPhone.isEmpty();
                boolean emailMissing = guestEmail.isEmpty();

                if (phoneMissing || emailMissing) {
                    statusLabel.setText("Please enter BOTH phone number and email.");
                    if (phoneMissing) setPhoneErrorUI("Phone number is required.");
                    if (emailMissing) setEmailErrorUI("Email address is required.");
                    return;
                }

                if (!User.isValidPhone(guestPhone)) {
                    statusLabel.setText("Invalid phone format. Use: 05X-XXXXXXX");
                    setPhoneErrorUI("Invalid format. Example: 050-1234567");
                    return;
                }

                if (!User.isValidEmail(guestEmail)) {
                    statusLabel.setText("Invalid email format.");
                    setEmailErrorUI("Please enter a valid email (e.g., name@gmail.com)");
                    return;
                }
            }

            if (confirmBtn != null) confirmBtn.setDisable(true);
            statusLabel.setText("Confirming reservation...");

            LocalTime time = LocalTime.parse(hhmm);

            // normalize empty -> null
            if (guestPhone != null && guestPhone.isBlank()) guestPhone = null;
            if (guestEmail != null && guestEmail.isBlank()) guestEmail = null;

            controller.createReservation(date, time, guests, subNumber, guestPhone, guestEmail);

        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
            if (confirmBtn != null) confirmBtn.setDisable(false);

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            if (confirmBtn != null) confirmBtn.setDisable(false);
        }
    }

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

                if (copiedLabel != null) copiedLabel.setText("");
                showSuccessOverlay();

                statusLabel.setText("");
            } else {
                statusLabel.setText("Reservation confirmed.");
            }
        });
    }

    // ===== Popup actions =====

    @FXML
    private void onCopyCode() {
        if (lastConfirmationCode == null || lastConfirmationCode.isBlank()) return;

        ClipboardContent content = new ClipboardContent();
        content.putString(lastConfirmationCode);
        Clipboard.getSystemClipboard().setContent(content);

        if (copiedLabel != null) copiedLabel.setText("Copied ✅");
    }

    @FXML
    private void onCloseSuccess() {
        try {
            hideSuccessOverlay();

            if (subscriber != null) {
                ConnectApp.showViewReservations(subscriber);
            } else {
                ConnectApp.showWalkInMenu();
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

    // ===== Error UI helpers =====

    private void markError(TextField field, boolean hasError) {
        if (field == null) return;
        if (hasError) {
            if (!field.getStyleClass().contains("input-error")) {
                field.getStyleClass().add("input-error");
            }
        } else {
            field.getStyleClass().remove("input-error");
        }
    }

    private void setPhoneErrorUI(String msg) {
        markError(phoneField, true);
        if (phoneWarnIcon != null) {
            phoneWarnIcon.setVisible(true);
            phoneWarnIcon.setManaged(true);
        }
        if (phoneErrorLabel != null) {
            phoneErrorLabel.setText(msg);
            phoneErrorLabel.setVisible(true);
            phoneErrorLabel.setManaged(true);
        }
    }

    private void setEmailErrorUI(String msg) {
        markError(emailField, true);
        if (emailWarnIcon != null) {
            emailWarnIcon.setVisible(true);
            emailWarnIcon.setManaged(true);
        }
        if (emailErrorLabel != null) {
            emailErrorLabel.setText(msg);
            emailErrorLabel.setVisible(true);
            emailErrorLabel.setManaged(true);
        }
    }

    private void clearPhoneErrorUI() {
        markError(phoneField, false);
        if (phoneWarnIcon != null) {
            phoneWarnIcon.setVisible(false);
            phoneWarnIcon.setManaged(false);
        }
        if (phoneErrorLabel != null) {
            phoneErrorLabel.setText("");
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);
        }
    }

    private void clearEmailErrorUI() {
        markError(emailField, false);
        if (emailWarnIcon != null) {
            emailWarnIcon.setVisible(false);
            emailWarnIcon.setManaged(false);
        }
        if (emailErrorLabel != null) {
            emailErrorLabel.setText("");
            emailErrorLabel.setVisible(false);
            emailErrorLabel.setManaged(false);
        }
    }

    private void clearGuestErrors() {
        clearPhoneErrorUI();
        clearEmailErrorUI();
    }
}