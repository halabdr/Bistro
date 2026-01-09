package reservationgui;

import client.ClientController;
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

public class ReservationSearchController implements MessageListener {

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> dinersSpinner;
    @FXML private Button checkBtn;
    @FXML private Button backBtn;
    @FXML private Label resultLabel;
    @FXML private ListView<String> timesList;

    private ClientController controller;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        datePicker.setValue(LocalDate.now().plusDays(1));
        dinersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));

        resultLabel.setText("");
        timesList.getItems().clear();

        timesList.setOnMouseClicked(e -> {
            String hhmm = timesList.getSelectionModel().getSelectedItem();
            if (hhmm == null) return;

            try {
                ConnectApp.showCreateReservation(datePicker.getValue(), hhmm, dinersSpinner.getValue());
            } catch (Exception ex) {
                resultLabel.setText("Navigation error: " + ex.getMessage());
            }
        });
    }

    @FXML
    public void onCheck() {
        try {
            resultLabel.setText("Loading...");
            timesList.getItems().clear();
            controller.requestAvailableSlots(datePicker.getValue(), dinersSpinner.getValue());
        } catch (IOException e) {
            resultLabel.setText("Failed: " + e.getMessage());
        }
    }

    @FXML
    public void onBack() {
        try {
            ConnectApp.showCustomerMenu();
        } catch (Exception e) {
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Object msg) {
        Platform.runLater(() -> {
            if (!(msg instanceof Message m)) return;
            if (!"GET_AVAILABLE_SLOTS".equals(m.getCommand())) return;

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
            resultLabel.setText(times.isEmpty() ? "No available times" : "Choose a time");
        });
    }
}
