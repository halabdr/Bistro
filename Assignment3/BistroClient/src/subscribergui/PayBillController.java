package subscribergui;

import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller for paying a bill.
 * Sends a command PAY_BILL request with code billNumber.
 * The server expects a map containing only:
 *   code billNumber
 * and returns a response map with fields:
 * code billNumber, totalPrice, discountValue, finalAmount, message.
 */
public class PayBillController implements MessageListener {

    /** Input field for the bill number. */
    @FXML private TextField billNumber;

    /** Label for status messages. */
    @FXML private Label statusLabel;

    /** Label for displaying payment details/result. */
    @FXML private Label resultLabel;

    /** Shared client controller. */
    private ClientController controller;

    /**
     * Initializes the controller with the shared ClientController
     * and registers this screen as the active message listener.
     *
     * @param controller shared client controller
     */
    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);
        statusLabel.setText("");
        resultLabel.setText("");
    }

    /**
     * Handles the Pay action.
     * Validates bill number and sends command PAY_BILL to the server.
     */
    @FXML
    private void onPay() {
        try {
            String billTxt = billNumber.getText() == null ? "" : billNumber.getText().trim();
            if (billTxt.isEmpty()) {
                statusLabel.setText("Please enter bill number.");
                return;
            }

            int billNumber = Integer.parseInt(billTxt);

            resultLabel.setText("");
            statusLabel.setText("Paying...");

            // Server expects only billNumber
            controller.payBill(billNumber);

        } catch (NumberFormatException e) {
            statusLabel.setText("Bill number must be a number.");
        } catch (IOException e) {
            statusLabel.setText("Failed: " + e.getMessage());
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Receives server messages.
     * Only handles responses for PAY_BILL.
     *
     * @param m message received from server
     */
    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.PAY_BILL.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Failed: " + m.getError());
                return;
            }

            statusLabel.setText("Payment completed.");

            Object data = m.getData();

            if (data instanceof Map<?, ?> map) {
                resultLabel.setText(formatPayBillResponse(map));
                return;
            }

            resultLabel.setText("Result: " + String.valueOf(data));
        });
    }

    /**
     * Formats the PAY_BILL response map returned by the server into readable text.
     *
     * @param map response data map
     * @return formatted string for UI display
     */
    private String formatPayBillResponse(Map<?, ?> map) {
        Object billNumber = map.get("billNumber");
        Object total = map.get("totalPrice");
        Object discount = map.get("discountValue");
        Object finalAmount = map.get("finalAmount");
        Object message = map.get("message");

        return "Bill #" + String.valueOf(billNumber)
                + "\nTotal: " + String.valueOf(total)
                + "\nDiscount: " + String.valueOf(discount)
                + "\nFinal: " + String.valueOf(finalAmount)
                + "\nMessage: " + String.valueOf(message);
    }

    /**
     * Navigates back to the customer menu.
     *
     * @throws Exception if navigation fails
     */
    @FXML
    private void onBack() throws Exception {
        ConnectApp.showCustomerMenu();
    }
}