package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Terminal screen controller.
 * Seat reservation by confirmation code OR by subscriber member code (list reservations).
 */
public class SeatByCodeController implements MessageListener {

    // --- Tab 1: by confirmation code
    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    // --- Tab 2: subscriber flow
    @FXML private TextField memberCodeField;
    @FXML private ListView<Reservation> reservationsList;
    @FXML private Label selectedCodeLabel;
    @FXML private Label statusLabel2;

    private ClientController controller;

    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        // Clear UI
        resultLabel.setText("");
        setStatus(statusLabel, "", null);
        setStatus(statusLabel2, "", null);
        selectedCodeLabel.setText("");

        // ListView setup
        reservationsList.setItems(reservations);
        reservationsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText(null);
                    return;
                }

                String d = r.getBookingDate() != null ? r.getBookingDate().format(dateFmt) : "-";
                String t = r.getBookingTime() != null ? r.getBookingTime().format(timeFmt) : "-";
                String code = r.getConfirmationCode() != null ? r.getConfirmationCode() : "-";
                String status = r.getStatus() != null ? r.getStatus().name() : "-";

                setText(
                	    d + "  " + t
                	    + "   | guests: " + r.getGuestCount()
                	    + "   | status: " + status
                	    + "   | code: " + code
                	);
            }
        });

        reservationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                selectedCodeLabel.setText("");
                return;
            }
            String code = newV.getConfirmationCode();
            selectedCodeLabel.setText(code == null ? "" : code);
            if (code != null && !code.isBlank()) {
                codeField.setText(code);
            }
        });
    }

    private void setStatus(Label label, String text, String styleClass) {
        if (label == null) return;
        label.setText(text == null ? "" : text);
        label.getStyleClass().removeAll("status-ok","status-bad","status-error");
        if (styleClass != null && !styleClass.isBlank()) {
            label.getStyleClass().add(styleClass);
        }
    }
    
    @FXML
    private void onLoadReservations() {
        try {
            String member = memberCodeField.getText() == null ? "" : memberCodeField.getText().trim();
            if (member.isEmpty()) {
                setStatus(statusLabel2, "Please enter member code.", "status-bad");
                return;
            }

            reservations.clear();
            selectedCodeLabel.setText("");
            setStatus(statusLabel2, "Loading reservations...", "status-bad");

            controller.getUserReservations(member);

        } catch (Exception e) {
            setStatus(statusLabel2, "Error: " + e.getMessage(), "status-error");
        }
    }

    @FXML
    private void onSeat() {
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            setStatus(statusLabel, "Please enter a confirmation code.", "status-error");
            return;
        }

        // Clear other side messages for cleaner UX
        setStatus(statusLabel2, "", "terminal-hint");
        resultLabel.setText("");

        setStatus(statusLabel, "Seating reservation...", "terminal-hint");

        try {
            controller.seatByCode(code);
        } catch (IOException e) {
            setStatus(statusLabel, "Failed to send request: " + e.getMessage(), "status-error");
        }
    }

    @FXML
    private void onSeatSelected() {
        String code = selectedCodeLabel.getText() == null ? "" : selectedCodeLabel.getText().trim();
        if (code.isEmpty() || "-".equals(code)) {
            setStatus(statusLabel2, "Please select a reservation first.", "status-error");
            return;
        }

        // Clear other side messages for cleaner UX
        setStatus(statusLabel, "", "terminal-hint");
        resultLabel.setText("");

        setStatus(statusLabel2, "Seating selected reservation...", "terminal-hint");

        try {
            controller.seatByCode(code);
        } catch (IOException e) {
            setStatus(statusLabel2, "Failed to send request: " + e.getMessage(), "status-error");
        }
    }


    @FXML
    private void onForgotCode() throws Exception {
        ConnectApp.showTerminalLostCodeFromSeatByCode();
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        Platform.runLater(() -> {
            String cmd = m.getCommand();

            // 1) Subscriber reservations list
            if (Commands.GET_USER_RESERVATIONS.equals(cmd)) {
                if (!m.isSuccess()) {
                    setStatus(statusLabel2, "Failed: " + m.getError(), "status-error");
                    return;
                }

                Object data = m.getData();
                if (!(data instanceof List<?>)) {
                    setStatus(statusLabel2, "Invalid data received from server.", "status-error");
                    return;
                }

                @SuppressWarnings("unchecked")
                List<Reservation> list = (List<Reservation>) data;

                // keep only ACTIVE
                list.removeIf(r -> r == null || r.getStatus() != Reservation.ReservationStatus.ACTIVE);

                // Sort: closest reservation first (date + time ascending)
                list.sort((a, b) -> {
                    if (a == null && b == null) return 0;
                    if (a == null) return 1;
                    if (b == null) return -1;

                    int cmp = a.getBookingDate().compareTo(b.getBookingDate());
                    if (cmp != 0) return cmp;

                    if (a.getBookingTime() == null && b.getBookingTime() == null) return 0;
                    if (a.getBookingTime() == null) return 1;
                    if (b.getBookingTime() == null) return -1;

                    return a.getBookingTime().compareTo(b.getBookingTime());
                });

                reservations.setAll(list);

                if (reservations.isEmpty()) {
                    setStatus(statusLabel2, "No active reservations found.", "status-bad");
                } else {
                    setStatus(statusLabel2, "Loaded " + reservations.size() + " reservation(s).", "status-ok");
                }
                return;
            }

            // 2) Seat by code result (STRICT contract)
            if (Commands.SEAT_BY_CODE.equals(cmd)) {
                if (!m.isSuccess()) {
                    setStatus(statusLabel, "Failed: " + m.getError(), "status-error");
                    setStatus(statusLabel2, "Failed: " + m.getError(), "status-error");
                    return;
                }

                Object data = m.getData();

                // ✅ STRICT: must be Reservation
                if (!(data instanceof Reservation r)) {
                    setStatus(statusLabel, "Server response mismatch: expected Reservation.", "status-error");
                    setStatus(statusLabel2, "Server response mismatch: expected Reservation.", "status-error");
                    resultLabel.setText("");
                    return;
                }

                int table = r.getTableNumber();

                // ✅ STRICT: must have assigned table number (>0)
                if (table <= 0) {
                    setStatus(statusLabel, "Server response mismatch: table number not assigned.", "status-error");
                    setStatus(statusLabel2, "Server response mismatch: table number not assigned.", "status-error");
                    resultLabel.setText("");
                    return;
                }

                // Success UI
                setStatus(statusLabel, "Seated successfully.", "status-ok");
                setStatus(statusLabel2, "Seated successfully.", "status-ok");

                resultLabel.setText("Table number: " + table);
                showTablePopup(table);

                // Clear after popup
                codeField.clear();
                memberCodeField.clear();
                reservations.clear();
                selectedCodeLabel.setText("");

                return;
            }
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showTerminalMenu();
    }
    
    private void showTablePopup(int tableNumber) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("Table Ready");
        alert.setHeaderText("Please proceed to table " + tableNumber);

        alert.setContentText("Enjoy your meal 😊");

        alert.showAndWait();
    }
}