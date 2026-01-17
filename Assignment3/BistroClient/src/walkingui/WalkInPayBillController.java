package walkingui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller for walk-in customers to pay their bill.
 * Uses confirmation code for identification.
 * Note: Walk-in customers do NOT receive the 10% subscriber discount.
 */
public class WalkInPayBillController implements MessageListener {

    @FXML private TextField confirmationCodeField;
    @FXML private Label statusLabel;
    @FXML private VBox billDetailsBox;
    @FXML private Label tableNumberLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label finalAmountLabel;
    @FXML private Button fetchBillButton;
    @FXML private Button payButton;

    private ClientController controller;
    private String currentConfirmationCode;
    private boolean billFetched = false;

    /**
     * Initializes the controller.
     *
     * @param controller connected client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        clearForm();
    }

    /**
     * Clears the form and resets state.
     */
    private void clearForm() {
        statusLabel.setText("");
        billDetailsBox.setVisible(false);
        billDetailsBox.setManaged(false);
        payButton.setDisable(true);
        billFetched = false;
        currentConfirmationCode = null;
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
     * Navigates back to the walk-in menu.
     */
    @FXML
    private void onBack() {
        try {
            ConnectApp.showWalkInMenu();
        } catch (Exception e) {
            setStatus("Navigation error: " + e.getMessage(), StatusType.ERROR);
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;

        Platform.runLater(() -> {
            String command = m.getCommand();

            if (Commands.GET_BILL.equals(command)) {
                handleGetBillResponse(m);
            } else if (Commands.PAY_BILL.equals(command)) {
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

            // Check for "not seated yet" message
            if (err.toLowerCase().contains("not") && err.toLowerCase().contains("seated")) {
                setStatus("Notice: " + err, StatusType.NOTICE);
            } else {
                setStatus("Error: " + err, StatusType.ERROR);
            }

            billDetailsBox.setVisible(false);
            billDetailsBox.setManaged(false);
            payButton.setDisable(true);
            billFetched = false;
            return;
        }

        // Parse bill data
        Object data = m.getData();
        if (!(data instanceof Map)) {
            setStatus("Invalid response from server.", StatusType.ERROR);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> billData = (Map<String, Object>) data;

        // Extract values
        Integer tableNumber = (Integer) billData.get("tableNumber");
        BigDecimal totalPrice = toBigDecimal(billData.get("totalPrice"));
        BigDecimal finalAmount = toBigDecimal(billData.get("finalAmount"));

        // Update UI
        tableNumberLabel.setText(tableNumber != null ? String.valueOf(tableNumber) : "-");
        totalAmountLabel.setText(String.format("%.2f NIS", totalPrice));
        finalAmountLabel.setText(String.format("%.2f NIS", finalAmount));

        // Show bill details
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
            setStatus("Payment failed: " + m.getError(), StatusType.ERROR);
            return;
        }

        // Payment successful
        setStatus("Payment successful! Thank you for dining with us.", StatusType.SUCCESS);

        // Disable further actions
        payButton.setDisable(true);
        fetchBillButton.setDisable(true);
        confirmationCodeField.setDisable(true);
    }

    /**
     * Converts an object to BigDecimal.
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private enum StatusType { INFO, SUCCESS, ERROR, NOTICE }

    private void setStatus(String message, StatusType type) {
        statusLabel.setText(message);
        switch (type) {
            case SUCCESS:
                statusLabel.setStyle("-fx-text-fill: #38A169; -fx-font-size: 14px; -fx-font-weight: 600;");
                break;
            case ERROR:
                statusLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 14px; -fx-font-weight: 600;");
                break;
            case NOTICE:
                statusLabel.setStyle("-fx-text-fill: #D69E2E; -fx-font-size: 14px; -fx-font-weight: 500;");
                break;
            case INFO:
            default:
                statusLabel.setStyle("-fx-text-fill: #3182CE; -fx-font-size: 14px;");
                break;
        }
    }
}