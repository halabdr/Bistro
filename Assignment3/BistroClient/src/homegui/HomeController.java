package homegui;

import client.ClientController;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class HomeController implements MessageListener {

    private ClientController controller;

    @FXML
    public void initialize() {
        controller = ConnectApp.getController();
    }

    @FXML
    private void onCustomerClicked() throws Exception {
        ConnectApp.showCustomerMenu();
    }

    @FXML
    private void onTerminalClicked() throws Exception {
        ConnectApp.showTerminalSeatByCode();
    }

    @FXML
    private void onStaffClicked() {
        try {
            ClientController controller = clientgui.ConnectApp.getController();

            if (controller == null || !controller.isConnected()) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Not connected.\nPlease connect first.", ButtonType.OK);
                a.showAndWait();
                return;
            }

            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle("Staff Login");
            ButtonType loginBtn = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

            TextField emailField = new TextField();
            emailField.setPromptText("Email");
            PasswordField passField = new PasswordField();
            passField.setPromptText("Password");

            GridPane g = new GridPane();
            g.setHgap(10); g.setVgap(10);
            g.addRow(0, new Label("Email:"), emailField);
            g.addRow(1, new Label("Password:"), passField);
            dialog.getDialogPane().setContent(g);

            dialog.getDialogPane().lookupButton(loginBtn).disableProperty()
                    .bind(emailField.textProperty().isEmpty().or(passField.textProperty().isEmpty()));

            dialog.setResultConverter(btn -> {
                if (btn == loginBtn) return new String[]{emailField.getText().trim(), passField.getText()};
                return null;
            });

            dialog.showAndWait().ifPresent(creds -> {
                try {
                    controller.setListener(message -> {
                        if (!client.commands.LOGIN.equals(message.getCommand())) return;

                        javafx.application.Platform.runLater(() -> {
                            if (!message.isSuccess()) {
                                new Alert(Alert.AlertType.ERROR, "Login failed: " + message.getError(), ButtonType.OK).showAndWait();
                                return;
                            }

                            Object data = message.getData();
                            if (!(data instanceof entities.User u)) {
                                new Alert(Alert.AlertType.ERROR, "Login succeeded but returned invalid user object.", ButtonType.OK).showAndWait();
                                return;
                            }

                            // Staff only:
                            if (u.getUserRole() != entities.User.UserRole.MANAGER &&
                                    u.getUserRole() != entities.User.UserRole.REPRESENTATIVE) {
                                new Alert(Alert.AlertType.ERROR, "Access denied. Role: " + u.getUserRole(), ButtonType.OK).showAndWait();
                                return;
                            }

                            try {
                                clientgui.ConnectApp.showStaffDashboard(u);
                            } catch (Exception e) {
                                new Alert(Alert.AlertType.ERROR, "Failed to open staff dashboard: " + e.getMessage(), ButtonType.OK).showAndWait();
                            }
                        });
                    });

                    controller.login(creds[0], creds[1]);

                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Login error: " + e.getMessage(), ButtonType.OK).showAndWait();
                }
            });

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }


    @Override
    public void onMessage(Object msg) {
        if (!(msg instanceof Message m)) return;
        if (!"LOGIN".equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                showAlert(Alert.AlertType.ERROR, "Login failed", String.valueOf(m.getError()));
                return;
            }

            Object data = m.getData();
            if (!(data instanceof User u)) {
                // In case server returns Subscriber/Representative (they extend User) -> still ok because instanceof User
                showAlert(Alert.AlertType.INFORMATION, "Logged in",
                        "Login succeeded, but returned object is not User.");
                return;
            }

            User.UserRole role = u.getUserRole();
            if (role == User.UserRole.MANAGER || role == User.UserRole.REPRESENTATIVE) {
                showAlert(Alert.AlertType.INFORMATION, "Staff login OK",
                        "Welcome " + u.getName() + " (" + role + ").\n\nStaff screens are not connected yet.");
                // פה בעתיד: ConnectApp.showStaffMenu();
            } else {
                showAlert(Alert.AlertType.ERROR, "Access denied",
                        "This account is not staff. Role: " + role);
            }
        });
    }

    // -------- helpers --------

    private record LoginData(String email, String password) {}

    private Dialog<LoginData> buildLoginDialog() {
        Dialog<LoginData> dialog = new Dialog<>();
        dialog.setTitle("Staff Login");

        ButtonType loginBtn = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Email:"), emailField);
        grid.addRow(1, new Label("Password:"), passField);

        dialog.getDialogPane().setContent(grid);

        // disable Login if empty
        dialog.getDialogPane().lookupButton(loginBtn).disableProperty()
                .bind(emailField.textProperty().isEmpty().or(passField.textProperty().isEmpty()));

        dialog.setResultConverter(btn -> {
            if (btn == loginBtn) {
                return new LoginData(emailField.getText().trim(), passField.getText());
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
