package staffgui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.OpeningHours;
import entities.Reservation;
import entities.SpecialHours;
import entities.Table;
import entities.User;
import entities.WaitlistEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class StaffDashboardController implements MessageListener {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

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

    private ClientController controller;
    private User staffUser;

    public void init(ClientController controller, User staffUser) {
        this.controller = controller;
        this.staffUser = staffUser;
        this.controller.setListener(this);

        welcomeLabel.setText("Staff Dashboard - " + staffUser.getName() + " (" + staffUser.getUserRole() + ")");

        setupColumns();

        // initial load
        refreshReservations();
        refreshWaitlist();
        refreshTables();
        refreshOpeningHours();
        refreshSpecialHours();
    }

    private void setupColumns() {
        // Reservations
        resCodeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getConfirmationCode()));
        resDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBookingDate())));
        resTimeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBookingTime())));
        resGuestsCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getGuestCount()));
        resSubCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSubscriberNumber()));

        // Waitlist
        wlCodeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEntryCode()));
        wlGuestsCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNumberOfDiners()));
        wlSubCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSubscriberNumber()));

        // Tables
        tNumCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTableNumber()));
        tCapCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSeatCapacity()));
        tLocCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTableLocation()));
        tStatusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTableStatus()));

        // Hours
        hDayCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDayOfWeek()));
        hOpenCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOpeningTime()));
        hCloseCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getClosingTime()));

        // Special hours
        sIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSpecialId()));
        sDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDate()));
        sOpenCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOpeningTime()));
        sCloseCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getClosingTime()));
    }

    // ---------------- Navigation ----------------

    @FXML
    private void onLogout() {
        try {
            ConnectApp.showHome();
        } catch (Exception e) {
            showError("Logout failed: " + e.getMessage());
        }
    }

    // ---------------- Refresh actions ----------------

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

    // ---------------- Tables CRUD ----------------

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
        g.setHgap(10); g.setVgap(10);
        g.addRow(0, new Label("Number:"), num);
        g.addRow(1, new Label("Seats:"), cap);
        g.addRow(2, new Label("Location:"), loc);
        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> {
            if (btn == ok) {
                return new TableInput(num.getText().trim(), cap.getText().trim(), loc.getText().trim());
            }
            return null;
        });

        Optional<TableInput> r = d.showAndWait();
        r.ifPresent(in -> {
            try {
                int n = Integer.parseInt(in.number);
                int c = Integer.parseInt(in.capacity);
                controller.addTable(n, c, in.location);
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
        g.setHgap(10); g.setVgap(10);
        g.addRow(0, new Label("Table #:"), new Label(String.valueOf(sel.getTableNumber())));
        g.addRow(1, new Label("Seats:"), cap);
        g.addRow(2, new Label("Location:"), loc);
        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> {
            if (btn == ok) return new TableInput(String.valueOf(sel.getTableNumber()), cap.getText().trim(), loc.getText().trim());
            return null;
        });

        d.showAndWait().ifPresent(in -> {
            try {
                sel.setSeatCapacity(Integer.parseInt(in.capacity));
                sel.setTableLocation(in.location);
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

    private static class TableInput {
        final String number;
        final String capacity;
        final String location;
        TableInput(String number, String capacity, String location) {
            this.number = number; this.capacity = capacity; this.location = location;
        }
    }

    // ---------------- Opening hours update ----------------

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

    // ---------------- Special hours CRUD ----------------

    @FXML
    private void onAddSpecialHours() {
        TextInputDialog dateD = new TextInputDialog("2026-01-01");
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
            s.setSpecialDate(java.time.LocalDate.parse(dateOpt.get().trim()));
            s.setOpeningTime(LocalTime.parse(openOpt.get().trim()));
            s.setClosingTime(LocalTime.parse(closeOpt.get().trim()));
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
        a.setContentText("Delete special-hours ID " + sel.getSpecialId() + "?");
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    controller.deleteSpecialHours(sel.getSpecialId());
                    status("Deleting special hours...");
                } catch (Exception e) {
                    showError("Delete failed: " + e.getMessage());
                }
            }
        });
    }

    // ---------------- Server responses ----------------

    @Override
    public void onMessage(Message m) {
        Platform.runLater(() -> {
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
                default -> status("OK: " + m.getCommand());
            }
        });
    }

    private void status(String s) { statusLabel.setText(s); }
    private void showError(String s) { statusLabel.setText(s); }
}
