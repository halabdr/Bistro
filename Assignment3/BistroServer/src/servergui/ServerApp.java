package servergui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for Bistro Server GUI. Launches the JavaFX interface
 * for managing the server.
 */
public class ServerApp extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/servergui/server_gui.fxml"));
		Scene scene = new Scene(loader.load());

// Load CSS stylesheet
		scene.getStylesheets().add(getClass().getResource("/servergui/server_styles.css").toExternalForm());

		stage.setTitle("Bistro Restaurant - Server Manager");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> {
			ServerGuiController controller = loader.getController();
			if (controller != null) {
				controller.shutdown();
			}
		});
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}