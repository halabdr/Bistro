package subscribergui;
import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Reservation;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for viewing subscriber's reservation history.
 * Displays all reservations with optional filtering for active reservations only.
 */
public class ViewReservationsController implements MessageListener {

    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> codeCol;
    @FXML private TableColumn<Reservation, String> dateCol;
    @FXML private TableColumn<Reservation, String> timeCol;
    @FXML private TableColumn<Reservation, Number> guestsCol;
    @FXML private TableColumn<Reservation, String> statusCol;
    @FXML private TableColumn<Reservation, String> tableCol;
    @FXML private TableColumn<Reservation, Void> actionsCol;
    
    @FXML private CheckBox showActiveOnlyCheckBox;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;

    private ClientController controller;
    private Subscriber subscriber;
    private List<Reservation> allReservations;

    /**
     * Initializes the controller with client controller and subscriber information.
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

        setupTableColumns();
        setupActionsColumn();
        setupCheckBoxListener();
        loadReservations();
    }

    /**
     * Configures table column bindings to display reservation data.
     */
    private void setupTableColumns() {
        if (codeCol != null) {
            codeCol.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getConfirmationCode()));
        }

        if (dateCol != null) {
            dateCol.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(cellData.getValue().getBookingDate())));
        }

        if (timeCol != null) {
            timeCol.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(cellData.getValue().getBookingTime())));
        }

        if (guestsCol != null) {
            guestsCol.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleIntegerProperty(
                            cellData.getValue().getGuestCount()));
        }

        if (statusCol != null) {
            statusCol.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            formatStatus(cellData.getValue().getStatus())));
        }

        if (tableCol != null) {
            tableCol.setCellValueFactory(cellData -> {
                int tableNum = cellData.getValue().getTableNumber();
                String display = (tableNum > 0) ? String.valueOf(tableNum) : "-";
                return new javafx.beans.property.SimpleStringProperty(display);
            });
        }
    }

    /**
     * Sets up the Actions column with Cancel buttons for each active reservation.
     */
    private void setupActionsColumn() {
        if (actionsCol == null) {
            return;
        }

        actionsCol.setCellFactory(col -> new TableCell<Reservation, Void>() {
            private final Button cancelBtn = new Button("Cancel");

            {
                cancelBtn.setStyle(
                    "-fx-background-color: #FC8181; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 12px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 16; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;"
                );

                cancelBtn.setOnMouseEntered(e -> 
                    cancelBtn.setStyle(
                        "-fx-background-color: #F56565; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 6 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
                    )
                );

                cancelBtn.setOnMouseExited(e -> 
                    cancelBtn.setStyle(
                        "-fx-background-color: #FC8181; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 6 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
                    )
                );

                cancelBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleCancelReservation(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Reservation reservation = getTableView().getItems().get(getIndex());
                
                // Only show Cancel button for ACTIVE reservations
                if (reservation.getStatus() == Reservation.ReservationStatus.ACTIVE) {
                    HBox container = new HBox(cancelBtn);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                } else {
                    Label statusLabel = new Label("-");
                    statusLabel.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 12px;");
                    HBox container = new HBox(statusLabel);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    /**
     * Handles canceling a reservation directly from the table.
     * Shows confirmation dialog and sends cancellation request to server.
     * 
     * @param reservation the reservation to cancel
     */
    private void handleCancelReservation(Reservation reservation) {
        // Create confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Cancel Reservation");
        confirmDialog.setHeaderText("Are you sure you want to cancel this reservation?");
        confirmDialog.setContentText(
            "Confirmation Code: " + reservation.getConfirmationCode() + "\n" +
            "Date: " + reservation.getBookingDate() + "\n" +
            "Time: " + reservation.getBookingTime() + "\n" +
            "Guests: " + reservation.getGuestCount()
        );

        // Customize buttons
        ButtonType yesButton = new ButtonType("Yes, Cancel It", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No, Keep It", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(yesButton, noButton);

        // Show dialog and wait for response
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == yesButton) {
            // User confirmed - send cancellation request
            try {
                statusLabel.setText("Cancelling reservation...");
                controller.cancelReservation(reservation.getConfirmationCode());
            } catch (IOException e) {
                statusLabel.setText("Failed to cancel: " + e.getMessage());
                showErrorAlert("Cancellation Failed", "Could not cancel reservation: " + e.getMessage());
            }
        }
    }

    /**
     * Sets up listener for the "Active Only" checkbox to filter reservations.
     */
    private void setupCheckBoxListener() {
        if (showActiveOnlyCheckBox != null) {
            showActiveOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                filterReservations();
            });
        }
    }

    /**
     * Sends request to server to load user's reservations.
     */
    private void loadReservations() {
        if (subscriber == null || subscriber.getSubscriberNumber() == null) {
            statusLabel.setText("Error: Subscriber information not available");
            return;
        }

        try {
            statusLabel.setText("Loading reservations...");
            controller.getUserReservations(subscriber.getSubscriberNumber());
        } catch (Exception e) {
            statusLabel.setText("Failed to load reservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manually refreshes the reservations table.
     * Called when user clicks the Refresh button.
     */
    @FXML
    private void onRefresh() {
        // Re-register listener before refreshing
        if (controller != null) {
            controller.setListener(this);
        }
        statusLabel.setText("Refreshing...");
        loadReservations();
    }

    /**
     * Filters reservations based on checkbox state.
     * Shows only active reservations when checkbox is selected.
     */
    private void filterReservations() {
        if (allReservations == null || allReservations.isEmpty()) {
            return;
        }

        List<Reservation> filtered;
        
        if (showActiveOnlyCheckBox.isSelected()) {
            filtered = allReservations.stream()
                    .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
                    .collect(Collectors.toList());
            statusLabel.setText("Showing active reservations only");
        } else {
            filtered = allReservations;
            statusLabel.setText("Showing all reservations");
        }

        updateTable(filtered);
    }

    /**
     * Updates the table with the provided list of reservations.
     * 
     * @param reservations list of reservations to display
     */
    private void updateTable(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            reservationsTable.setItems(FXCollections.observableArrayList());
            reservationsTable.setVisible(false);
            emptyLabel.setVisible(true);
            return;
        }

        ObservableList<Reservation> observableList = FXCollections.observableArrayList(reservations);
        reservationsTable.setItems(observableList);
        reservationsTable.setVisible(true);
        emptyLabel.setVisible(false);
    }

    /**
     * Formats reservation status for display.
     * 
     * @param status reservation status enum
     * @return formatted status string
     */
    private String formatStatus(Reservation.ReservationStatus status) {
        if (status == null) {
            return "UNKNOWN";
        }
        
        return switch (status) {
            case ACTIVE -> "Active";
            case CANCELLED -> "Cancelled";
            case COMPLETED -> "Completed";
            case NO_SHOW -> "No Show";
        };
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title dialog title
     * @param message error message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a success alert dialog.
     * 
     * @param title dialog title
     * @param message success message
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
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

        Platform.runLater(() -> {
            String command = message.getCommand();

            // Handle reservation list response
            if (Commands.GET_USER_RESERVATIONS.equals(command)) {
                if (!message.isSuccess()) {
                    statusLabel.setText("Error: " + message.getError());
                    emptyLabel.setVisible(true);
                    reservationsTable.setVisible(false);
                    return;
                }

                Object data = message.getData();
                
                if (!(data instanceof List<?>)) {
                    statusLabel.setText("Invalid data received from server");
                    return;
                }

                @SuppressWarnings("unchecked")
                List<Reservation> reservations = (List<Reservation>) data;
                
                allReservations = reservations;
                
                if (reservations.isEmpty()) {
                    statusLabel.setText("You have no reservations yet");
                    emptyLabel.setVisible(true);
                    reservationsTable.setVisible(false);
                } else {
                    filterReservations();
                }
            }
            
            // Handle cancellation response
            else if (Commands.CANCEL_RESERVATION.equals(command)) {
                if (message.isSuccess()) {
                    statusLabel.setText("Reservation cancelled successfully!");
                    showSuccessAlert("Success", "Your reservation has been cancelled.");
                    
                    // Reload reservations to update the table
                    loadReservations();
                } else {
                    statusLabel.setText("Cancellation failed: " + message.getError());
                    showErrorAlert("Cancellation Failed", message.getError());
                }
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