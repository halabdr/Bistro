package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import welcomegui.WelcomeController;

/**
 * Main entry point for the Bistro Client application.
 */
public class ClientMain extends Application {

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 5555;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/welcomegui/Welcome.fxml"));
        Scene scene = new Scene(loader.load());
        
        WelcomeController controller = loader.getController();
        controller.setStage(primaryStage);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bistro Restaurant - Welcome");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}