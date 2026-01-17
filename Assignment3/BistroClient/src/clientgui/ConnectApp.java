package clientgui;

import client.BistroClient;
import client.ClientController;
import entities.Subscriber;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import subscribergui.PayBillController;
import subscribergui.SubscriberLeaveWaitlistController;
import terminalgui.TerminalCancelReservationController;
import walkingui.WalkInMenuController;
import walkingui.WalkInCancelReservationController;
import walkingui.WalkInLeaveWaitlistController;
import walkingui.WalkInPayBillController;
import terminalgui.TerminalPayBillController;

public final class ConnectApp {

    private static Stage primaryStage;
    private static ClientController controller;

    private static Subscriber currentSubscriber;

    private ConnectApp() {}

    public static void init(Stage stage, String host, int port) throws IOException {
        primaryStage = stage;
        controller = new ClientController(new BistroClient(host, port));
        controller.connect();

        // DEV FIX: if you keep CSS/FXML/images under src (not resources),
        // Eclipse sometimes won't copy them to bin automatically.
        // This makes sure styles exist in /bin/styles so @/styles/... works.
        ensureDevResourcesCopied();
    }

    public static ClientController getController() {
        return controller;
    }

    public static void showWelcome() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/welcomegui/Welcome.fxml"));
        Scene scene = new Scene(loader.load());

