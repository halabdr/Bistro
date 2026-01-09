package reservationgui;

import client.BistroClient;
import client.ClientController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;

public final class ReservationApp {

    private static Stage primaryStage;
    private static ClientController controller;

    // keep last selection so "Back" works nicely
    private static LocalDate lastDate;
    private static String lastTime;
    private static int lastGuests;

    private ReservationApp() {}

    public static void init(Stage stage, String host, int port) throws Exception {
        primaryStage = stage;

        BistroClient c = new BistroClient(host, port);
        controller = new ClientController(c);
        controller.connect();

        showHome();
    }

    public static void showHome() {
        showReservationSearch();
    }

    public static void showReservationSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(ReservationApp.class.getResource("/reservationgui/ReservationSearch.fxml"));
            Scene scene = new Scene(loader.load());
            ReservationSearchController screen = loader.getController();
            screen.init(controller);

            primaryStage.setTitle("Bistro Client");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void showCreateReservation(LocalDate date, String hhmm, int guests) {
        try {
            lastDate = date;
            lastTime = hhmm;
            lastGuests = guests;

            FXMLLoader loader = new FXMLLoader(ReservationApp.class.getResource("/reservationgui/CreateReservation.fxml"));
            Scene scene = new Scene(loader.load());
            CreateReservationController screen = loader.getController();
            screen.init(controller, date, hhmm, guests);

            primaryStage.setScene(scene);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
