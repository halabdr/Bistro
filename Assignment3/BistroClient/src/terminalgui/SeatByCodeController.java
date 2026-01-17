package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Terminal screen controller for seating customers.
 * Supports two methods:
 * 1. By confirmation code (for all customers)
 * 2. By membership card scan (for subscribers - loads their reservations)
 */
public class SeatByCodeController implements MessageListener {

    // --- Confirmation Code Section ---
    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    // --- Subscriber Section ---
    @FXML private TextField memberCardField;
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> dateCol;
    @FXML private TableColumn<Reservation, String> timeCol;
    @FXML private TableColumn<Reservation, String> guestsCol;
    @FXML private TableColumn<Reservation, String> codeCol;
    @FXML private TableColumn<Reservation, String> statusCol;
    @FXML private Label selectedCodeLabel;
    @FXML private Label statusLabel2;

    private ClientController controller;

    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Initializes the controller.
     *
     * @param controller the client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        // Clear UI
        resultLabel.setText("");
        statusLabel.setText("Enter your confirmation code above");
        statusLabel2.setText("");
        selectedCodeLabel.setText("—");

        // Setup TableView
        setupReservationsTable();
    }

    /**
     * Sets up the reservations table with columns and selection listener.
     */
    private void setupReservationsTable() {
        reservationsTable.setItems(reservations);

        // Date column
        dateCol.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            String date = r.getBookingDate() != null ? r.getBookingDate().format(dateFmt) : "-";
            return new SimpleStringProperty(date);
        });

        // Time column
        timeCol.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            String time = r.getBookingTime() != null ? r.getBookingTime().format(timeFmt) : "-";
            return new SimpleStringProperty(time);
        });

        // Guests column
        guestsCol.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            return new SimpleStringProperty(String.valueOf(r.getGuestCount()));
        });

        // Code column
        codeCol.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            String code = r.getConfirmationCode() != null ? r.getConfirmationCode() : "-";
            return new SimpleStringProperty(code);
        });

        // Status column
        statusCol.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            String status = r.getStatus() != null ? r.getStatus().name() : "-";
            return new SimpleStringProperty(status);
        });

        // Style the status column
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 700;");
                    } else {
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });

        // Selection listener
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                selectedCodeLabel.setText("—");
                return;
            }
            String code = newV.getConfirmationCode();
            selectedCodeLabel.setText(code == null ? "—" : code);
            if (code != null && !code.isBlank()) {
                codeField.setText(code);
            }
        });
    }

    /**
     * Loads reservations for the subscriber by membership card.
     */
    @FXML
    private void onLoadReservations() {
        try {
            String cardCode = memberCardField.getText() == null ? "" : memberCardField.getText().trim();
            if (cardCode.isEmpty()) {
                statusLabel2.setText("Please enter or scan your membership card code.");
                return;
            }

            reservations.clear();
            selectedCodeLabel.setText("—");
            statusLabel2.setText("Loading reservations...");

            // Send request with membership card code
            controller.getReservationsByMembershipCard(cardCode);

        } catch (Exception e) {
            statusLabel2.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Seats customer by confirmation code.
     */
    @FXML
    private void onSeat() {
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            statusLabel.setText("Please enter a confirmation code.");
            return;
        }

        statusLabel2.setText("");
        resultLabel.setText("");
        statusLabel.setText("Processing...");

        try {
            controller.seatByCode(code);
        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
        }
    }

    /**
     * Seats customer using the selected reservation from the table.
     */
    @FXML
    private void onSeatSelected() {
        String code = selectedCodeLabel.getText();
        if (code == null || code.isEmpty() || "—".equals(code)) {
            statusLabel2.setText("Please select a reservation from the table first.");
            return;
        }

        statusLabel.setText("");
        resultLabel.setText("");
        statusLabel2.setText("Processing...");

        try {
            controller.seatByCode(code);
        } catch (IOException e) {
            statusLabel2.setText("Failed: " + e.getMessage());
        }
    }

    /**
     * Navigates to the Lost Code screen.
     */
    @FXML
    private void onForgotCode() throws Exception {
        ConnectApp.showTerminalLostCodeFromSeatByCode();
    }

    /**
     * Handles server messages.
     *
     * @param m the message from server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        Platform.runLater(() -> {
            String cmd = m.getCommand();

            // Handle reservations list response (by membership card)
            if (Commands.GET_RESERVATIONS_BY_CARD.equals(cmd)) {
                handleReservationsResponse(m);
                return;
            }

            // Handle seat by code response
            if (Commands.SEAT_BY_CODE.equals(cmd)) {
                handleSeatResponse(m);
                return;
            }
        });
    }

    /**
     * Handles the reservations list response.
     */
    private void handleReservationsResponse(Message m) {
        if (!m.isSuccess()) {
            statusLabel2.setText("Failed: " + m.getError());
            return;
        }

        Object data = m.getData();
        if (!(data instanceof List<?>)) {
            statusLabel2.setText("Invalid data received from server.");
            return;
        }

        @SuppressWarnings("unchecked")
        List<Reservation> list = (List<Reservation>) data;

        // Keep only ACTIVE reservations
        list.removeIf(r -> r == null || r.getStatus() != Reservation.ReservationStatus.ACTIVE);

        // Sort: closest reservation first
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
            statusLabel2.setText("No active reservations found for this card.");
        } else {
            statusLabel2.setText("Found " + reservations.size() + " active reservation(s). Select one to seat.");
        }
    }

    /**
     * Handles the seat by code response.
     */
    private void handleSeatResponse(Message m) {
        if (!m.isSuccess()) {
            String error = m.getError();

            // Check for WAIT response
            if (error != null && error.startsWith("WAIT:")) {
                String waitMessage = error.substring(5);
                statusLabel.setText("Please Wait");
                statusLabel2.setText("Please Wait");
                resultLabel.setText("");
                showWaitPopup(waitMessage);
                return;
            }

            // Regular error
            statusLabel.setText("Failed: " + error);
            statusLabel2.setText("Failed: " + error);
            return;
        }

        Object data = m.getData();

        if (!(data instanceof Reservation r)) {
            statusLabel.setText("Server response error.");
            statusLabel2.setText("Server response error.");
            resultLabel.setText("");
            return;
        }

        Integer tableNumber = r.getAssignedTableNumber();

        if (tableNumber == null || tableNumber <= 0) {
            statusLabel.setText("No table assigned. Please contact staff.");
            statusLabel2.setText("No table assigned. Please contact staff.");
            resultLabel.setText("");
            return;
        }

        // Success
        statusLabel.setText("✅ Seated successfully!");
        statusLabel2.setText("✅ Seated successfully!");
        resultLabel.setText("Proceed to Table #" + tableNumber);

        showTablePopup(tableNumber);

        // Clear form
        codeField.clear();
        memberCardField.clear();
        reservations.clear();
        selectedCodeLabel.setText("—");
    }

    /**
     * Shows popup with assigned table number.
     */
    private void showTablePopup(int tableNumber) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("Table Ready");
        alert.setHeaderText("Please proceed to Table #" + tableNumber);
        alert.setContentText("Enjoy your meal! 🍽️");
        alert.showAndWait();
    }

    /**
     * Shows popup for waiting.
     */
    private void showWaitPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("Please Wait");
        alert.setHeaderText("No Table Available Right Now");
        alert.setContentText(message + "\n\nYou will be notified when a table is ready.");
        alert.showAndWait();
    }

    /**
     * Navigates back to terminal menu.
     */
    @FXML
    private void onBack() throws Exception {
        ConnectApp.showTerminalMenu();
    }
}