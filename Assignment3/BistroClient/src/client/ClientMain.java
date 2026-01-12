package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the Bistro Client application.
 * Shows the connection screen first, then navigates to Welcome after connecting.
 */
public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientgui/connect.fxml"));
        Scene scene = new Scene(loader.load());
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Connect to Bistro Server");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}