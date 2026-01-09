package slotsgui;

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

public class AvailableSlotsController implements MessageListener {

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> dinersSpinner;
    @FXML private Button checkBtn;
    @FXML private Button backBtn;
    @FXML private Label resultLabel;
    @FXML private ListView<String> slotsList;

    private ClientController clientController;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public void init(ClientController controller) {
        this.clientController = controller;
        this.clientController.setListener(this);

        datePicker.setValue(LocalDate.now().plusDays(1));
        dinersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        resultLabel.setText("");
        slotsList.getItems().clear();
        checkBtn.setDisable(false);
    }

    @FXML
    public void onCheckAvailability() {
        if (clientController == null || !clientController.isConnected()) {
            resultLabel.setText("Not connected to server.");
            return;
        }

        try {
            LocalDate date = datePicker.getValue();
            int diners = dinersSpinner.getValue();

            resultLabel.setText("Request sent...");
            slotsList.getItems().clear();

            clientController.requestAvailableSlots(date, diners);

        } catch (IOException e) {
            resultLabel.setText("Failed to send request: " + e.getMessage());
        }
    }

    @FXML
    public void onBack() {
        try {
            ConnectApp.showHome();
        } catch (Exception e) {
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Object msg) {
        Platform.runLater(() -> handleServerMessage(msg));
    }

    private void handleServerMessage(Object msg) {
        if (msg instanceof String s && s.startsWith("DISCONNECTED:")) {
            resultLabel.setText("Disconnected from server");
            slotsList.getItems().clear();
            checkBtn.setDisable(true);
            return;
        }

        if (!(msg instanceof Message response)) {
            resultLabel.setText("Unexpected response type");
            return;
        }

        if (!"GET_AVAILABLE_SLOTS".equals(response.getCommand())) {
            return; 
        }

        if (!response.isSuccess()) {
            resultLabel.setText("Error: " + response.getError());
            return;
        }

        Object data = response.getData();
        if (!(data instanceof List<?> list)) {
            resultLabel.setText("Invalid data returned");
            return;
        }

        List<String> times = list.stream()
                .filter(o -> o instanceof LocalDateTime)
                .map(o -> ((LocalDateTime) o).toLocalTime().format(timeFmt))
                .toList();

        slotsList.setItems(FXCollections.observableArrayList(times));

        resultLabel.setText(times.isEmpty() ? "No available slots found" : "Found " + times.size() + " slots");
    }
}
