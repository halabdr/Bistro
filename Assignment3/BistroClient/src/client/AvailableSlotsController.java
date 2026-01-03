package client;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import common.AvailableSlot;
import common.CommandType;
import common.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AvailableSlotsController implements MessageListener {

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> dinersSpinner;
    @FXML private Button checkBtn;
    @FXML private Button backBtn;
    @FXML private Label resultLabel;
    @FXML private ListView<String> slotsList;

    private ClientController clientController;

    public void setClientController(ClientController controller) {
        this.clientController = controller;

        this.clientController.setListener(this);

        if (datePicker != null) datePicker.setValue(LocalDate.now());
        if (dinersSpinner != null) {
            dinersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        }
    }

    @FXML
    public void initialize() {
        resultLabel.setText("");
    }

    @FXML
    public void onCheckAvailability() {
        try {
            LocalDate date = datePicker.getValue();
            int diners = dinersSpinner.getValue();

            resultLabel.setText("Request sent...");
            slotsList.getItems().clear();

            clientController.requestAvailableSlots(date, diners);

        } catch (IOException e) {
            resultLabel.setText("Failed to send request: " + e.getMessage());
        } catch (Exception e) {
            resultLabel.setText("Invalid input: " + e.getMessage());
        }
    }

    @FXML
    public void onBack() {
        try {
            ConnectApp.showConnect();
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
            return;
        }

        if (!(msg instanceof Message)) {
            resultLabel.setText("Unexpected response type");
            return;
        }

        Message response = (Message) msg;

        if (response.getCommand() != CommandType.GET_AVAILABLE_SLOTS) {
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

        slotsList.setItems(FXCollections.observableArrayList(
                list.stream()
                    .map(o -> (o instanceof AvailableSlot slot)
                            ? (slot.toString())
                            : String.valueOf(o))
                    .toList()
        ));

        resultLabel.setText("Found " + list.size() + " slots");
    }
}