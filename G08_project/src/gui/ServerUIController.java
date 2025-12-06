package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import server.EchoServer;

//Controller for the sever GUI
//Displays port, server status and a log of operations
public class ServerUIController {

    @FXML
    private TextField portField;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea logArea;

    private Thread serverThread;

    //Called after FXML loading, initializes default port and auto-start 
    @FXML
    private void initialize() {
    	portField.setText("5555");
        appendLog("Server UI loaded.");
        
        //Auto-start server 
        Platform.runLater(this::handleStart);
    }

    //Starts the server
    @FXML
    private void handleStart() {
    	//Prevent starting the server twice
        if (serverThread != null && serverThread.isAlive()) {
            appendLog("Server is already running.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            appendLog("Port must be an integer.");
            return;
        }

        statusLabel.setText("Running on port " + port);

        //Logger that updates the textArea
        EchoServer server = new EchoServer(port, this::appendLog);

        serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        appendLog("Server started.");
    }

    //There is no button for close basiclly closing the window of the server stops the JVM
    @FXML
    private void handleStop() {
        appendLog("To fully stop the server, close this window.");
        statusLabel.setText("Stop requested (close window).");
    }

    //Appends message to the GUI text area
    private void appendLog(String msg) {
        //Used because logs might arrive from a non-JavaFX thread
    	Platform.runLater(() -> {
            logArea.appendText(msg + System.lineSeparator());
        });
    }
}