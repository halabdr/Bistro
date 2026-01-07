package clientgui;

import client.ClientController;
import homegui.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import slotsgui.AvailableSlotsController;

public class ConnectApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showConnect();
        stage.setTitle("Bistro Client");
        stage.show();
    }

    public static void showConnect() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/clientgui/connect.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
    }

    public static void showAvailableSlots(ClientController controller) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/AvailableSlots.fxml"));
        Scene scene = new Scene(loader.load());

        AvailableSlotsController c = loader.getController();
        c.setClientController(controller);

        primaryStage.setScene(scene);
    }
    
    public static void showHome(ClientController controller) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/home.fxml"));
        Scene scene = new Scene(loader.load());

        HomeController c = loader.getController();
        c.setClientController(controller);

        primaryStage.setScene(scene);
    }


    public static void main(String[] args) {
        launch(args);
    }
}