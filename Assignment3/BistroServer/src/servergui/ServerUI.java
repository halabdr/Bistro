package servergui;

import common.ChatIF;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerUI implements ChatIF {

    private final TextArea logArea;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ServerUI(TextArea logArea) {
        this.logArea = logArea;
    }

    @Override
    public void display(String message) {
        Platform.runLater(() -> {
            String ts = LocalDateTime.now().format(fmt);
            logArea.appendText("[" + ts + "] " + message + "\n");
        });
    }
}
