package gui;

import client.BistroClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientLoginController {

    @FXML
    private TextField ipField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleConnect(ActionEvent event) {
        String host = ipField.getText().trim();
        BistroClient.setHost(host);

        try {
            // נפתח חיבור לשרת
            BistroClient.connect();

            // נטען את החלון הראשי
            Parent root = FXMLLoader.load(getClass().getResource("/gui/BistroClientUI.fxml"));
            Stage mainStage = new Stage();
            mainStage.setTitle("Bistro Client");
            mainStage.setScene(new Scene(root));
            mainStage.show();

            // נסגור את חלון ה-Login
            Stage current = (Stage) ipField.getScene().getWindow();
            current.close();

        } catch (Exception e) {
            errorLabel.setText("Cannot connect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage current = (Stage) ipField.getScene().getWindow();
        current.close();
    }
}