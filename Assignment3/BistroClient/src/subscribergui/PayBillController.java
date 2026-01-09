package subscribergui;

import client.ClientController;
import client.commands;
import clientgui.ConnectApp;
import common.Message;
import entities.Bill;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PayBillController {

    @FXML private TextField tableField;
    @FXML private TextField subscriberField;
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setMessageListener(this::onMessage);
        statusLabel.setText("");
        resultLabel.setText("");
    }

    @FXML
    private void onPay() {
        try {
            String tableTxt = tableField.getText() == null ? "" : tableField.getText().trim();
            if (tableTxt.isEmpty()) {
                statusLabel.setText("Please enter table number.");
                return;
            }
            int tableNumber = Integer.parseInt(tableTxt);

            String sn = subscriberField.getText() == null ? "" : subscriberField.getText().trim();

            resultLabel.setText("");
            statusLabel.setText("Paying...");
            controller.payBill(tableNumber, sn);
        } catch (NumberFormatException e) {
            statusLabel.setText("Table number must be a number.");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void onMessage(Message m) {
        if (m == null || !commands.PAY_BILL.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                statusLabel.setText("Failed: " + m.getError());
                return;
            }

            statusLabel.setText("Payment completed.");

            Object data = m.getData();

            // If server returns Bill entity (recommended)
            if (data instanceof Bill b) {
                resultLabel.setText(
                        "Bill #" + b.getBillNumber()
                                + "\nTotal: " + b.getTotalPrice()
                                + "\nDiscount: " + b.getDiscountValue()
                                + "\nFinal: " + b.getFinalAmount()
                                + "\nPayment date: " + b.getPaymentDate()
                                + "\nTable: " + b.getTableNumber()
                                + "\nSubscriber: " + b.getSubscriberNumber()
                );
                return;
            }

            // Otherwise show whatever server returns
            resultLabel.setText("Result: " + String.valueOf(data));
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showCustomerMenu();
    }
}
