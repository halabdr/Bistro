package client;

import javafx.application.Application;
import javafx.stage.Stage;
import reservationgui.ReservationApp;

public class ClientMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String host = "localhost";
        int port = 5555; 
        ReservationApp.init(stage, host, port);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
