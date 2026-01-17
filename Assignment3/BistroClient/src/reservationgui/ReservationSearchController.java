package reservationgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for searching available reservation slots.
 * Sends GET_AVAILABLE_SLOTS and displays the returned times.
 * If no slots available, suggests alternative dates/times.
 */
public class ReservationSearchController implements MessageListener {

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> dinersSpinner;
    @FXML private Label resultLabel;
    @FXML private ListView<String> timesList;
    @FXML private Button continueBtn;

    private ClientController controller;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private Subscriber subscriber;
    
    // Track if we're showing alternatives (to handle date selection differently)
    private boolean showingAlternatives = false;
    private LocalDate selectedAlternativeDate = null;
    private String selectedAlternativeTime = null;

    public void init(ClientController controller, Subscriber subscriber) {
        this.subscriber = subscriber;
        init(controller);
    }

    /**
     * Initializes this screen with a connected ClientController.
     *
     * @param controller connected client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        datePicker.setValue(LocalDate.now().plusDays(1));
        dinersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));

        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;

                LocalDate today = LocalDate.now();
                LocalDate max = today.plusMonths(1);

                setDisable(item.isBefore(today) || item.isAfter(max));
            }
        });

        resultLabel.setText("");
        timesList.getItems().clear();

        if (continueBtn != null) continueBtn.setDisable(true);

        timesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (continueBtn != null) {
                continueBtn.setDisable(newV == null || newV.isBlank());
            }
            
            // If showing alternatives, parse the selected date/time
            if (showingAlternatives && newV != null && !newV.isBlank()) {
                parseAlternativeSelection(newV);
            }
        });
    }

    /**
     * Parses alternative selection to extract date and time.
     */
    private void parseAlternativeSelection(String selection) {
        try {
            // Format: "2026-01-20 14:30"
            LocalDateTime dt = LocalDateTime.parse(selection, dateTimeFmt);
            selectedAlternativeDate = dt.toLocalDate();
            selectedAlternativeTime = dt.toLocalTime().format(timeFmt);
        } catch (Exception e) {
            // If parsing fails, treat as regular time
            selectedAlternativeDate = null;
            selectedAlternativeTime = null;
        }
    }

    /**
     * Sends a GET_AVAILABLE_SLOTS request to the server.
     */
    @FXML
    public void onCheck() {
        try {
            // Reset UI before loading
            resultLabel.setText("Loading...");
            timesList.getItems().clear();
            if (continueBtn != null) continueBtn.setDisable(true);
            showingAlternatives = false;
            selectedAlternativeDate = null;
            selectedAlternativeTime = null;
            
            // Simple validation
            if (datePicker.getValue() == null) {
                resultLabel.setText("Please choose a date.");
                return;
            }
            
            LocalDate d = datePicker.getValue();
            LocalDate today = LocalDate.now();
            LocalDate max = today.plusMonths(1);

            if (d.isBefore(today) || d.isAfter(max)) {
                resultLabel.setText("You can book from today up to one month ahead.");
                return;
            }

            controller.getAvailableSlots(datePicker.getValue(), dinersSpinner.getValue());
        } catch (IOException e) {
            resultLabel.setText("Failed: " + e.getMessage());
        }
    }

    /**
     * Continue to reservation creation (after choosing time).
     */
    @FXML
    public void onContinue() {
        String selection = timesList.getSelectionModel().getSelectedItem();
        if (selection == null || selection.isBlank()) {
            resultLabel.setText("Please choose a time first.");
            return;
        }

        try {
            LocalDate dateToUse;
            String timeToUse;
            
            if (showingAlternatives && selectedAlternativeDate != null && selectedAlternativeTime != null) {
                // Using alternative date/time
                dateToUse = selectedAlternativeDate;
                timeToUse = selectedAlternativeTime;
            } else {
                // Using original date with selected time
                dateToUse = datePicker.getValue();
                timeToUse = selection;
            }
            
            ConnectApp.showCreateReservation(subscriber, dateToUse, timeToUse, dinersSpinner.getValue());
        } catch (Exception ex) {
            resultLabel.setText("Navigation error: " + ex.getMessage());
        }
    }

    @FXML
    public void onBack() {
        try {
            if (subscriber != null) {
                ConnectApp.showSubscriberMenu(subscriber);
            } else {
                ConnectApp.showWalkInMenu();
            }
        } catch (Exception e) {
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        
        String command = m.getCommand();
        
        // Handle available slots response
        if (Commands.GET_AVAILABLE_SLOTS.equals(command)) {
            handleAvailableSlotsResponse(m);
            return;
        }
        
        // Handle alternative slots response
        if (Commands.GET_ALTERNATIVE_SLOTS.equals(command)) {
            handleAlternativeSlotsResponse(m);
            return;
        }
    }

    /**
     * Handles the response for GET_AVAILABLE_SLOTS.
     */
    private void handleAvailableSlotsResponse(Message m) {
        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                resultLabel.setText("Error: " + m.getError());
                return;
            }

            Object data = m.getData();
            if (!(data instanceof List<?> list)) {
                resultLabel.setText("Invalid data");
                return;
            }

            LocalDateTime minAllowed = LocalDateTime.now().plusHours(1);
            LocalDate maxAllowedDate = LocalDate.now().plusMonths(1);

            List<String> times = list.stream()
                    .filter(o -> o instanceof LocalDateTime)
                    .map(o -> (LocalDateTime) o)                    
                    .filter(dt -> !dt.toLocalDate().isAfter(maxAllowedDate))
                    .filter(dt -> !dt.toLocalDate().isEqual(LocalDate.now()) || !dt.isBefore(minAllowed))
                    .map(dt -> dt.toLocalTime().format(timeFmt))
                    .toList();

            timesList.setItems(FXCollections.observableArrayList(times));
            showingAlternatives = false;

            if (times.isEmpty()) {
                resultLabel.setText("No available times for this date. Searching for alternatives...");
                if (continueBtn != null) continueBtn.setDisable(true);
                
                // Request alternative slots
                try {
                    controller.getAlternativeSlots(datePicker.getValue(), dinersSpinner.getValue());
                } catch (IOException e) {
                    resultLabel.setText("No available times. Failed to search alternatives: " + e.getMessage());
                }
            } else {
                resultLabel.setText("Choose a time, then press Continue");
            }
        });
    }

    /**
     * Handles the response for GET_ALTERNATIVE_SLOTS.
     */
    private void handleAlternativeSlotsResponse(Message m) {
        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                resultLabel.setText("No available times for this date and no alternatives found.");
                return;
            }

            Object data = m.getData();
            if (!(data instanceof List<?> list)) {
                resultLabel.setText("No available times for this date and no alternatives found.");
                return;
            }

            List<String> alternatives = list.stream()
                    .filter(o -> o instanceof LocalDateTime)
                    .map(o -> (LocalDateTime) o)
                    .map(dt -> dt.format(dateTimeFmt))
                    .toList();

            if (alternatives.isEmpty()) {
                resultLabel.setText("No available times for this date and no alternatives found in the next 7 days.");
                return;
            }

            timesList.setItems(FXCollections.observableArrayList(alternatives));
            showingAlternatives = true;
            
            resultLabel.setText("No times available for " + datePicker.getValue() + 
                              ". Here are " + alternatives.size() + " alternative date/times:");
        });
    }
}