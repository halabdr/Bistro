package staffgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import common.Message;
import entities.MonthlyReport;
import entities.OpeningHours;
import entities.Reservation;
import entities.SpecialHours;
import entities.Subscriber;
import entities.Table;
import entities.User;
import entities.WaitlistEntry;
import entities.User.UserRole;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;

/**
 * Controller for the Staff Dashboard screen.
 * Supports staff actions: viewing reservations/waitlist, managing tables,
 * updating opening/special hours, viewing reports, viewing subscribers, and registering subscribers.
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
    
 // Current Diners
    @FXML private TableView<Map<String, Object>> dinersTable;
    @FXML private TableColumn<Map<String, Object>, Number> dTableCol;
    @FXML private TableColumn<Map<String, Object>, Number> dGuestsCol;
    @FXML private TableColumn<Map<String, Object>, String> dLocationCol;
    @FXML private TableColumn<Map<String, Object>, String> dCodeCol;
    @FXML private TableColumn<Map<String, Object>, String> dCustomerCol;
    @FXML private TableColumn<Map<String, Object>, String> dSeatedAtCol;

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

    // View Subscribers
    @FXML private TextField searchSubscriberField;
    @FXML private TableView<Subscriber> subscribersTable;
    @FXML private TableColumn<Subscriber, String> subNumCol;
    @FXML private TableColumn<Subscriber, String> subNameCol;
    @FXML private TableColumn<Subscriber, String> subEmailCol;
    @FXML private TableColumn<Subscriber, String> subPhoneCol;
    @FXML private TableColumn<Subscriber, String> subQRCol;
    
    // Subscriber Details
    @FXML private VBox subscriberDetailsBox;
    @FXML private Label detailSubNumber;
    @FXML private Label detailName;
    @FXML private Label detailEmail;
    @FXML private Label detailPhone;
    @FXML private Label detailQR;
    @FXML private Label detailRegDate;
    
    // Subscriber History
    @FXML private TableView<Reservation> subscriberHistoryTable;
    @FXML private TableColumn<Reservation, String> histCodeCol;
    @FXML private TableColumn<Reservation, String> histDateCol;
    @FXML private TableColumn<Reservation, String> histTimeCol;
    @FXML private TableColumn<Reservation, Number> histGuestsCol;
    @FXML private TableColumn<Reservation, String> histStatusCol;
    @FXML private TableColumn<Reservation, String> histTableCol;
    @FXML private Label historyStatusLabel;

    // Reports
    @FXML private ComboBox<String> reportMonthCombo;
    @FXML private TextField reportYearField;
    @FXML private TableView<ReportRow> reportTable;
    @FXML private TableColumn<ReportRow, String> repCol1;
    @FXML private TableColumn<ReportRow, String> repCol2;
 // Report Charts
    @FXML private HBox chartsArea;
    @FXML private PieChart reportPieChart;
    @FXML private BarChart<String, Number> reportBarChart;

    // Register Subscriber
    @FXML private TextField regNameField;
    @FXML private TextField regEmailField;
    @FXML private TextField regPhoneField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private VBox registrationResultBox;
    @FXML private Label resultSubscriberNumber;
    @FXML private Label resultQRCode;
    @FXML private Label qrCodeDisplay;
    @FXML private Label regStatusLabel;

    private String activeReportCommand;
    private ClientController controller;
    private User staffUser;

    // Store generated codes for display after successful registration
    private String pendingSubscriberNumber;
    private String pendingQRCode;
    
    // Currently selected subscriber for history view
    private Subscriber selectedSubscriber;

    /**
     * Report table row (field-value pair).
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

        this.controller.setListener(this);

        if (welcomeLabel != null && staffUser != null) {
            welcomeLabel.setText("Staff Dashboard - " + staffUser.getName() + " (" + staffUser.getUserRole() + ")");
        }

        setupColumns();
        setupReportsUI();
        setupSubscribersTableListener();

        refreshReservations();
        refreshWaitlist();
        refreshCurrentDiners();
        refreshTables();
        refreshOpeningHours();
        refreshSpecialHours();
        
     // Hide Reports tab for non-managers
        if (staffUser != null && staffUser.getUserRole() != User.UserRole.MANAGER) {
            tabs.getTabs().removeIf(tab -> "Reports".equals(tab.getText()));
        }
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

        // Subscribers List
        if (subNumCol != null) {
            subNumCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getSubscriberNumber()));
        }
        if (subNameCol != null) {
            subNameCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        }
        if (subEmailCol != null) {
            subEmailCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getEmailAddress()));
        }
        if (subPhoneCol != null) {
            subPhoneCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getPhoneNumber()));
        }
        if (subQRCol != null) {
            subQRCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getMembershipCard()));
        }

        // Subscriber History
        if (histCodeCol != null) {
            histCodeCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getConfirmationCode()));
        }
        if (histDateCol != null) {
            histDateCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBookingDate())));
        }
        if (histTimeCol != null) {
            histTimeCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBookingTime())));
        }
        if (histGuestsCol != null) {
            histGuestsCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleIntegerProperty(c.getValue().getGuestCount()));
        }
        if (histStatusCol != null) {
            histStatusCol.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(
                            c.getValue().getStatus() != null ? c.getValue().getStatus().name() : ""));
        }
        if (histTableCol != null) {
            histTableCol.setCellValueFactory(c -> {
                Integer tableNum = c.getValue().getAssignedTableNumber();
                String display = (tableNum != null && tableNum > 0) ? String.valueOf(tableNum) : "-";
                return new javafx.beans.property.SimpleStringProperty(display);
            });
        }

        // Reports table
        if (repCol1 != null && repCol2 != null) {
            repCol1.setCellValueFactory(new PropertyValueFactory<>("field"));
            repCol2.setCellValueFactory(new PropertyValueFactory<>("value"));
        }
        
     // Current Diners
        if (dTableCol != null) {
            dTableCol.setCellValueFactory(c -> {
                Object val = c.getValue().get("tableNumber");
                return new javafx.beans.property.SimpleIntegerProperty(val != null ? (Integer) val : 0);
            });
        }
        if (dGuestsCol != null) {
            dGuestsCol.setCellValueFactory(c -> {
                Object val = c.getValue().get("guestCount");
                return new javafx.beans.property.SimpleIntegerProperty(val != null ? (Integer) val : 0);
            });
        }
        if (dLocationCol != null) {
            dLocationCol.setCellValueFactory(c -> {
                Object val = c.getValue().get("tableLocation");
                return new javafx.beans.property.SimpleStringProperty(val != null ? val.toString() : "");
            });
        }
        if (dCodeCol != null) {
            dCodeCol.setCellValueFactory(c -> {
                Object val = c.getValue().get("confirmationCode");
                return new javafx.beans.property.SimpleStringProperty(val != null ? val.toString() : "");
            });
        }
        if (dCustomerCol != null) {
            dCustomerCol.setCellValueFactory(c -> {
                Map<String, Object> row = c.getValue();
                String name = (String) row.get("subscriberName");
                if (name != null && !name.isEmpty()) {
                    return new javafx.beans.property.SimpleStringProperty(name + " (Member)");
                }
                String phone = (String) row.get("walkInPhone");
                if (phone != null && !phone.isEmpty()) {
                    return new javafx.beans.property.SimpleStringProperty(phone + " (Walk-in)");
                }
                String email = (String) row.get("walkInEmail");
                if (email != null && !email.isEmpty()) {
                    return new javafx.beans.property.SimpleStringProperty(email + " (Walk-in)");
                }
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            });
        }
        if (dSeatedAtCol != null) {
            dSeatedAtCol.setCellValueFactory(c -> {
                Object val = c.getValue().get("seatedAt");
                if (val != null && !val.toString().isEmpty()) {
                    String dt = val.toString().replace("T", " ");
                    if (dt.length() > 16) dt = dt.substring(0, 16);
                    return new javafx.beans.property.SimpleStringProperty(dt);
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
        }
    }

    /**
     * Sets up listener for subscriber selection in table.
     */
    private void setupSubscribersTableListener() {
        if (subscribersTable != null) {
            subscribersTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            onSubscriberSelected(newSelection);
                        }
                    });
        }
    }

    /**
     * Sets up the reports UI with month/year selection.
     */
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
            clientgui.ConnectApp.showWelcome();
        } catch (Exception e) {
            showError("Logout failed: " + e.getMessage());
        }
    }

    // Refresh Actions

    @FXML
    public void refreshReservations() {
        try {
            status("Loading reservations...");
            controller.getAllReservations();
        } catch (Exception e) {
            showError("Failed to load reservations: " + e.getMessage());
        }
    }

    @FXML
    public void refreshWaitlist() {
        try {
            status("Loading waitlist...");
            controller.getWaitlist();
        } catch (Exception e) {
            showError("Failed to load waitlist: " + e.getMessage());
        }
    }
    
    @FXML
    public void refreshCurrentDiners() {
        try {
            status("Loading current diners...");
            controller.getCurrentDiners();
        } catch (Exception e) {
            showError("Failed to load current diners: " + e.getMessage());
        }
    }

    @FXML
    public void refreshTables() {
        try {
            status("Loading tables...");
            controller.getTables();
        } catch (Exception e) {
            showError("Failed to load tables: " + e.getMessage());
        }
    }

    @FXML
    public void refreshOpeningHours() {
        try {
            status("Loading opening hours...");
            controller.getOpeningHours();
        } catch (Exception e) {
            showError("Failed to load opening hours: " + e.getMessage());
        }
    }

    @FXML
    public void refreshSpecialHours() {
        try {
            status("Loading special hours...");
            controller.getSpecialHours();
        } catch (Exception e) {
            showError("Failed to load special hours: " + e.getMessage());
        }
    }

    // View Subscribers Actions

    /**
     * Searches for a specific subscriber by number.
     */
    @FXML
    private void onSearchSubscriber() {
        String searchTerm = searchSubscriberField != null ? searchSubscriberField.getText().trim() : "";
        
        if (searchTerm.isEmpty()) {
            showError("Please enter a subscriber number to search");
            return;
        }

        try {
            status("Searching for subscriber...");
            controller.getSubscriberByNumber(searchTerm);
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    /**
     * Shows all subscribers.
     */
    @FXML
    private void onShowAllSubscribers() {
        try {
            status("Loading all subscribers...");
            controller.getAllSubscribers();
        } catch (Exception e) {
            showError("Failed to load subscribers: " + e.getMessage());
        }
    }

    /**
     * Refreshes the subscribers list.
     */
    @FXML
    private void onRefreshSubscribers() {
        onShowAllSubscribers();
    }

    /**
     * Handles subscriber selection - shows details and loads history.
     */
    private void onSubscriberSelected(Subscriber subscriber) {
        selectedSubscriber = subscriber;
        
        // Show details panel
        if (subscriberDetailsBox != null) {
            subscriberDetailsBox.setVisible(true);
            subscriberDetailsBox.setManaged(true);
        }

        // Fill in details
        if (detailSubNumber != null) detailSubNumber.setText(subscriber.getSubscriberNumber());
        if (detailName != null) detailName.setText(subscriber.getName());
        if (detailEmail != null) detailEmail.setText(subscriber.getEmailAddress());
        if (detailPhone != null) detailPhone.setText(subscriber.getPhoneNumber());
        if (detailQR != null) detailQR.setText(subscriber.getMembershipCard());
        if (detailRegDate != null && subscriber.getRegistrationDate() != null) {
            detailRegDate.setText(subscriber.getRegistrationDate().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        // Load reservation history
        loadSubscriberHistory(subscriber.getSubscriberNumber());
    }

    /**
     * Loads reservation history for selected subscriber.
     */
    private void loadSubscriberHistory(String subscriberNumber) {
        try {
            if (historyStatusLabel != null) {
                historyStatusLabel.setText("Loading history...");
            }
            controller.getUserReservations(subscriberNumber);
        } catch (Exception e) {
            if (historyStatusLabel != null) {
                historyStatusLabel.setText("Failed to load history: " + e.getMessage());
            }
        }
    }

    // Reports Actions

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

    // Table Actions

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

    // Opening Hours Actions

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

    // Special Hours Actions

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

    // Register Subscriber Actions

    /**
     * Handles Register Subscriber button click.
     */
    @FXML
    private void onRegisterSubscriber() {
        if (regStatusLabel != null) {
            regStatusLabel.setText("");
        }
        hideRegistrationResult();

        String name = regNameField != null ? regNameField.getText().trim() : "";
        String email = regEmailField != null ? regEmailField.getText().trim() : "";
        String phone = regPhoneField != null ? regPhoneField.getText().trim() : "";
        String password = regPasswordField != null ? regPasswordField.getText() : "";
        String confirmPassword = regConfirmPasswordField != null ? regConfirmPasswordField.getText() : "";

        if (name.isEmpty()) {
            showRegError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            showRegError("Email is required");
            return;
        }
        if (!isValidEmail(email)) {
            showRegError("Please enter a valid email address");
            return;
        }
        if (phone.isEmpty()) {
            showRegError("Phone number is required");
            return;
        }
        if (!isValidPhone(phone)) {
            showRegError("Please enter a valid phone number (10 digits starting with 05)");
            return;
        }
        if (password.isEmpty()) {
            showRegError("Password is required");
            return;
        }
        if (password.length() < 6) {
            showRegError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showRegError("Passwords do not match");
            return;
        }

        pendingSubscriberNumber = generateSubscriberNumber();
        pendingQRCode = generateQRCode();

        try {
            status("Registering subscriber...");
            controller.registerSubscriber(name, email, phone, password, pendingSubscriberNumber, pendingQRCode);
        } catch (Exception e) {
            showRegError("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Clears the registration form.
     */
    @FXML
    private void onClearRegistration() {
        if (regNameField != null) regNameField.clear();
        if (regEmailField != null) regEmailField.clear();
        if (regPhoneField != null) regPhoneField.clear();
        if (regPasswordField != null) regPasswordField.clear();
        if (regConfirmPasswordField != null) regConfirmPasswordField.clear();
        if (regStatusLabel != null) regStatusLabel.setText("");
        hideRegistrationResult();
    }

    private String generateSubscriberNumber() {
        return "SUB" + System.currentTimeMillis() % 100000;
    }

    private String generateQRCode() {
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^05\\d{8}$");
    }

    private void showRegError(String message) {
        if (regStatusLabel != null) {
            regStatusLabel.setText(message);
            regStatusLabel.setStyle("-fx-text-fill: #E53E3E;");
        }
    }

    private void showRegistrationResult(String subscriberNumber, String qrCode) {
        if (registrationResultBox != null) {
            registrationResultBox.setVisible(true);
            registrationResultBox.setManaged(true);
        }
        if (resultSubscriberNumber != null) {
            resultSubscriberNumber.setText(subscriberNumber);
        }
        if (resultQRCode != null) {
            resultQRCode.setText(qrCode);
        }
        if (qrCodeDisplay != null) {
            qrCodeDisplay.setText(qrCode);
        }
    }

    private void hideRegistrationResult() {
        if (registrationResultBox != null) {
            registrationResultBox.setVisible(false);
            registrationResultBox.setManaged(false);
        }
    }

    // Server Message Handler

    @Override
    public void onMessage(Message m) {
        Platform.runLater(() -> {
            if (m == null) {
                showError("Received null message from server.");
                return;
            }
            if (!m.isSuccess()) {
                if (Commands.REGISTER_SUBSCRIBER.equals(m.getCommand())) {
                    showRegError("Registration failed: " + m.getError());
                    return;
                }
                if (Commands.GET_SUBSCRIBER_BY_NUMBER.equals(m.getCommand())) {
                    showError("Subscriber not found: " + m.getError());
                    return;
                }
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
                
                case Commands.GET_CURRENT_DINERS -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = (List<Map<String, Object>>) m.getData();
                    if (dinersTable != null) {
                        dinersTable.setItems(FXCollections.observableArrayList(list));
                    }
                    status("Current diners loaded: " + list.size() + " table(s) occupied");
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

                case Commands.GET_ALL_SUBSCRIBERS -> {
                    @SuppressWarnings("unchecked")
                    List<Subscriber> list = (List<Subscriber>) m.getData();
                    if (subscribersTable != null) {
                        subscribersTable.setItems(FXCollections.observableArrayList(list));
                    }
                    status("Subscribers loaded: " + list.size());
                }

                case Commands.GET_SUBSCRIBER_BY_NUMBER -> {
                    Subscriber subscriber = (Subscriber) m.getData();
                    if (subscribersTable != null) {
                        subscribersTable.setItems(FXCollections.observableArrayList(subscriber));
                        subscribersTable.getSelectionModel().select(0);
                    }
                    status("Subscriber found: " + subscriber.getName());
                }

                case Commands.GET_USER_RESERVATIONS -> {
                    @SuppressWarnings("unchecked")
                    List<Reservation> list = (List<Reservation>) m.getData();
                    if (subscriberHistoryTable != null) {
                        subscriberHistoryTable.setItems(FXCollections.observableArrayList(list));
                    }
                    if (historyStatusLabel != null) {
                        historyStatusLabel.setText("History loaded: " + list.size() + " reservation(s)");
                    }
                }

                case Commands.REGISTER_SUBSCRIBER -> {
                    status("Subscriber registered successfully!");
                    
                    // Show alert with subscriber details
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registration Successful");
                    alert.setHeaderText("New Subscriber Registered Successfully!");
                    alert.setContentText(
                        "Subscriber Number: " + pendingSubscriberNumber + "\n\n" +
                        "Membership Card: " + pendingQRCode + "\n\n" +
                        "Please provide these details to the customer."
                    );
                    alert.showAndWait();
                    
                    // Clear form fields
                    if (regNameField != null) regNameField.clear();
                    if (regEmailField != null) regEmailField.clear();
                    if (regPhoneField != null) regPhoneField.clear();
                    if (regPasswordField != null) regPasswordField.clear();
                    if (regConfirmPasswordField != null) regConfirmPasswordField.clear();
                    
                    if (regStatusLabel != null) {
                        regStatusLabel.setText("Registration successful!");
                        regStatusLabel.setStyle("-fx-text-fill: #38A169;");
                    }
                }

                case Commands.GET_TIME_REPORT -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = (List<Map<String, Object>>) m.getData();
                    displayTimeReportWithCharts(list);
                    status("Time report loaded");
                }
                case Commands.GET_SUBSCRIBERS_REPORT -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = (List<Map<String, Object>>) m.getData();
                    displaySubscribersReportWithCharts(list);
                    status("Subscribers report loaded");
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
    
    /**
     * Displays the Time Report with charts.
     */
    private void displayTimeReportWithCharts(List<Map<String, Object>> data) {
        // Update table
        if (reportTable != null) {
        	List<ReportRow> rows = new ArrayList<>();
        	for (Map<String, Object> item : data) {
        	    String field = item.get("field") != null ? item.get("field").toString() : "";
        	    String value = item.get("value") != null ? item.get("value").toString() : "";
        	    rows.add(new ReportRow(field, value));
        	}
        	reportTable.setItems(FXCollections.observableArrayList(rows));
        }

        // Clear previous charts
        if (reportPieChart != null) {
            reportPieChart.getData().clear();
            reportPieChart.setTitle("Reservation Status");
        }
        if (reportBarChart != null) {
            reportBarChart.getData().clear();
            reportBarChart.setTitle("Reservations by Time Slot");
        }

        // Extract data for charts
        int completed = 0, noShow = 0, cancelled = 0, active = 0;
        int morning = 0, afternoon = 0, evening = 0;

        for (Map<String, Object> row : data) {
            String field = (String) row.get("field");
            String value = (String) row.get("value");
            
            if (field == null || value == null || value.isEmpty()) continue;
            
            try {
                switch (field) {
                    case "Completed (Checked In)" -> completed = Integer.parseInt(value);
                    case "No-Shows" -> noShow = Integer.parseInt(value);
                    case "Cancelled" -> cancelled = Integer.parseInt(value);
                    case "Active (Pending)" -> active = Integer.parseInt(value);
                    case "Morning (before 12:00)" -> morning = Integer.parseInt(value);
                    case "Afternoon (12:00-17:00)" -> afternoon = Integer.parseInt(value);
                    case "Evening (after 17:00)" -> evening = Integer.parseInt(value);
                }
            } catch (NumberFormatException ignored) {}
        }

        // Pie Chart - Reservation Status
        if (reportPieChart != null) {
            if (completed > 0) reportPieChart.getData().add(new PieChart.Data("Completed (" + completed + ")", completed));
            if (noShow > 0) reportPieChart.getData().add(new PieChart.Data("No-Show (" + noShow + ")", noShow));
            if (cancelled > 0) reportPieChart.getData().add(new PieChart.Data("Cancelled (" + cancelled + ")", cancelled));
            if (active > 0) reportPieChart.getData().add(new PieChart.Data("Active (" + active + ")", active));
        }

        // Bar Chart - Time Slots
        if (reportBarChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Reservations");
            series.getData().add(new XYChart.Data<>("Morning", morning));
            series.getData().add(new XYChart.Data<>("Afternoon", afternoon));
            series.getData().add(new XYChart.Data<>("Evening", evening));
            reportBarChart.getData().add(series);
        }
    }

    /**
     * Displays the Subscribers Report with charts.
     */
    private void displaySubscribersReportWithCharts(List<Map<String, Object>> data) {
        // Update table
        if (reportTable != null) {
            List<ReportRow> rows = new ArrayList<>();
            for (Map<String, Object> item : data) {
                String field = item.get("field") != null ? item.get("field").toString() : "";
                String value = item.get("value") != null ? item.get("value").toString() : "";
                rows.add(new ReportRow(field, value));
            }
            reportTable.setItems(FXCollections.observableArrayList(rows));
        }

        // Clear previous charts
        if (reportPieChart != null) {
            reportPieChart.getData().clear();
            reportPieChart.setTitle("Reservations: Subscribers vs Walk-ins");
        }
        if (reportBarChart != null) {
            reportBarChart.getData().clear();
            reportBarChart.setTitle("Subscriber Activity");
        }

        // Extract data for charts
        int subRes = 0, walkInRes = 0;
        int totalSubs = 0, activeSubs = 0;
        int wlSub = 0;

        for (Map<String, Object> row : data) {
            String field = (String) row.get("field");
            String value = (String) row.get("value");
            
            if (field == null || value == null || value.isEmpty()) continue;
            
            try {
                // Remove non-numeric suffixes
                String numValue = value.replaceAll("[^0-9]", "");
                if (numValue.isEmpty()) continue;
                
                switch (field) {
                    case "Total Subscribers" -> totalSubs = Integer.parseInt(numValue);
                    case "Active Subscribers This Month" -> activeSubs = Integer.parseInt(numValue);
                    case "Subscriber Reservations" -> subRes = Integer.parseInt(numValue);
                    case "Walk-in Reservations" -> walkInRes = Integer.parseInt(numValue);
                    case "Subscriber Waitlist Entries" -> wlSub = Integer.parseInt(numValue);
                }
            } catch (NumberFormatException ignored) {}
        }

        // Pie Chart - Subscriber vs Walk-in
        if (reportPieChart != null) {
            if (subRes > 0) reportPieChart.getData().add(new PieChart.Data("Subscribers (" + subRes + ")", subRes));
            if (walkInRes > 0) reportPieChart.getData().add(new PieChart.Data("Walk-ins (" + walkInRes + ")", walkInRes));
        }

        // Bar Chart - Activity comparison
        if (reportBarChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Count");
            series.getData().add(new XYChart.Data<>("Total Subscribers", totalSubs));
            series.getData().add(new XYChart.Data<>("Active This Month", activeSubs));
            series.getData().add(new XYChart.Data<>("Waitlist Joins", wlSub));
            reportBarChart.getData().add(series);
        }
    }
}