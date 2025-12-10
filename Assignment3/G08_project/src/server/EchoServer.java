package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

//Listens on a port, accept clients and handle text commands
public class EchoServer {

    private int port;
    
    //Logger to print messages
    private Consumer<String> logger;
   
    //Constructor that uses the default logger
    public EchoServer(int port) {
        this(port, null);
    }
    
    public EchoServer(int port, Consumer<String> logger) {
        this.port = port;
        this.logger = (logger != null) ? logger : System.out::println;
        
        //Connect the logger of the server to DBController
        DBController.setLogger(this.logger);
    }

    private void log(String msg) {
        logger.accept(msg);
    }

    //Accepts clients and delegates each client to handleClient()
    public void start() {
        log("Starting server on port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            DBController.connect();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log("Server error: " + e.getMessage());
        } finally {
            DBController.close();
        }
    }
    
    //Handle the connection for each client
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String clientIp   = clientSocket.getInetAddress().getHostAddress();
            String clientHost = clientSocket.getInetAddress().getHostName();
            log("Client connected: IP=" + clientIp + ", host=" + clientHost + ", status=Connected");

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("GET_ORDERS")) {
                    handleGetOrders(out);
                } else if (line.startsWith("UPDATE;")) {
                    handleUpdate(line, out);
                } else if (line.equals("QUIT")) {
                    log("Client requested to quit.");
                    break;
                } else {
                    out.println("ERROR: Unknown command");
                }
            }

            log("Client disconnected: IP=" + clientIp + ", host=" + clientHost + ", status=Disconnected");

        } catch (Exception e) {
            e.printStackTrace();
            log("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    //Reads all orders from DB and sends them line by line 
    private void handleGetOrders(PrintWriter out) {
        try {
            List<String> orders = DBController.readOrders();
            for (String s : orders) {
                out.println(s);
            }
            out.println("END");
        } catch (Exception e) {
            out.println("ERROR: " + e.getMessage());
            log("DB error (GET_ORDERS): " + e.getMessage());
        }
    }

    //Implements UPDATE command
    private void handleUpdate(String line, PrintWriter out) {
        try {
            String[] parts = line.split(";", -1);
            if (parts.length != 4) {
                out.println("ERROR: Bad UPDATE format");
                return;
            }

            int orderNumber   = Integer.parseInt(parts[1]);
            String dateStr    = parts[2];
            String guestsStr  = parts[3];

            LocalDate newDate = null;
            Integer newGuests = null;

            if (!dateStr.isEmpty()) {
                newDate = LocalDate.parse(dateStr);
            }

            if (!guestsStr.isEmpty()) {
                newGuests = Integer.parseInt(guestsStr);
            }

            //Validate the new date that the client insert
            if (newDate != null) {

                //Get the original dates from DB
                LocalDate[] dates = DBController.getOrderDates(orderNumber);
                if (dates == null) {
                    out.println("ERROR: Order " + orderNumber + " not found");
                    return;
                }

                LocalDate currentOrderDate = dates[0];
                LocalDate placingDate      = dates[1];

                //Validate that the new date is not before the placed date
                if (newDate.isBefore(placingDate)) {
                    out.println("ERROR: New date cannot be before placing date (" + placingDate + ")");
                    return;
                }

                //Validate that the new date is not more than month of the placed date
                if (newDate.isAfter(placingDate.plusMonths(1))) {
                    out.println("ERROR: New date must be within one month of placing date (" + placingDate + ")");
                    return;
                }
            }

            //Update
            DBController.updateOrder(orderNumber, newDate, newGuests);
            out.println("OK");
            log("Order #" + orderNumber + " updated");

        } catch (Exception e) {
            out.println("ERROR: " + e.getMessage());
            log("Update error: " + e.getMessage());
        }
    }
}