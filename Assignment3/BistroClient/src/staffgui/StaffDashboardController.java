package staffgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import common.Message;
import entities.MonthlyReport;
import entities.OpeningHours;
import entities.Reservation;
import entities.SpecialHours;
import entities.Table;
import entities.User;
import entities.WaitlistEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Staff Dashboard screen.
 * This dashboard supports staff actions such as:
 * viewing reservations/waitlist, managing tables, and updating opening/special hours.
 */
public class StaffDashboardController implements MessageListener {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private TabPane tabs;

    // Reservations
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> resCodeCol;
    @FXML private TableColumn<Reservation, String> resDateCol;
    @FXML private TableColumn<Reservation, String> resTimeCol;
    @FXML private TableColumn<Reservation, Number> resGuestsCol;
    @FXML private TableColumn<Reservation, String> resSubCol;

    // Waitlist
    @FXML private TableView<WaitlistEntry> waitlistTable;
    @FXML private TableColumn<WaitlistEntry, String> wlCodeCol;
    @FXML private TableColumn<WaitlistEntry, Number> wlGuestsCol;
    @FXML private TableColumn<WaitlistEntry, String> wlSubCol;

    // Tables
    @FXML private TableView<Table> tablesTable;
    @FXML private TableColumn<Table, Number> tNumCol;
    @FXML private TableColumn<Table, Number> tCapCol;
    @FXML private TableColumn<Table, String> tLocCol;
    @FXML private TableColumn<Table, Object> tStatusCol;

    // Opening hours
    @FXML private TableView<OpeningHours> hoursTable;
    @FXML private TableColumn<OpeningHours, Object> hDayCol;
    @FXML private TableColumn<OpeningHours, Object> hOpenCol;
    @FXML private TableColumn<OpeningHours, Object> hCloseCol;

    // Special hours
    @FXML private TableView<SpecialHours> specialTable;
    @FXML private TableColumn<SpecialHours, Number> sIdCol;
    @FXML private TableColumn<SpecialHours, Object> sDateCol;
    @FXML private TableColumn<SpecialHours, Object> sOpenCol;
    @FXML private TableColumn<SpecialHours, Object> sCloseCol;

    // ---------------- Reports ----------------
    @FXML private ComboBox<String> reportMonthCombo;
    @FXML private TextField reportYearField;

    @FXML private TableView<ReportRow> reportTable;
    @FXML private TableColumn<ReportRow, String> repCol1;
    @FXML private TableColumn<ReportRow, String> repCol2;

    private String activeReportCommand; // כדי לדעת איזה דוח חזר (אופציונלי לשימוש)

    private ClientController controller;
    private User staffUser;

    /**
     * Report table row (field-value).
     */
    public static class ReportRow {
        private final String field;
        private final String value;

        public ReportRow(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public String getField() { return field; }
        public String getValue() { return value; }
    }

    /**
     * Initializes the dashboard after successful staff login.
     *
     * @param controller connected client controller
     * @param staffUser logged-in staff user
     */
    public void init(ClientController controller, User staffUser) {
        this.controller = controller;
        this.staffUser = staffUser;

        // Register for server responses
        this.controller.setListener(this);

        if (welcomeLabel != null && staffUser != null) {
            welcomeLabel.setText("Staff Dashboard - " + staffUser.getName() + " (" + staffUser.getUserRole() + ")");
        }

        setupColumns();
        setupReportsUI(); // NEW

        refreshReservations();
        refreshWaitlist();
        refreshTables();
        refreshOpeningHours();
        refreshSpecialHours();
    }

