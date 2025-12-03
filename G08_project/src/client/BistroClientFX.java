package client;

import java.awt.Button;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BistroClientFX extends Application {

    private TextArea ordersArea;
    private TextField orderNumberField;
    private TextField dateField;
    private TextField guestsField;

    private static final String HOST = "localhost";
    private static final int PORT = 5555;

    @Override
    public void start(Stage primaryStage) {
        // אזור להצגת ההזמנות
        ordersArea = new TextArea();
        ordersArea.setEditable(false);
        ordersArea.setPrefRowCount(15);

        // שדות קלט לעדכון
        orderNumberField = new TextField();
        dateField = new TextField();
        guestsField = new TextField();

        orderNumberField.setPromptText("Order number");
        dateField.setPromptText("YYYY-MM-DD");
        guestsField.setPromptText("Guests");

        // כפתורים
        Button loadBtn = new Button("Load orders");
        Button updateBtn = new Button("Update order");
        Button clearBtn = new Button("Clear");

        loadBtn.setOnAction(e -> loadOrders());
        updateBtn.setOnAction(e -> updateOrder());
        clearBtn.setOnAction(e -> ordersArea.clear());

        // חלק עליון – כפתורים כלליים
        HBox topButtons = new HBox(10, loadBtn, clearBtn);

        // טופס לעדכון הזמנה
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Order #:"), 0, 0);
        form.add(orderNumberField, 1, 0);
        form.add(new Label("New date:"), 0, 1);
        form.add(dateField, 1, 1);
        form.add(new Label("Guests:"), 0, 2);
        form.add(guestsField, 1, 2);
        form.add(updateBtn, 1, 3);

        // סידור כללי של המסך
        VBox root = new VBox(10, topButtons, ordersArea, form);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 700, 450);
        primaryStage.setTitle("Bistro Client (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // טעינת ההזמנות מהשרת
    private void loadOrders() {
        ordersArea.clear();

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("GET_ORDERS");

            String line;
            while ((line = in.readLine()) != null) {
                if ("END".equals(line)) {
                    break;
                }

                // פורמט השורה: orderNumber;orderDate;guests;conf;subscriberId;placingDate
                String[] parts = line.split(";");
                if (parts.length == 6) {
                    String row = String.format(
                            "Order #%s | Date: %s | Guests: %s | Conf: %s | Sub: %s | Placed: %s",
                            parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]
                    );
                    ordersArea.appendText(row + "\n");
                } else {
                    // במקרה שיש שורה לא בפורמט – נציג כמו שהיא
                    ordersArea.appendText(line + "\n");
                }
            }

            out.println("QUIT");

        } catch (Exception e) {
            ordersArea.appendText("ERROR: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    // עדכון הזמנה דרך השרת
    private void updateOrder() {
        String num = orderNumberField.getText().trim();
        String date = dateField.getText().trim();
        String guests = guestsField.getText().trim();

        if (num.isEmpty() || date.isEmpty() || guests.isEmpty()) {
            showAlert("Please fill all fields.");
            return;
        }

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String msg = "UPDATE;" + num + ";" + date + ";" + guests;
            out.println(msg);

            String response = in.readLine();
            if (response == null) {
                showAlert("No response from server.");
            } else if (response.startsWith("OK")) {
                showAlert("Order updated successfully.");
            } else {
                showAlert("Server response: " + response);
            }

            out.println("QUIT");

        } catch (Exception e) {
            showAlert("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}