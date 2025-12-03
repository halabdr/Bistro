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

    public BistroServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Starting server on port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
        	
        	DBController.connect();//Added this - to connect to DB once at beginning
            
        	while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	DBController.close();//Close connection to DB when server is closed
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
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
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	try {
        		clientSocket.close();
        	}
        	catch(IOException ignorred) {}
        }
    }
        	
    private void handleGetOrders(PrintWriter out) {
        try {
            List<String> orders = DBController.readOrders();
            for (String s : orders) {
                out.println(s);
            }
            out.println("END"); //marking to client that it is the end of the list
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
            LocalDate newDate = LocalDate.parse(parts[2]);//format: dd-mm-yyyy
            int guests = Integer.parseInt(parts[3]);

            DBController.updateOrder(orderNumber, newDate, guests);
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