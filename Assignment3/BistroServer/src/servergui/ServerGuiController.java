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
			
			// TEST DATABASE CONNECTION
			ui.display("Testing database connection...");
			if (!MySQLConnectionPool.testConnection()) {
				
				ui.display("ERROR: Failed to connect to database!");
				
				
				ui.display("Please verify your password and try again.");
				return;
			}
			ui.display("Database connection successful!");

			server = new BistroServer(port);
			server.setUI(ui, this::setClientsCount);
			server.listen();

			serverStatusLabel.setText("ONLINE");
			serverStatusLabel.getStyleClass().clear();
			serverStatusLabel.getStyleClass().add("status-online-compact");

			portLabel.setText(String.valueOf(port));
			clientsLabel.setText("0");

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
			ipLabel.setText(local.getHostAddress());
			hostLabel.setText("localhost");
		} catch (Exception ex) {
			ipLabel.setText("Unknown");
			hostLabel.setText("localhost");
		}
	}

	/**
	 * Updates the connected clients count display. Called by BistroServer when
	 * client count changes.
	 */
	private void setClientsCount(int count) {
		javafx.application.Platform.runLater(() -> {
			clientsLabel.setText(String.valueOf(count));
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
			serverStatusLabel.getStyleClass().clear();
			serverStatusLabel.getStyleClass().add("status-offline-compact");

			portLabel.setText("-");
			clientsLabel.setText("0");

			startBtn.setDisable(false);
			stopBtn.setDisable(true);
			dbPasswordField.setDisable(false);
		}
	}
}