        welcomegui.WelcomeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Bistro Restaurant");
        primaryStage.show();
    }

    public static void showSubscriberLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/SubscriberLogin.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.SubscriberLoginController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Subscriber Login - Bistro");
    }

    public static void showSubscriberMenu(entities.Subscriber subscriber) throws Exception {
        currentSubscriber = subscriber;

        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/CustomerMenu.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.SubscriberMenuController c = loader.getController();
        c.init(controller, subscriber);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Subscriber Menu - Bistro");
    }

    public static void showCustomerMenu() throws Exception {
        if (currentSubscriber != null) {
            showSubscriberMenu(currentSubscriber);
        } else {
            showSubscriberLogin();
        }
    }

    public static void showCancelReservation() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/CancelReservation.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.CancelReservationController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    /** Shows the Terminal Cancel Reservation screen. */
    public static void showTerminalCancelReservation() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalCancelReservation.fxml"));
        Parent root = loader.load();
        TerminalCancelReservationController ctrl = loader.getController();
        ctrl.init(controller);
        primaryStage.getScene().setRoot(root);
    }

    /** Shows the Subscriber Leave Waitlist screen. */
    public static void showSubscriberLeaveWaitlist(Subscriber subscriber) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/SubscriberLeaveWaitlist.fxml"));
        Parent root = loader.load();
        SubscriberLeaveWaitlistController ctrl = loader.getController();
        ctrl.init(controller, subscriber);
        primaryStage.getScene().setRoot(root);
    }

    public static void showPayBill() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/PayBill.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.PayBillController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showReservationSearch(Subscriber subscriber) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/reservationgui/ReservationSearch.fxml"));
        Scene scene = new Scene(loader.load());

        reservationgui.ReservationSearchController c = loader.getController();
        c.init(controller, subscriber);

        primaryStage.setScene(scene);
    }

    public static void showReservationSearch() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/reservationgui/ReservationSearch.fxml"));
        Scene scene = new Scene(loader.load());

        reservationgui.ReservationSearchController c = loader.getController();
        c.init(controller, null); // Walk-in

        primaryStage.setScene(scene);
    }

    public static void showCreateReservation(entities.Subscriber subscriber, LocalDate date, String hhmm, int guests) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/reservationgui/CreateReservation.fxml"));
        Scene scene = new Scene(loader.load());

        reservationgui.CreateReservationController c = loader.getController();
        c.init(controller, subscriber, date, hhmm, guests);

        primaryStage.setScene(scene);
    }

    public static void showLostCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/LostCode.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.LostCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    // ======================= TERMINAL SCREENS =======================

    public static void showTerminalMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalMenu.fxml"));
        Parent root = loader.load();
        primaryStage.getScene().setRoot(root);
        primaryStage.setTitle("Terminal - Bistro");
    }
    
    /**
     * Shows the Terminal More Options screen.
     */
    public static void showTerminalMoreOptions() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalMoreOptions.fxml"));
        Parent root = loader.load();
        primaryStage.getScene().setRoot(root);
    }
    
    /**
     * Shows the Terminal Pay Bill screen.
     */
    public static void showTerminalPayBill() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalPayBill.fxml"));
        Parent root = loader.load();
        TerminalPayBillController c = loader.getController();
        c.init(controller);
        primaryStage.getScene().setRoot(root);
    }

    public static void showTerminalSeatByCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/SeatByCode.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.SeatByCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Terminal - Seat by Code");
    }

    /** Lost Code opened from terminal menu -> Back returns to terminal menu. */
    public static void showTerminalLostCode() throws Exception {
        showTerminalLostCode(terminalgui.TerminalLostCodeController.BackTarget.MENU);
    }

    /** Lost Code opened from Seat-by-Code -> Back returns to Seat-by-Code. */
    public static void showTerminalLostCodeFromSeatByCode() throws Exception {
        showTerminalLostCode(terminalgui.TerminalLostCodeController.BackTarget.SEAT_BY_CODE);
    }

    private static void showTerminalLostCode(terminalgui.TerminalLostCodeController.BackTarget backTarget) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalLostCode.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.TerminalLostCodeController c = loader.getController();
        c.init(controller, backTarget);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Terminal - Lost Code");
    }

    public static void showTerminalJoinWaitlist() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalJoinWaitlist.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.TerminalJoinWaitlistController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Terminal - Join Waitlist");
    }

    public static void showTerminalLeaveWaitlist() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalLeaveWaitlist.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.TerminalLeaveWaitlistController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Terminal - Leave Waitlist");
    }

    public static void showTerminalCheckAvailability() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalCheckAvailability.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.TerminalCheckAvailabilityController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Terminal - Check Availability");
    }

    // ======================= STAFF =======================

    public static void showStaffDashboard(entities.User user) throws Exception {
        // Ensure CSS exists in bin for @/styles/... inside FXML
        ensureDevResourcesCopied();

        try {
            FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/staffgui/StaffDashboard.fxml"));
            Scene scene = new Scene(loader.load());

            // OPTIONAL: also add CSS by code (doesn't hurt, helps if FXML stylesheet fails in some env)
            var cssUrl = ConnectApp.class.getResource("/styles/bistro_dashboard.css");
            if (cssUrl != null) {
                if (!scene.getStylesheets().contains(cssUrl.toExternalForm())) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } else {
                System.err.println("[WARN] CSS not found on classpath: /styles/bistro_dashboard.css");
            }

            staffgui.StaffDashboardController c = loader.getController();
            if (c != null) {
                c.init(controller, user);
            }

            primaryStage.setTitle("Staff Dashboard - Bistro");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("=== Failed to open Staff Dashboard ===");
            e.printStackTrace();
            throw e;
        }
    }

  

    // ======================= SUBSCRIBER SCREENS =======================

    public static void showViewReservations(entities.Subscriber subscriber) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/ViewReservations.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.ViewReservationsController c = loader.getController();
        c.init(controller, subscriber);

        primaryStage.setScene(scene);
        primaryStage.setTitle("My Reservations - Bistro");
    }

    public static void showUpdatePersonalInfo(entities.Subscriber subscriber) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/UpdatePersonalInfo.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.UpdatePersonalInfoController c = loader.getController();
        c.init(controller, subscriber);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Update Personal Info - Bistro");
    }

    public static void showStaffLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/staffgui/StaffLogin.fxml"));
        Scene scene = new Scene(loader.load());

        staffgui.StaffLoginController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Login - Bistro");
    }

    // ======================= DEV-ONLY HELPERS =======================

    /**
     * If you store CSS in src/styles (not in resources),
     * this copies it into bin/styles so FXMLLoader (@/styles/...) can resolve it.
     * Safe to call multiple times.
     */
    private static void ensureDevResourcesCopied() {
        try {
            // If already available on classpath -> nothing to do
            if (ConnectApp.class.getResource("/styles/bistro_dashboard.css") != null) return;

            // Working dir in Eclipse is usually the project folder (e.g. .../BistroClient)
            Path projectDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

            Path srcCss = projectDir.resolve("src").resolve("styles").resolve("bistro_dashboard.css");
            Path binCss = projectDir.resolve("bin").resolve("styles").resolve("bistro_dashboard.css");

            if (!Files.exists(srcCss)) {
                System.err.println("[WARN] Cannot find CSS in src: " + srcCss);
                return;
            }

            Files.createDirectories(binCss.getParent());
            Files.copy(srcCss, binCss, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("[DEV] Copied CSS to bin: " + binCss);

        } catch (Exception e) {
            System.err.println("[WARN] ensureDevResourcesCopied failed:");
            e.printStackTrace();
        }
    }
    
    /**
     * Shows the PayBill screen with a pre-filled confirmation code.
     * 
     * @param subscriber the logged-in subscriber
     * @param confirmationCode the confirmation code to pre-fill
     */
    public static void showPayBillWithCode(Subscriber subscriber, String confirmationCode) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/PayBill.fxml"));
        Parent root = loader.load();
        
        PayBillController c = loader.getController();
        c.init(controller, subscriber);
        c.setConfirmationCode(confirmationCode);
        
        primaryStage.getScene().setRoot(root);
        primaryStage.setTitle("Bistro - Pay Bill");
    }

    // ======================= WALK-IN CUSTOMER SCREENS =======================

    /**
     * Shows the Walk-In Customer Menu screen.
     */
    public static void showWalkInMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/walkingui/WalkInMenu.fxml"));
        Scene scene = new Scene(loader.load());

        WalkInMenuController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Guest Services - Bistro");
    }

    /**
     * Shows the Walk-In Cancel Reservation screen.
     */
    public static void showWalkInCancelReservation() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/walkingui/WalkInCancelReservation.fxml"));
        Scene scene = new Scene(loader.load());

        WalkInCancelReservationController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Cancel Reservation - Bistro");
    }

    /**
     * Shows the Walk-In Leave Waitlist screen.
     */
    public static void showWalkInLeaveWaitlist() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/walkingui/WalkInLeaveWaitlist.fxml"));
        Scene scene = new Scene(loader.load());

        WalkInLeaveWaitlistController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Leave Waitlist - Bistro");
    }

    /**
     * Shows the Walk-In Pay Bill screen.
     */
    public static void showWalkInPayBill() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/walkingui/WalkInPayBill.fxml"));
        Scene scene = new Scene(loader.load());

        WalkInPayBillController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Pay Bill - Bistro");
    }
}