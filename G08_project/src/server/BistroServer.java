package server;

import database.DBController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class BistroServer {

    private int port;
    private DBController db;

    public BistroServer(int port) {
        this.port = port;
        this.db = new DBController();
    }

    public void start() {
        System.out.println("Starting server on port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String clientIp = clientSocket.getInetAddress().getHostAddress();
            String clientHost = clientSocket.getInetAddress().getHostName();
            System.out.println("Client connected: IP=" + clientIp + ", host=" + clientHost + ", status=Connected");

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
            System.out.println("Client disconnected: IP=" + clientIp + ", host=" + clientHost + ", status=Disconnected");
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleGetOrders(PrintWriter out) {
        try {
            List<String> orders = db.readOrders();
            for (String s : orders) {
                out.println(s);
            }
            out.println("END");
        } catch (Exception e) {
            out.println("ERROR: " + e.getMessage());
        }
    }
    private void handleUpdate(String line, PrintWriter out) {
        try {
            String[] parts = line.split(";");
            if (parts.length != 4) {
                out.println("ERROR: Bad UPDATE format");
                return;
            }
            int orderNumber = Integer.parseInt(parts[1]);
            LocalDate newDate = LocalDate.parse(parts[2]);
            int guests = Integer.parseInt(parts[3]);

            db.updateOrder(orderNumber, newDate, guests);
            out.println("OK");
        } catch (Exception e) {
            out.println("ERROR: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        int port = 5555; 
        BistroServer server = new BistroServer(port);
        server.start();
    }
}