    /**
     * Maps entity fields to TableView columns.
     */
    private void setupColumns() {
        // Reservations
        if (resCodeCol != null) {
            resCodeCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getConfirmationCode()));
        }
        if (resDateCol != null) {
            resDateCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBookingDate())));
        }
        if (resTimeCol != null) {
            resTimeCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBookingTime())));
        }
        if (resGuestsCol != null) {
            resGuestsCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleIntegerProperty(c.getValue().getGuestCount()));
        }
        if (resSubCol != null) {
            resSubCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getSubscriberNumber()));
        }

        // Waitlist
        if (wlCodeCol != null) {
            wlCodeCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getEntryCode()));
        }
        if (wlGuestsCol != null) {
            wlGuestsCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNumberOfDiners()));
        }
        if (wlSubCol != null) {
            wlSubCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getSubscriberNumber()));
        }

        // Tables
        if (tNumCol != null) {
            tNumCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTableNumber()));
        }
        if (tCapCol != null) {
            tCapCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSeatCapacity()));
        }
        if (tLocCol != null) {
            tLocCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getTableLocation()));
        }
        if (tStatusCol != null) {
            tStatusCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTableStatus()));
        }

        // OpeningHours
        if (hDayCol != null) {
            hDayCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getWeekday()));
        }
        if (hOpenCol != null) {
            hOpenCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOpeningTime()));
        }
        if (hCloseCol != null) {
            hCloseCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getClosingTime()));
        }

        // SpecialHours
        if (sIdCol != null) {
            sIdCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSpecialId()));
        }
        if (sDateCol != null) {
            sDateCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getSpecialDate()));
        }
        if (sOpenCol != null) {
            sOpenCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOpeningTime()));
        }
        if (sCloseCol != null) {
            sCloseCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getClosingTime()));
        }

        // Reports table (NEW)
        if (repCol1 != null && repCol2 != null) {
            repCol1.setCellValueFactory(new PropertyValueFactory<>("field"));
            repCol2.setCellValueFactory(new PropertyValueFactory<>("value"));
        }
    }

    // NEW
    private void setupReportsUI() {
        if (reportMonthCombo == null || reportYearField == null) return;

        ObservableList<String> months = FXCollections.observableArrayList(
                "JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE",
                "JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"
        );
        reportMonthCombo.setItems(months);
        reportMonthCombo.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
        reportYearField.setText(String.valueOf(LocalDate.now().getYear()));
    }

    /**
     * Returns to the home screen.
     */
    @FXML
    private void onLogout() {
        try {
            clientgui.ConnectApp.showHome();
        } catch (Exception e) {
            showError("Logout failed: " + e.getMessage());
        }
    }

    /** Requests all reservations from the server. */
    @FXML
    public void refreshReservations() {
        try {
            status("Loading reservations...");
            controller.getAllReservations();
        } catch (Exception e) {
            showError("Failed to load reservations: " + e.getMessage());
        }
    }

    /** Requests waitlist entries from the server. */
    @FXML
    public void refreshWaitlist() {
        try {
            status("Loading waitlist...");
            controller.getWaitlist();
        } catch (Exception e) {
            showError("Failed to load waitlist: " + e.getMessage());
        }
    }

    /** Requests the list of tables from the server. */
    @FXML
    public void refreshTables() {
        try {
            status("Loading tables...");
            controller.getTables();
        } catch (Exception e) {
            showError("Failed to load tables: " + e.getMessage());
        }
    }

    /** Requests weekly opening hours from the server. */
    @FXML
    public void refreshOpeningHours() {
        try {
            status("Loading opening hours...");
            controller.getOpeningHours();
        } catch (Exception e) {
            showError("Failed to load opening hours: " + e.getMessage());
        }
    }

    /** Requests special hours list from the server. */
    @FXML
    public void refreshSpecialHours() {
        try {
            status("Loading special hours...");
            controller.getSpecialHours();
        } catch (Exception e) {
            showError("Failed to load special hours: " + e.getMessage());
        }
    }

    // ---------------- Reports actions ----------------

    @FXML
    private void onFetchNotificationLog() {
        fetchReport(Commands.GET_NOTIFICATION_LOG);
    }

    @FXML
    private void onFetchTimeReport() {
        fetchReport(Commands.GET_TIME_REPORT);
    }

    @FXML
    private void onFetchSubscribersReport() {
        fetchReport(Commands.GET_SUBSCRIBERS_REPORT);
    }

    private void fetchReport(String command) {
        try {
            if (reportYearField == null || reportMonthCombo == null) {
                showError("Reports UI not loaded (missing FXML ids).");
                return;
            }

            int year = Integer.parseInt(reportYearField.getText().trim());
            String monthName = reportMonthCombo.getValue();
            if (monthName == null || monthName.isBlank()) {
                showError("Choose month first.");
                return;
            }
            int month = Month.valueOf(monthName).getValue();

            activeReportCommand = command;
            status("Loading report...");

            switch (command) {
                case Commands.GET_NOTIFICATION_LOG -> controller.getNotificationLogReport(year, month);
                case Commands.GET_TIME_REPORT -> controller.getTimeReport(year, month);
                case Commands.GET_SUBSCRIBERS_REPORT -> controller.getSubscribersReport(year, month);
                default -> throw new IllegalStateException("Unknown report command: " + command);
            }

        } catch (NumberFormatException e) {
            showError("Year must be a number (e.g., 2026).");
        } catch (Exception e) {
            showError("Failed to request report: " + e.getMessage());
        }
    }

    // ---------------- Table actions ----------------

    @FXML
    private void onAddTable() {
        Dialog<TableInput> d = new Dialog<>();
        d.setTitle("Add Table");

        ButtonType ok = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField num = new TextField();
        num.setPromptText("Table number");

        TextField cap = new TextField();
        cap.setPromptText("Seat capacity");

        TextField loc = new TextField();
        loc.setPromptText("Location");

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.addRow(0, new Label("Number:"), num);
        g.addRow(1, new Label("Seats:"), cap);
        g.addRow(2, new Label("Location:"), loc);

        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> (btn == ok)
                ? new TableInput(num.getText().trim(), cap.getText().trim(), loc.getText().trim())
                : null);

        d.showAndWait().ifPresent(in -> {
            try {
                int n = Integer.parseInt(in.number());
                int c = Integer.parseInt(in.capacity());
                controller.addTable(n, c, in.location());
                status("Adding table...");
            } catch (Exception e) {
                showError("Add failed: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onUpdateTable() {
        Table sel = tablesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Select a table first.");
            return;
        }

        Dialog<TableInput> d = new Dialog<>();
        d.setTitle("Update Table");

        ButtonType ok = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField cap = new TextField(String.valueOf(sel.getSeatCapacity()));
        TextField loc = new TextField(sel.getTableLocation());

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.addRow(0, new Label("Table #:"), new Label(String.valueOf(sel.getTableNumber())));
        g.addRow(1, new Label("Seats:"), cap);
        g.addRow(2, new Label("Location:"), loc);

        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> (btn == ok)
                ? new TableInput(String.valueOf(sel.getTableNumber()), cap.getText().trim(), loc.getText().trim())
                : null);

        d.showAndWait().ifPresent(in -> {
            try {
                sel.setSeatCapacity(Integer.parseInt(in.capacity()));
                sel.setTableLocation(in.location());
                controller.updateTable(sel);
                status("Updating table...");
            } catch (Exception e) {
                showError("Update failed: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteTable() {
        Table sel = tablesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Select a table first.");
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Delete Table");
        a.setHeaderText(null);
        a.setContentText("Delete table #" + sel.getTableNumber() + "?");

        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    controller.deleteTable(sel.getTableNumber());
                    status("Deleting table...");
                } catch (Exception e) {
                    showError("Delete failed: " + e.getMessage());
                }
            }
        });
    }

    private record TableInput(String number, String capacity, String location) { }

    @FXML
    private void onUpdateOpeningHours() {
        OpeningHours sel = hoursTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Select a day first.");
            return;
        }

        TextInputDialog openD = new TextInputDialog(String.valueOf(sel.getOpeningTime()));
        openD.setTitle("Update Opening Hours");
        openD.setHeaderText(null);
        openD.setContentText("Open time (HH:mm):");
        Optional<String> openOpt = openD.showAndWait();
        if (openOpt.isEmpty()) return;

        TextInputDialog closeD = new TextInputDialog(String.valueOf(sel.getClosingTime()));
        closeD.setTitle("Update Opening Hours");
        closeD.setHeaderText(null);
        closeD.setContentText("Close time (HH:mm):");
        Optional<String> closeOpt = closeD.showAndWait();
        if (closeOpt.isEmpty()) return;

        try {
            sel.setOpeningTime(LocalTime.parse(openOpt.get().trim()));
            sel.setClosingTime(LocalTime.parse(closeOpt.get().trim()));
            controller.updateOpeningHours(sel);
            status("Updating opening hours...");
        } catch (Exception e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void onAddSpecialHours() {
        TextInputDialog dateD = new TextInputDialog(LocalDate.now().toString());
        dateD.setTitle("Add Special Hours");
        dateD.setHeaderText(null);
        dateD.setContentText("Date (YYYY-MM-DD):");
        Optional<String> dateOpt = dateD.showAndWait();
        if (dateOpt.isEmpty()) return;

        TextInputDialog openD = new TextInputDialog("10:00");
        openD.setTitle("Add Special Hours");
        openD.setHeaderText(null);
        openD.setContentText("Open time (HH:mm):");
        Optional<String> openOpt = openD.showAndWait();
        if (openOpt.isEmpty()) return;

        TextInputDialog closeD = new TextInputDialog("22:00");
        closeD.setTitle("Add Special Hours");
        closeD.setHeaderText(null);
        closeD.setContentText("Close time (HH:mm):");
        Optional<String> closeOpt = closeD.showAndWait();
        if (closeOpt.isEmpty()) return;

        try {
            SpecialHours s = new SpecialHours();
            s.setSpecialDate(LocalDate.parse(dateOpt.get().trim()));
            s.setOpeningTime(LocalTime.parse(openOpt.get().trim()));
            s.setClosingTime(LocalTime.parse(closeOpt.get().trim()));
            s.setClosedFlag(false);

            controller.addSpecialHours(s);
            status("Adding special hours...");
        } catch (Exception e) {
            showError("Add failed: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteSpecialHours() {
        SpecialHours sel = specialTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Select a special-hours row first.");
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Delete Special Hours");
        a.setHeaderText(null);
        a.setContentText("Delete special-hours for date " + sel.getSpecialDate() + "?");

        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    controller.deleteSpecialHours(sel.getSpecialDate());
                    status("Deleting special hours...");
                } catch (Exception e) {
                    showError("Delete failed: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Handles server responses and updates the UI.
     */
    @Override
    public void onMessage(Message m) {
        Platform.runLater(() -> {
            if (m == null) {
                showError("Received null message from server.");
                return;
            }
            if (!m.isSuccess()) {
                showError(m.getCommand() + " failed: " + m.getError());
                return;
            }

            switch (m.getCommand()) {

                case Commands.GET_RESERVATIONS -> {
                    @SuppressWarnings("unchecked")
                    List<Reservation> list = (List<Reservation>) m.getData();
                    reservationsTable.setItems(FXCollections.observableArrayList(list));
                    status("Reservations loaded: " + list.size());
                }

                case Commands.GET_WAITLIST -> {
                    @SuppressWarnings("unchecked")
                    List<WaitlistEntry> list = (List<WaitlistEntry>) m.getData();
                    waitlistTable.setItems(FXCollections.observableArrayList(list));
                    status("Waitlist loaded: " + list.size());
                }

                case Commands.GET_TABLES -> {
                    @SuppressWarnings("unchecked")
                    List<Table> list = (List<Table>) m.getData();
                    tablesTable.setItems(FXCollections.observableArrayList(list));
                    status("Tables loaded: " + list.size());
                }

                case Commands.GET_OPENING_HOURS -> {
                    @SuppressWarnings("unchecked")
                    List<OpeningHours> list = (List<OpeningHours>) m.getData();
                    hoursTable.setItems(FXCollections.observableArrayList(list));
                    status("Opening hours loaded: " + list.size());
                }

                case Commands.GET_SPECIAL_HOURS -> {
                    @SuppressWarnings("unchecked")
                    List<SpecialHours> list = (List<SpecialHours>) m.getData();
                    specialTable.setItems(FXCollections.observableArrayList(list));
                    status("Special hours loaded: " + list.size());
                }

                case Commands.ADD_TABLE, Commands.UPDATE_TABLE, Commands.DELETE_TABLE -> {
                    status("Tables updated.");
                    refreshTables();
                }

                case Commands.UPDATE_OPENING_HOURS -> {
                    status("Opening hours updated.");
                    refreshOpeningHours();
                }

                case Commands.ADD_SPECIAL_HOURS, Commands.DELETE_SPECIAL_HOURS -> {
                    status("Special hours updated.");
                    refreshSpecialHours();
                }

                // Reports (NEW)
                case Commands.GET_NOTIFICATION_LOG, Commands.GET_TIME_REPORT, Commands.GET_SUBSCRIBERS_REPORT -> {
                    Object data = m.getData();
                    List<ReportRow> rows = new ArrayList<>();

                    if (data == null) {
                        rows.add(new ReportRow("Result", "No data returned"));
                    } else if (data instanceof List<?> list) {
                        rows.add(new ReportRow("Items", String.valueOf(list.size())));
                        int i = 1;
                        for (Object item : list) {
                            rows.add(new ReportRow("Item " + (i++), String.valueOf(item)));
                        }
                    } else if (data instanceof java.util.Map<?, ?> map) {
                        rows.add(new ReportRow("Entries", String.valueOf(map.size())));
                        for (var e : map.entrySet()) {
                            rows.add(new ReportRow(String.valueOf(e.getKey()), String.valueOf(e.getValue())));
                        }
                    } else {
                        rows.add(new ReportRow("Result", String.valueOf(data)));
                    }

                    if (reportTable != null) {
                        reportTable.setItems(FXCollections.observableArrayList(rows));
                    }
                    status("Report loaded: " + m.getCommand());
                }

                default -> status("OK: " + m.getCommand());
            }
        });
    }

    private void status(String s) {
        if (statusLabel != null) statusLabel.setText(s);
    }

    private void showError(String s) {
        if (statusLabel != null) statusLabel.setText("Error: " + s);
    }
}