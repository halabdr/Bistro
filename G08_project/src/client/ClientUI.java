package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//Loads the login window where the user enters the server IP
public class ClientUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
    	//Load the FXML for the login window from the gui package
        Parent root = FXMLLoader.load(getClass().getResource("/gui/ClientLogin.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Bistro - Connect to Server");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        //When the JavaFX application is closed,we close the socket
        BistroClient.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}