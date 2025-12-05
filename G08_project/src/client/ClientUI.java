package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/gui/ClientLogin.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Bistro - Connect to Server");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // נסגור יפה את החיבור כשסוגרים את האפליקציה
        BistroClient.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}