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

//Controller for the login window where the user types the server IP
public class ClientLoginController {

    @FXML
    private TextField ipField;

    @FXML
    private Label errorLabel;

    //Read the IP
    //Sets it in BistroClient
    //Try to open a socket if successful, opens the main window (orders screen)
    @FXML
    private void handleConnect(ActionEvent event) {
        String host = ipField.getText().trim();
        BistroClient.setHost(host);

        try {
            BistroClient.connect();

            //opens the orders screen window
            Parent root = FXMLLoader.load(getClass().getResource("/gui/BistroClientUI.fxml"));
            Stage mainStage = new Stage();
            mainStage.setTitle("Bistro Client");
            mainStage.setScene(new Scene(root));
            mainStage.show();

            //Close the login window
            Stage current = (Stage) ipField.getScene().getWindow();
            current.close();

        } catch (Exception e) {
            errorLabel.setText("Cannot connect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Handles the Exit button on the login window
    //Closes the stage
    @FXML
    private void handleExit(ActionEvent event) {
        Stage current = (Stage) ipField.getScene().getWindow();
        current.close();
    }
}