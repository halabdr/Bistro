package terminalgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import entities.Subscriber;
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
 * Supports three methods:
 * 1. By confirmation code (for all customers)
 * 2. By membership card scan (for subscribers)
 * 3. By login with email and password (for subscribers)
 */
public class SeatByCodeController implements MessageListener {

    // --- Confirmation Code Section (Left) ---
    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    // --- Subscriber Section (Right) ---
    @FXML private TabPane subscriberTabPane;
    
    // Tab 1: Membership Card
    @FXML private TextField memberCardField;
    
    // Tab 2: Login
    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;
    
    // Reservations Table
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> dateCol;
    @FXML private TableColumn<Reservation, String> timeCol;
    @FXML private TableColumn<Reservation, String> guestsCol;
    @FXML private TableColumn<Reservation, String> codeCol;
    @FXML private TableColumn<Reservation, String> statusCol;
    @FXML private Label selectedCodeLabel;
    @FXML private Label statusLabel2;

    private ClientController controller;
    private String loggedInSubscriberNumber;  // Store subscriber number after login

    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    private enum SeatSource { LEFT_CODE, RIGHT_SELECTED }
    private SeatSource lastSeatSource = SeatSource.LEFT_CODE;

    private void setStatus(String msg) {
        if (lastSeatSource == SeatSource.LEFT_CODE) {
            statusLabel.setText(msg);
        } else {
            statusLabel2.setText(msg);
        }
    }

    private void setProcessing() {
        if (lastSeatSource == SeatSource.LEFT_CODE) {
            statusLabel.setText("Processing...");
            statusLabel2.setText(""); 
        } else {
            statusLabel2.setText("Processing...");
            statusLabel.setText("");
        }
        resultLabel.setText("");
    }

    /**
     * Initializes the controller.
     *
     * @param controller the client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        this.loggedInSubscriberNumber = null;

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
     * Logs in the subscriber and loads their reservations.
     */
    @FXML
    private void onLoginAndLoad() {
        try {
            String email = loginEmailField.getText() == null ? "" : loginEmailField.getText().trim();
            String password = loginPasswordField.getText() == null ? "" : loginPasswordField.getText();

            if (email.isEmpty()) {
                statusLabel2.setText("Please enter your email.");
                return;
            }
            if (password.isEmpty()) {
                statusLabel2.setText("Please enter your password.");
                return;
            }

            reservations.clear();
            selectedCodeLabel.setText("—");
            statusLabel2.setText("Logging in...");

            // Send login request
            controller.login(email, password);

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

        lastSeatSource = SeatSource.LEFT_CODE;
        setProcessing();

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

        lastSeatSource = SeatSource.RIGHT_SELECTED;
        setProcessing();

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
        ConnectApp.showTerminalLostCode();
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

            // Handle login response
            if (Commands.LOGIN.equals(cmd)) {
                handleLoginResponse(m);
                return;
            }

            // Handle reservations list response (by membership card)
            if (Commands.GET_RESERVATIONS_BY_CARD.equals(cmd)) {
                handleReservationsResponse(m);
                return;
            }

            // Handle reservations list response (by subscriber number after login)
            if (Commands.GET_USER_RESERVATIONS.equals(cmd)) {
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
     * Handles the login response.
     */
    private void handleLoginResponse(Message m) {
        if (!m.isSuccess()) {
            statusLabel2.setText("Login failed: " + m.getError());
            return;
        }

        Object data = m.getData();
        
        // Check if this is a Subscriber
        if (data instanceof Subscriber subscriber) {
            loggedInSubscriberNumber = subscriber.getSubscriberNumber();
            statusLabel2.setText("Login successful! Loading reservations...");
            
            // Clear password field for security
            loginPasswordField.clear();
            
            // Now load reservations for this subscriber
            try {
                controller.getUserReservations(loggedInSubscriberNumber);
            } catch (IOException e) {
                statusLabel2.setText("Failed to load reservations: " + e.getMessage());
            }
        } else {
            // Not a subscriber (maybe staff?) - can't use this feature
            statusLabel2.setText("This login is for subscribers only.");
        }
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
            statusLabel2.setText("No active reservations found.");
        } else {
            statusLabel2.setText("Found " + reservations.size() + " active reservation(s). Select one to seat.");
        }
    }

    /**
     * Handles the seat by code response.
     */
    private void handleSeatResponse(Message m) {
        // Helper: update only the relevant status label
        java.util.function.Consumer<String> setStatus = msg -> {
            if (lastSeatSource == SeatSource.LEFT_CODE) {
                statusLabel.setText(msg);
            } else {
                statusLabel2.setText(msg);
            }
        };

        if (!m.isSuccess()) {
            String error = m.getError();

            // WAIT response
            if (error != null && error.startsWith("WAIT:")) {
                String waitMessage = error.substring(5);
                setStatus.accept("Please Wait");
                resultLabel.setText("");
                showWaitPopup(waitMessage);
                return;
            }

            // Too early response - show as alert
            if (error != null && error.contains("Too early")) {
                setStatus.accept("");
                resultLabel.setText("");
                showTooEarlyAlert(error);
                return;
            }

            // Regular error
            setStatus.accept("Failed: " + error);
            return;
        }

        Object data = m.getData();

        if (!(data instanceof Reservation r)) {
            setStatus.accept("Server response error.");
            resultLabel.setText("");
            return;
        }

        Integer tableNumber = r.getAssignedTableNumber();

        if (tableNumber == null || tableNumber <= 0) {
            setStatus.accept("No table assigned. Please contact staff.");
            resultLabel.setText("");
            return;
        }

        // Success
        setStatus.accept("✅ Seated successfully!");
        resultLabel.setText("Proceed to Table #" + tableNumber);

        showTablePopup(tableNumber);

        // Clear only the relevant side (better UX)
        codeField.clear();

        if (lastSeatSource == SeatSource.RIGHT_SELECTED) {
            memberCardField.clear();
            loginEmailField.clear();
            loginPasswordField.clear();
            reservations.clear();
            selectedCodeLabel.setText("—");
            loggedInSubscriberNumber = null;
        }
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
     * Shows alert when customer arrives too early.
     */
    private void showTooEarlyAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
        alert.setTitle("Too Early");
        alert.setHeaderText("You've arrived too early");
        alert.setContentText(message + "\n\nPlease come back closer to your reservation time.");
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