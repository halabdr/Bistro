package servergui;

import common.ChatIF;
import database.DBController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import server.BistroServer;

import java.net.InetAddress;

public class ServerGuiController {

    @FXML private TextField portField;

    @FXML private TextField dbHostField;
    @FXML private TextField dbNameField;
    @FXML private TextField dbUserField;
    @FXML private PasswordField dbPassField;

    @FXML private Label serverStatusLabel;
    @FXML private Label dbStatusLabel;
    
    @FXML private Accordion accordion;
    @FXML private TitledPane dbPane;

    @FXML private Label ipLabel;
    @FXML private Label hostLabel;
    @FXML private Label portLabel;
    @FXML private Label clientsLabel;

    @FXML private TextArea logArea;

    @FXML private Button startBtn;
    @FXML private Button stopBtn;

    private BistroServer server;
    private ChatIF ui;

    @FXML
    public void initialize() {
    	ui = new ServerUI(logArea);

        if (accordion != null && dbPane != null) {
            accordion.setExpandedPane(dbPane);
        }

        portField.setText(String.valueOf(BistroServer.DEFAULT_PORT));
        refreshHostInfo();
        ui.display("Server GUI ready.");
    }

    @FXML
    private void onStart(ActionEvent e) {
        int port = Integer.parseInt(portField.getText().trim());

        try {
            server = new BistroServer(port);
            server.setUI(ui, this::setClientsCount); 
            server.listen();

            serverStatusLabel.setText("ONLINE");
            portLabel.setText("Port: " + port);

            startBtn.setDisable(true);
            stopBtn.setDisable(false);

            ui.display("Server started and listening on port " + port);
        } catch (Exception ex) {
            ui.display("Failed to start server: " + ex.getMessage());
        }
    }

    @FXML
    private void onStop(ActionEvent e) {
        try {
            if (server != null) {
                server.close();
                ui.display("Server stopped.");
            }
        } catch (Exception ex) {
            ui.display("Failed to stop server: " + ex.getMessage());
        } finally {
            serverStatusLabel.setText("OFFLINE");
            startBtn.setDisable(false);
            stopBtn.setDisable(true);
        }
    }

    @FXML
    private void onTestDb(ActionEvent e) {
        String host = dbHostField.getText().trim();
        String db = dbNameField.getText().trim();
        String user = dbUserField.getText().trim();
        String pass = dbPassField.getText();

        try {
            boolean ok = DBController.testConnection(host, db, user, pass); 
            dbStatusLabel.setText(ok ? "CONNECTED" : "FAILED");
            ui.display(ok ? "DB connected successfully." : "DB connection failed.");
        } catch (Exception ex) {
            dbStatusLabel.setText("FAILED");
            ui.display("DB error: " + ex.getMessage());
        }
    }

    @FXML
    private void onClearLog(ActionEvent e) {
        logArea.clear();
    }

    @FXML
    private void onExit(ActionEvent e) {
        onStop(null);
        System.exit(0);
    }

    private void refreshHostInfo() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            ipLabel.setText("IP: " + local.getHostAddress());
            hostLabel.setText("Host: " + local.getHostName());
        } catch (Exception ex) {
            ipLabel.setText("IP: -");
            hostLabel.setText("Host: -");
        }
    }

    private void setClientsCount(int count) {
        clientsLabel.setText("Clients connected: " + count);
    }
}
