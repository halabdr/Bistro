package reservationgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controller for creating a reservation.
 * Sends CREATE_RESERVATION and shows a styled confirmation popup.
 */
public class CreateReservationController implements MessageListener {

    // Summary labels
    @FXML private Label dateValue;
    @FXML private Label timeValue;
    @FXML private Label guestsValue;
    @FXML private Label subscriberValue;
    @FXML private TextArea notesArea;
    @FXML private Label statusLabel;

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
        subscriberValue.setText(subscriber != null ? subscriber.getSubscriberNumber() : "-");

        statusLabel.setText("");

        // Make sure overlay starts hidden
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
            if (subscriber == null || subscriber.getSubscriberNumber() == null || subscriber.getSubscriberNumber().isBlank()) {
                statusLabel.setText("Missing subscriber number.");
                return;
            }

            if (confirmBtn != null) confirmBtn.setDisable(true);
            statusLabel.setText("Confirming reservation...");

            LocalTime time = LocalTime.parse(hhmm);

            // notesArea currently not sent; can be added later if server supports it
            controller.createReservation(date, time, guests, subscriber.getSubscriberNumber());

        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
            if (confirmBtn != null) confirmBtn.setDisable(false);

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            if (confirmBtn != null) confirmBtn.setDisable(false);
        }
    }

    /**
     * Called when server replies.
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

                copiedLabel.setText(""); // reset "Copied" text
                showSuccessOverlay();

                statusLabel.setText(""); // keep screen clean
            } else {
                // Fallback if server didn't send a Reservation object
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

    /**
     * Done -> go to My Reservations.
     */
    @FXML
    private void onCloseSuccess() {
        try {
            hideSuccessOverlay();

            // Go to My Reservations
            if (subscriber != null) {
                ConnectApp.showViewReservations(subscriber);
            } else {
                // fallback: if somehow no subscriber, go back to menu/login
                ConnectApp.showSubscriberLogin();
            }
        } catch (Exception e) {
            // If navigation fails, keep user on this page and show message
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