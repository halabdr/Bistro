package subscribergui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller for paying a bill.
 * According to requirements:
 * - Payment is done using the confirmation code (from reservation or waitlist)
 * - Subscribers get a 10% discount
 * - Table is released immediately after payment
 */
public class PayBillController implements MessageListener {

    @FXML private TextField confirmationCodeField;
    @FXML private VBox billDetailsBox;
    @FXML private Label tableNumberLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label discountLabel;
    @FXML private Label finalAmountLabel;
    @FXML private Label statusLabel;
    @FXML private Button fetchBillButton;
    @FXML private Button payButton;

    private ClientController controller;
    private Subscriber subscriber;
    private String currentConfirmationCode;
    private boolean billFetched = false;

    // Business-rule message (not a system error)
    private static final String MSG_NOT_SEATED =
            "Payment is only available after you've been seated";

    private enum StatusType { SUCCESS, ERROR, INFO }

    /**
     * Initializes the controller with the shared ClientController.
     *
     * @param controller shared client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        clearForm();
    }

    /**
     * Initializes the controller with subscriber information.
     *
     * @param controller shared client controller
     * @param subscriber logged-in subscriber
     */
    public void init(ClientController controller, Subscriber subscriber) {
        this.controller = controller;
        this.subscriber = subscriber;
        this.controller.setListener(this);
        clearForm();
    }

    /**
     * Clears all form fields and resets state.
     */
    private void clearForm() {
        statusLabel.setText("");
        billDetailsBox.setVisible(false);
        billDetailsBox.setManaged(false);
        payButton.setDisable(true);
        billFetched = false;
        currentConfirmationCode = null;
        // Optional: re-enable if user returns to this screen
        fetchBillButton.setDisable(false);
        confirmationCodeField.setDisable(false);
    }

    /**
     * Fetches the bill details for the entered confirmation code.
     */
    @FXML
    private void onFetchBill() {
        String code = confirmationCodeField.getText();
        if (code == null || code.trim().isEmpty()) {
            setStatus("Please enter your confirmation code.", StatusType.ERROR);
            return;
        }

        currentConfirmationCode = code.trim();
        setStatus("Fetching bill details...", StatusType.INFO);

        try {
            controller.getBillByCode(currentConfirmationCode);
        } catch (IOException e) {
            setStatus("Failed to fetch bill: " + e.getMessage(), StatusType.ERROR);
        }
    }

    /**
     * Processes the payment for the current bill.
     */
    @FXML
    private void onPay() {
        if (!billFetched || currentConfirmationCode == null) {
            setStatus("Please fetch the bill details first.", StatusType.ERROR);
            return;
        }

        setStatus("Processing payment...", StatusType.INFO);

        try {
            controller.payBillByCode(currentConfirmationCode);
        } catch (IOException e) {
            setStatus("Payment failed: " + e.getMessage(), StatusType.ERROR);
        }
    }

    /**
     * Handles server responses.
     *
     * @param m message received from server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        Platform.runLater(() -> {
            String command = m.getCommand();

            // Handle GET_BILL response
            if (Commands.GET_BILL.equals(command)) {
                handleGetBillResponse(m);
            }
            // Handle PAY_BILL response
            else if (Commands.PAY_BILL.equals(command)) {
                handlePayBillResponse(m);
            }
        });
    }

    /**
     * Handles the response from fetching bill details.
     */
    private void handleGetBillResponse(Message m) {
        if (!m.isSuccess()) {
            String err = m.getError() != null ? m.getError() : "Unknown error";

            // Business rule: not seated yet -> show as Notice/Info
            if (err.toLowerCase().contains(MSG_NOT_SEATED.toLowerCase())) {
                setStatus("Notice: " + err, StatusType.INFO);
            } else {
                setStatus("Error: " + err, StatusType.ERROR);
            }

            billDetailsBox.setVisible(false);
            billDetailsBox.setManaged(false);
            payButton.setDisable(true);
            billFetched = false;
            return;
        }

        Object data = m.getData();
        if (!(data instanceof Map<?, ?>)) {
            setStatus("Invalid response from server.", StatusType.ERROR);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> billData = (Map<String, Object>) data;

        // Display bill details
        Object tableNum = billData.get("tableNumber");
        Object total = billData.get("totalPrice");
        Object discount = billData.get("discountValue");
        Object finalAmount = billData.get("finalAmount");

        tableNumberLabel.setText(tableNum != null ? String.valueOf(tableNum) : "-");
        totalAmountLabel.setText(formatCurrency(total));
        discountLabel.setText("-" + formatCurrency(discount));
        finalAmountLabel.setText(formatCurrency(finalAmount));

        billDetailsBox.setVisible(true);
        billDetailsBox.setManaged(true);
        payButton.setDisable(false);
        billFetched = true;

        setStatus("Bill loaded. Click 'Pay Now' to complete payment.", StatusType.SUCCESS);
    }

    /**
     * Handles the response from paying the bill.
     */
    private void handlePayBillResponse(Message m) {
        if (!m.isSuccess()) {
            String err = m.getError() != null ? m.getError() : "Unknown error";
            setStatus("Payment failed: " + err, StatusType.ERROR);
            return;
        }

        setStatus("Payment completed successfully!", StatusType.SUCCESS);
        payButton.setDisable(true);
        fetchBillButton.setDisable(true);
        confirmationCodeField.setDisable(true);

        // Show success alert
        showSuccessAlert(
                "Payment Successful",
                "Your bill has been paid successfully.\nThe table has been released.\nThank you for dining with us!"
        );
    }

    /**
     * Formats a number as currency.
     */
    private String formatCurrency(Object value) {
        if (value == null) return "₪0.00";

        try {
            if (value instanceof BigDecimal) {
                return String.format("₪%.2f", ((BigDecimal) value).doubleValue());
            } else if (value instanceof Number) {
                return String.format("₪%.2f", ((Number) value).doubleValue());
            } else {
                return "₪" + value.toString();
            }
        } catch (Exception e) {
            return "₪" + value.toString();
        }
    }

    /**
     * Sets the status message with styling based on type.
     */
    private void setStatus(String message, StatusType type) {
        statusLabel.setText(message);

        switch (type) {
            case ERROR:
                statusLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 13px;");
                break;
            case INFO:
                statusLabel.setStyle("-fx-text-fill: #2C5F7C; -fx-font-size: 13px;");
                break;
            default: // SUCCESS
                statusLabel.setStyle("-fx-text-fill: #38A169; -fx-font-size: 13px;");
                break;
        }
    }

    /**
     * Shows a success alert dialog.
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Navigates back to the subscriber menu.
     */
    @FXML
    private void onBack() throws Exception {
        if (subscriber != null) {
            ConnectApp.showSubscriberMenu(subscriber);
        } else {
            ConnectApp.showCustomerMenu();
        }
    }
}