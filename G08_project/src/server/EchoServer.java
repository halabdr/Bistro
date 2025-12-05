package server;

import server.DBController;   // אם DBController אצלך ב-package server אז פשוט "import server.DBController;"
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class EchoServer {

    private int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Starting server on port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            DBController.connect(); // להתחבר ל-DB פעם אחת

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBController.close(); // סגירת החיבור ל-DB
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                 new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String clientIp = clientSocket.getInetAddress().getHostAddress();
            String clientHost = clientSocket.getInetAddress().getHostName();
            System.out.println("Client connected: IP=" + clientIp +
                               ", host=" + clientHost + ", status=Connected");

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("GET_ORDERS")) {
                    handleGetOrders(out);
                } else if (line.startsWith("UPDATE;")) {
                    handleUpdate(line, out);
                } else if (line.equals("QUIT")) {
                    System.out.println("Client requested to quit.");
                    break;
                } else {
                    out.println("ERROR: Unknown command");
                }
            }
            System.out.println("Client disconnected: IP=" + clientIp +
                               ", host=" + clientHost + ", status=Disconnected");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void handleGetOrders(PrintWriter out) {
        try {
            List<String> orders = DBController.readOrders();
            for (String s : orders) {
                out.println(s);
            }
            out.println("END"); // סימון סוף רשימה
        } catch (Exception e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleUpdate(String line, PrintWriter out) {
        try {
            String[] parts = line.split(";", -1); // גם שדות ריקים
            if (parts.length != 4) {
                out.println("ERROR: Bad UPDATE format");
                return;
            }

            int orderNumber = Integer.parseInt(parts[1]);
            String dateStr   = parts[2];
            String guestsStr = parts[3];

            LocalDate newDate = null;
            if (!dateStr.isEmpty()) {
                newDate = LocalDate.parse(dateStr);
            }

            Integer newGuests = null;
            if (!guestsStr.isEmpty()) {
                newGuests = Integer.parseInt(guestsStr);
            }

            DBController.updateOrder(orderNumber, newDate, newGuests);
            out.println("OK");

        } catch (Exception e) {
            out.println("ERROR: " + e.getMessage());
        }
    }
}