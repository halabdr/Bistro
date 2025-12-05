package gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

import client.BistroClient;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class BistroClientUIController {

    @FXML
    private TextArea ordersArea;

    @FXML
    private TextField orderNumberField;

    @FXML
    private TextField dateField;

    @FXML
    private TextField guestsField;

    private PrintWriter out;
    private BufferedReader in;

    @FXML
    private void initialize() {
        // ניקח את החיבור מ-BistroClient
        out = BistroClient.getOut();
        in  = BistroClient.getIn();
    }

    @FXML
    private void handleLoadOrders() {
        ordersArea.clear();

        try {
            if (out == null || in == null) {
                showError("Not connected to server");
                return;
            }

            out.println("GET_ORDERS");

            String line;
            while ((line = in.readLine()) != null && !"END".equals(line)) {
                ordersArea.appendText(formatOrderLine(line) + "\n");
            }

        } catch (IOException e) {
            showError("Failed to load orders: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateOrder() {
        String orderStr  = orderNumberField.getText().trim();
        String dateStr   = dateField.getText().trim();
        String guestsStr = guestsField.getText().trim();

        if (orderStr.isEmpty()) {
            showError("Order number is required");
            return;
        }
        if (dateStr.isEmpty() && guestsStr.isEmpty()) {
            showError("Please fill at least one field to update");
            return;
        }

        int orderNumber;
        try {
            orderNumber = Integer.parseInt(orderStr);
        } catch (NumberFormatException e) {
            showError("Order number must be an integer");
            return;
        }

        LocalDate date = null;
        Integer guests = null;

        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                showError("Date must be in format YYYY-MM-DD");
                return;
            }
        }

        if (!guestsStr.isEmpty()) {
            try {
                guests = Integer.parseInt(guestsStr);
            } catch (NumberFormatException e) {
                showError("Guests must be an integer");
                return;
            }
        }

        try {
            if (out == null || in == null) {
                showError("Not connected to server");
                return;
            }

            String datePart   = (date   == null) ? "" : date.toString();
            String guestsPart = (guests == null) ? "" : guests.toString();

            String command = "UPDATE;" + orderNumber + ";" + datePart + ";" + guestsPart;
            out.println(command);

            String response = in.readLine();
            if ("OK".equals(response)) {
                showInfo("Order updated successfully");
            } else {
                showError("Server error: " + response);
            }

        } catch (Exception e) {
            showError("Failed to update order: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        ordersArea.clear();
        orderNumberField.clear();
        dateField.clear();
        guestsField.clear();
    }

    private String formatOrderLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return line;
        }
        String subscriber = parts[4].isEmpty() ? "-" : parts[4];

        return String.format(
            "===========%n" +
            "     |Order #%s| %n" +
            "===========%n" +
            "Date: %s%n" +
            "Guests: %s%n" +
            "Confirmation code: %s%n" +
            "Subscriber ID: %s%n" +
            "Placed: %s%n" +
            "------------------------------------------------------------------------",
            parts[0], parts[1], parts[2], parts[3], subscriber, parts[5]
        );
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}