package clientgui;
import client.BistroClient;
import client.ClientController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public final class ConnectApp {

    private static Stage primaryStage;
    private static ClientController controller;

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
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/CustomerMenu.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.SubscriberMenuController c = loader.getController();
        c.init(controller, subscriber);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Subscriber Menu - Bistro");
    }

    public static void showCustomerMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/CustomerMenu.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.SubscriberMenuController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showCancelReservation() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/CancelReservation.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.CancelReservationController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showPayBill() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/PayBill.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.PayBillController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }
    
    public static void showReservationSearch() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ConnectApp.class.getResource("/reservationgui/ReservationSearch.fxml")
        );
        Scene scene = new Scene(loader.load());

        reservationgui.ReservationSearchController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showCreateReservation(LocalDate date, String hhmm, int guests) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/reservationgui/CreateReservation.fxml"));
        Scene scene = new Scene(loader.load());

        reservationgui.CreateReservationController c = loader.getController();
        c.init(controller, date, hhmm, guests);

        primaryStage.setScene(scene);
    }

    public static void showLostCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/LostCode.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.LostCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }
    
    public static void showTerminalLostCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/TerminalLostCode.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.TerminalLostCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showTerminalSeatByCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/SeatByCode.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.SeatByCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }
    
    public static void showStaffDashboard(entities.User staffUser) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/staffgui/StaffDashboard.fxml"));
        Scene scene = new Scene(loader.load());

        staffgui.StaffDashboardController c = loader.getController();
        c.init(controller, staffUser);

        primaryStage.setScene(scene);
    }
    
    public static void showViewReservations(entities.Subscriber subscriber) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/subscribergui/ViewReservations.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.ViewReservationsController c = loader.getController();
        c.init(controller, subscriber);

        primaryStage.setScene(scene);
        primaryStage.setTitle("My Reservations - Bistro");
    }
}