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

    public static void showHome() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/homegui/Home.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bistro Client");
        primaryStage.show();
    }

    public static void showCustomerMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/customergui/CustomerMenu.fxml"));
        Scene scene = new Scene(loader.load());

        customergui.CustomerMenuController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showReservationSearch() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/reservationgui/ReservationSearch.fxml"));
        Scene scene = new Scene(loader.load());

        reservationgui.ReservationSearchController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showCreateReservation(LocalDate date, String hhmm, int guests) throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/reservationgui/CreateReservation.fxml"));
        Scene scene = new Scene(loader.load());

        reservationgui.CreReservationController c = loader.getController();
        c.init(controller, date, hhmm, guests);

        primaryStage.setScene(scene);
    }

    public static void showCancelReservation() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/customergui/CancelReservation.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.CancelReservationController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showLostCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/customergui/LostCode.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.LostCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    public static void showPayBill() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/customergui/PayBill.fxml"));
        Scene scene = new Scene(loader.load());

        subscribergui.PayBillController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }

    // -------- Terminal --------
    public static void showTerminalSeatByCode() throws Exception {
        FXMLLoader loader = new FXMLLoader(ConnectApp.class.getResource("/terminalgui/SeatByCode.fxml"));
        Scene scene = new Scene(loader.load());

        terminalgui.SeatByCodeController c = loader.getController();
        c.init(controller);

        primaryStage.setScene(scene);
    }
}
