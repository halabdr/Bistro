package reservationgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
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
 */
public class ReservationSearchController implements MessageListener {

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> dinersSpinner;
    @FXML private Label resultLabel;
    @FXML private ListView<String> timesList;

    @FXML private Button continueBtn;

    private ClientController controller;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Initializes this screen with a connected {@link ClientController}.
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
                setDisable(item.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        resultLabel.setText("");
        timesList.getItems().clear();

        if (continueBtn != null) continueBtn.setDisable(true);

        timesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (continueBtn != null) {
                continueBtn.setDisable(newV == null || newV.isBlank());
            }
        });
    }

    /**
     * Sends a GET_AVAILABLE_SLOTS request to the server.
     */
    @FXML
    public void onCheck() {
        try {
            // reset UI before loading
            resultLabel.setText("Loading...");
            timesList.getItems().clear();
            if (continueBtn != null) continueBtn.setDisable(true);

            // simple validation
            if (datePicker.getValue() == null) {
                resultLabel.setText("Please choose a date.");
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
        String hhmm = timesList.getSelectionModel().getSelectedItem();
        if (hhmm == null || hhmm.isBlank()) {
            resultLabel.setText("Please choose a time first.");
            return;
        }

        try {
            ConnectApp.showCreateReservation(datePicker.getValue(), hhmm, dinersSpinner.getValue());
        } catch (Exception ex) {
            resultLabel.setText("Navigation error: " + ex.getMessage());
        }
    }

    /**
     * Navigates back to the customer menu.
     */
    @FXML
    public void onBack() {
        try {
            ConnectApp.showCustomerMenu();
        } catch (Exception e) {
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.GET_AVAILABLE_SLOTS.equals(m.getCommand())) return;

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

            List<String> times = list.stream()
                    .filter(o -> o instanceof LocalDateTime)
                    .map(o -> ((LocalDateTime) o).toLocalTime().format(timeFmt))
                    .toList();

            timesList.setItems(FXCollections.observableArrayList(times));

            if (times.isEmpty()) {
                resultLabel.setText("No available times");
                if (continueBtn != null) continueBtn.setDisable(true);
            } else {
                resultLabel.setText("Choose a time, then press Continue");
            }
        });
    }
}
