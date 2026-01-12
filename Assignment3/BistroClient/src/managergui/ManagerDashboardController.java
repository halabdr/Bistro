package managergui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import common.Message;
import entities.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManagerDashboardController implements MessageListener {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    @FXML private ComboBox<String> monthCombo;
    @FXML private TextField yearField;

    @FXML private Button loadBtn;
    @FXML private Button generateBtn;
    @FXML private Button exportBtn;

    @FXML private LineChart<String, Number> timeChart;
    @FXML private BarChart<String, Number> subscribersChart;

    @FXML private TableView<Row> detailsTable;
    @FXML private TableColumn<Row, String> fieldCol;
    @FXML private TableColumn<Row, String> valueCol;

    private ClientController controller;
    private User managerUser;

    public void init(ClientController controller, User managerUser) {
        this.controller = controller;
        this.managerUser = managerUser;

        controller.setListener(this);

        welcomeLabel.setText("Manager Dashboard - " + managerUser.getName());

        // months UI
        monthCombo.setItems(FXCollections.observableArrayList(
                "January","February","March","April","May","June",
                "July","August","September","October","November","December"
        ));
        monthCombo.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
        yearField.setText(String.valueOf(LocalDate.now().getYear()));

        // table
        fieldCol.setCellValueFactory(new PropertyValueFactory<>("field"));
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        detailsTable.setItems(FXCollections.observableArrayList());

        // optional: ask server for list of available months (once server supports it)
        try {
            controller.getMonthlyReportsList();
        } catch (IOException ignored) {
            // server might not support yet; not fatal
        }
    }

    @FXML
    private void onLoadReports() {
        Integer year = parseYear();
        Integer month = parseMonth();
        if (year == null || month == null) return;

        statusLabel.setText("Loading reports...");

        try {
            controller.getTimeReport(year, month);
            controller.getSubscribersReport(year, month);
        } catch (IOException e) {
            statusLabel.setText("Failed to request reports: " + e.getMessage());
        }
    }

    @FXML
    private void onGenerateReports() {
        Integer year = parseYear();
        Integer month = parseMonth();
        if (year == null || month == null) return;

        statusLabel.setText("Generating reports...");

        try {
            controller.generateReports(year, month);
        } catch (IOException e) {
            statusLabel.setText("Failed to generate reports: " + e.getMessage());
        }
    }

    @FXML
    private void onExport() {
        // Bonus step later (CSV/JSON). For now:
        statusLabel.setText("Export: not implemented yet (bonus).");
    }

    @Override
    public void onMessage(Message msg) {
        Platform.runLater(() -> handleMessageOnFx(msg));
    }

    private void handleMessageOnFx(Message msg) {
        String cmd = msg.getCommand();
        Object data = msg.getData();

        switch (cmd) {
            case Commands.GET_TIME_REPORT:
                statusLabel.setText("Time report received.");
                showAsKeyValue("Time Report", data);
                // chart rendering will be added when we define proper ReportData DTO
                break;

            case Commands.GET_SUBSCRIBERS_REPORT:
                statusLabel.setText("Subscribers report received.");
                showAsKeyValue("Subscribers Report", data);
                break;

            case Commands.GET_MONTHLY_REPORTS_LIST:
                // When server returns list, we can populate month/year choices.
                // For now we just show a summary:
                statusLabel.setText("Monthly reports list received.");
                showAsKeyValue("Reports List", data);
                break;

            case Commands.GENERATE_REPORTS:
                statusLabel.setText("Generate reports result received.");
                showAsKeyValue("Generate Reports", data);
                // optional: auto reload after generate
                onLoadReports();
                break;

            default:
                // ignore other messages
                break;
        }
    }

    private void showAsKeyValue(String title, Object data) {
        ObservableList<Row> rows = FXCollections.observableArrayList();
        rows.add(new Row("Section", title));

        Map<String, String> map = toFlatMap(data);
        if (map.isEmpty()) {
            rows.add(new Row("Data", String.valueOf(data)));
        } else {
            map.forEach((k, v) -> rows.add(new Row(k, v)));
        }
        detailsTable.setItems(rows);
    }

    private Map<String, String> toFlatMap(Object data) {
        Map<String, String> out = new LinkedHashMap<>();
        if (data instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                out.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
            }
        }
        return out;
    }

    private Integer parseYear() {
        try {
            int y = Integer.parseInt(yearField.getText().trim());
            if (y < 2000 || y > 2100) {
                statusLabel.setText("Invalid year.");
                return null;
            }
            return y;
        } catch (Exception e) {
            statusLabel.setText("Year must be a number.");
            return null;
        }
    }

    private Integer parseMonth() {
        int idx = monthCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            statusLabel.setText("Choose a month.");
            return null;
        }
        return idx + 1;
    }

    public static class Row {
        private final String field;
        private final String value;

        public Row(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public String getField() { return field; }
        public String getValue() { return value; }
    }
}