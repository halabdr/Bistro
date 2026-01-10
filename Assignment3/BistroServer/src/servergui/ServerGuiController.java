package servergui;
import common.ChatIF;
import connection.BistroServer;
import connection.MySQLConnectionPool;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.InetAddress;

/**
 * Controller for the Server GUI. Manages server start/stop, displays connection
 * info, and shows activity log.
 */
public class ServerGuiController {

	@FXML
	private TextField portField;
	@FXML
	private TextField dbHostField;
	@FXML
	private TextField dbPortField;
	@FXML
	private TextField dbUserField;
	@FXML
	private PasswordField dbPasswordField;
	@FXML
	private Label serverStatusLabel;
	@FXML
	private Label ipLabel;
	@FXML
	private Label hostLabel;
	@FXML
	private Label portLabel;
	@FXML
	private Label clientsLabel;
	@FXML
	private TextArea logArea;
	@FXML
	private Button startBtn;
	@FXML
	private Button stopBtn;

	private BistroServer server;
	private ChatIF ui;

	/**
	 * Initializes the controller. Called automatically by JavaFX after FXML
	 * loading.
	 */
	@FXML
	public void initialize() {
		ui = new ServerUI(logArea);
		portField.setText(String.valueOf(BistroServer.DEFAULT_PORT));
		refreshHostInfo();
		ui.display("Server GUI initialized and ready.");
		ui.display("Enter DB password to start server.");
	}

	/**
	 * Handles Start Server button click.
	 */
	@FXML
	private void onStart(ActionEvent e) {
		// Validate DB password
		String dbPassword = dbPasswordField.getText().trim();
		if (dbPassword.isEmpty()) {
			ui.display("ERROR: Please enter database password");
			showAlert("Database Error", "Please enter the database password before starting the server.");
			return;
		}

		try {
			int port = Integer.parseInt(portField.getText().trim());
			String dbHost = dbHostField.getText().trim();
			String dbPort = dbPortField.getText().trim();
			String dbUser = dbUserField.getText().trim();

			// Set DB credentials in connection pool before starting server
			MySQLConnectionPool.setDatabaseCredentials(dbHost, dbPort, dbUser, dbPassword);
			ui.display("Database credentials configured.");

			server = new BistroServer(port);
			server.setUI(ui, this::setClientsCount);
			server.listen();

			serverStatusLabel.setText("ONLINE");
			serverStatusLabel.getStyleClass().remove("status-offline");
			serverStatusLabel.getStyleClass().add("status-online");

			portLabel.setText("Port: " + port);

			startBtn.setDisable(true);
			stopBtn.setDisable(false);
			dbPasswordField.setDisable(true);

			ui.display("Server started successfully on port " + port);
			ui.display("Listening for client connections...");

		} catch (NumberFormatException ex) {
			ui.display("ERROR: Invalid port number");
		} catch (Exception ex) {
			ui.display("ERROR: Failed to start server - " + ex.getMessage());
			ex.printStackTrace();
			showAlert("Server Error", "Failed to start server: " + ex.getMessage());
		}
	}

	/**
	 * Handles Stop Server button click.
	 */
	@FXML
	private void onStop(ActionEvent e) {
		shutdown();
	}

	/**
	 * Handles Clear Log button click.
	 */
	@FXML
	private void onClearLog(ActionEvent e) {
		logArea.clear();
		ui.display("Log cleared.");
	}

	/**
	 * Handles Exit button click.
	 */
	@FXML
	private void onExit(ActionEvent e) {
		shutdown();
		System.exit(0);
	}

	/**
	 * Refreshes the host information display.
	 */
	private void refreshHostInfo() {
		try {
			InetAddress local = InetAddress.getLocalHost();
			ipLabel.setText("IP: " + local.getHostAddress());
			hostLabel.setText("Host: localhost");
		} catch (Exception ex) {
			ipLabel.setText("IP: Unknown");
			hostLabel.setText("Host: localhost");
		}
	}

	/**
	 * Updates the connected clients count display. Called by BistroServer when
	 * client count changes.
	 */
	private void setClientsCount(int count) {
		javafx.application.Platform.runLater(() -> {
			clientsLabel.setText("Connected Clients: " + count);
		});
	}

	/**
	 * Shuts down the server gracefully.
	 */
	public void shutdown() {
		try {
			if (server != null && server.isListening()) {
				server.close();
				ui.display("Server stopped successfully.");
			}
		} catch (Exception ex) {
			ui.display("ERROR: Failed to stop server - " + ex.getMessage());
		} finally {
			serverStatusLabel.setText("OFFLINE");
			serverStatusLabel.getStyleClass().remove("status-online");
			serverStatusLabel.getStyleClass().add("status-offline");

			portLabel.setText("Port: -");
			clientsLabel.setText("Connected Clients: 0");

			startBtn.setDisable(false);
			stopBtn.setDisable(true);
			dbPasswordField.setDisable(false);
		}
	}

	/**
	 * Shows an alert dialog to the user.
	 */
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}