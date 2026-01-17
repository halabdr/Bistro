package clientgui;

import client.BistroClient;
import client.ClientController;
import entities.Subscriber;
import subscribergui.SubscriberLeaveWaitlistController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;
import java.time.LocalDate;
import terminalgui.TerminalCancelReservationController;

public final class ConnectApp {

    private static Stage primaryStage;
    private static ClientController controller;

    private static Subscriber currentSubscriber;

    private ConnectApp() {}

    public static void init(Stage stage, String host, int port) throws IOException {
        primaryStage = stage;
        controller = new ClientController(new BistroClient(host, port));
        controller.connect();
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
    
    /**
     * Shows the Terminal Cancel Reservation screen.
     */
    public static void showTerminalCancelReservation() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalCancelReservation.fxml"));
        Parent root = loader.load();
        TerminalCancelReservationController ctrl = loader.getController();
        ctrl.init(controller);
        primaryStage.getScene().setRoot(root);
    }
    
    /**
     * Shows the Subscriber Leave Waitlist screen.
     */
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
        Scene scene = new Scene(loader.load());

        terminalgui.TerminalMenuController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Terminal - Bistro");
        primaryStage.show();
    }

    public static void showTerminalSeatByCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/SeatByCode.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.SeatByCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Terminal - Seat by Code");
    }

    /**
     * Lost Code opened from terminal menu -> Back returns to terminal menu.
     */
    public static void showTerminalLostCode() throws Exception {
        showTerminalLostCode(terminalgui.TerminalLostCodeController.BackTarget.MENU);
    }

    /**
     * Lost Code opened from Seat-by-Code -> Back returns to Seat-by-Code.
     */
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

    public static void showStaffDashboard(entities.User staffUser) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/staffgui/StaffDashboard.fxml"));
        Scene scene = new Scene(loader.load());

        staffgui.StaffDashboardController c = loader.getController();
        c.init(controller, staffUser);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Dashboard - Bistro");
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
}