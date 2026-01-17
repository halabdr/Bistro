package terminalgui;
import client.ClientController;
import client.Commands;
import client.MessageListener;
import clientgui.ConnectApp;
import common.Message;
import entities.WaitlistEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.Map;

public class TerminalJoinWaitlistController implements MessageListener {

    @FXML private Spinner<Integer> dinersSpinner;
    
    @FXML private RadioButton subscriberRadio;
    @FXML private RadioButton guestRadio;
    
    @FXML private VBox subscriberBox;
    @FXML private TextField subscriberField;
    
    @FXML private VBox guestBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    
    @FXML private Label statusLabel;
    @FXML private Label resultLabel;

    private ClientController controller;

    public void init(ClientController controller) {
        this.controller = controller;
        this.controller.setListener(this);

        // Diners spinner
        dinersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        
        // Toggle group (Subscriber / Guest)
        ToggleGroup tg = new ToggleGroup();
        subscriberRadio.setToggleGroup(tg);
        guestRadio.setToggleGroup(tg);
        
        subscriberRadio.setSelected(true);
        updateModeUI();
        
        tg.selectedToggleProperty().addListener((obs, oldT, newT) -> updateModeUI());
        
        resultLabel.setText("");
        setStatus("", null);
    }

    private void updateModeUI() {
        boolean isSubscriber = subscriberRadio.isSelected();
        
        subscriberBox.setVisible(isSubscriber);
        subscriberBox.setManaged(isSubscriber);
        
        guestBox.setVisible(!isSubscriber);
        guestBox.setManaged(!isSubscriber);
        
        setStatus("", null);
        resultLabel.setText("");
    }

    private void setStatus(String text, String styleClass) {
        statusLabel.setText(text == null ? "" : text);
        statusLabel.getStyleClass().removeAll("status-ok", "status-bad", "status-error");
        if (styleClass != null && !styleClass.isBlank()) {
            statusLabel.getStyleClass().add(styleClass);
        }
    }

    @FXML
    private void onJoin() {
        try {
            int diners = dinersSpinner.getValue();
            boolean isSubscriber = subscriberRadio.isSelected();
            
            String subscriberNumber = null;
            String walkInPhone = null;
            String walkInEmail = null;
            
            if (isSubscriber) {
                subscriberNumber = safeTrim(subscriberField.getText());
                if (subscriberNumber.isEmpty()) {
                    setStatus("Please enter subscriber number.", "status-error");
                    return;
                }
            } else {
                walkInPhone = safeTrim(phoneField.getText());
                walkInEmail = safeTrim(emailField.getText());
                
                if (walkInPhone.isEmpty() && walkInEmail.isEmpty()) {
                    setStatus("Please enter at least one contact: phone or email.", "status-error");
                    return;
                }
                if (!walkInEmail.isEmpty() && !walkInEmail.contains("@")) {
                    setStatus("Email looks invalid.", "status-error");
                    return;
                }
                
                // Convert empty strings to null
                if (walkInPhone.isEmpty()) walkInPhone = null;
                if (walkInEmail.isEmpty()) walkInEmail = null;
            }

            resultLabel.setText("");
            setStatus("Joining waitlist...", "status-bad");

            controller.joinWaitlist(diners, subscriberNumber, walkInPhone, walkInEmail);
            
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "status-error");
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m == null) return;
        if (!Commands.JOIN_WAITLIST.equals(m.getCommand())) return;

        Platform.runLater(() -> {
            if (!m.isSuccess()) {
                setStatus("Failed: " + m.getError(), "status-error");
                return;
            }

            Object data = m.getData();
            
            // Check if immediate table is available
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = (Map<String, Object>) data;
                
                if (Boolean.TRUE.equals(response.get("immediateTable"))) {
                    // Table available immediately
                    int tableNumber = (Integer) response.get("tableNumber");
                    setStatus("Great news! Table " + tableNumber + " is available now! " +
                             "Please proceed directly to the table.", "status-success");
                    return;
                }
            }
            
            // Added to waitlist
            if (data instanceof WaitlistEntry) {
                WaitlistEntry entry = (WaitlistEntry) data;
                setStatus("Added to waitlist! Your code: " + entry.getEntryCode() + 
                         ". You will be notified when a table is ready.", "status-success");
            } else {
                setStatus("Added to waitlist successfully!", "status-success");
            }
        });
    }

    @FXML
    private void onBack() throws Exception {
        ConnectApp.showTerminalMenu();
    }
    
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}