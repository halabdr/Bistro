package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BistroClientFX extends Application {

    private TextArea ordersArea;
    private TextField orderNumberField;
    private TextField dateField;
    private TextField guestsField;

    private TextField hostField;

    private static final int PORT = 5555;

    @Override
    public void start(Stage primaryStage) {
        //Show orders
        ordersArea = new TextArea();
        ordersArea.setEditable(false);
        ordersArea.setPrefRowCount(15);

        orderNumberField = new TextField();
        dateField = new TextField();
        guestsField = new TextField();

        orderNumberField.setPromptText("Order number");
        dateField.setPromptText("YYYY-MM-DD");
        guestsField.setPromptText("Guests");


        hostField = new TextField("localhost");
        hostField.setPromptText("Server IP (e.g. 192.168.1.15)");
        hostField.setPrefColumnCount(15);

         Button loadBtn = new Button("Load orders");
         Button updateBtn = new Button("Update order");
         Button clearBtn = new Button("Clear");

         loadBtn.setOnAction(e -> loadOrders());
         updateBtn.setOnAction(e -> updateOrder());
         clearBtn.setOnAction(e -> ordersArea.clear());

         HBox topButtons = new HBox(10, new Label("Server IP:"), hostField, loadBtn, updateBtn, clearBtn);
         topButtons.setPadding(new Insets(10));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Order #:"), 0, 0);
        form.add(orderNumberField, 1, 0);

        form.add(new Label("New date:"), 0, 1);
        form.add(dateField, 1, 1);

        form.add(new Label("Guests:"), 0, 2);
        form.add(guestsField, 1, 2);

        VBox root = new VBox(10, topButtons, ordersArea, form);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 700, 450);
        primaryStage.setTitle("Bistro Client (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadOrders() {
        ordersArea.clear();
        try {
        		String host = hostField.getText().trim();
        		if (host.isEmpty()) {
        		    host = "localhost"; 
        		}
        		    Socket socket = new Socket(host, PORT);
        		    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        		    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        		    out.println("GET_ORDERS");
        		
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")) {
                String pretty = formatOrderLine(line);
                ordersArea.appendText(pretty + "\n");
            }

            out.println("QUIT");
        } catch (Exception e) {
            showError("Failed to load orders: " + e.getMessage());
        }
    }

    private void updateOrder() {
        String orderStr = orderNumberField.getText().trim();
        String dateStr = dateField.getText().trim();
        String guestsStr = guestsField.getText().trim();

        if (orderStr.isEmpty()) {
            showError("Order number is required");
            return;
        }

        LocalDate date = null;
        Integer guests = null;

        if (!dateStr.isEmpty() && !"null".equals(dateStr)) {
            date = LocalDate.parse(dateStr);
        }

        if (!guestsStr.isEmpty() && !"null".equals(guestsStr)) {
            guests = Integer.parseInt(guestsStr);
        }

        if (date == null && guests == null) {
            showError("Please fill at least one field to update");
            return;
        }

        try {
        	String host = hostField.getText().trim();
        	if (host.isEmpty()) {
        	    host = "localhost";
        	}

        	Socket socket = new Socket(host, PORT);
        	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String command = "UPDATE;" + orderStr + ";" + dateStr + ";" + guestsStr;

            out.println(command);

            String response = in.readLine();

            if ("OK".equals(response)) {
                showInfo("Order updated successfully!");
            } else {
                showError("Server error: " + response);
            }

            socket.close();
        }
        catch (Exception e) {
            showError("Failed to update: " + e.getMessage());
        }
    }
    
    private String formatOrderLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return line;
        }
        String orderNumber = parts[0];
        String orderDate = parts[1];
        String guests = parts[2];
        String confCode = parts[3];
        String subscriber = parts[4].isEmpty() ? "-" : parts[4];
        String placingDate = parts[5];

        return String.format(
                "Order #%s | Date: %s | Guests: %s | Confirmation code: %s | Subscriber ID: %s | Placed: %s",
                orderNumber, orderDate, guests, confCode, subscriber, placingDate
        );
    }

    private void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}