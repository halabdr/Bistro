package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

	@Override
	public void start(Stage stage) throws Exception {
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientgui/connect.fxml"));
	    Scene scene = new Scene(loader.load());
	    stage.setScene(scene);
	    stage.setTitle("Bistro - Connect");
	    stage.show();
	}
    public static void main(String[] args) {
        launch(args);
    }
}


