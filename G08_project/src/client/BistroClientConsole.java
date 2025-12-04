package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BistroClientConsole {

	public static void main(String[] args) {
		//connect to server
		try (Socket socket = new Socket("localhost", 5555);
			 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			 Scanner scanner = new Scanner(System.in)) {
			 System.out.println("Connected to server");
			 
			 boolean running = true;
			 while (running) {
				 System.out.println();
				 System.out.println("==== Bistro Client ====");
				 System.out.println("1. View orders");
				 System.out.println("2. Update order (date & guests)");
				 System.out.println("3. Quit");
				 System.out.println("Choose option: ");
                
				 String choice = scanner.nextLine().trim();
				 switch(choice) {
				    case "1":
				    	out.println("GET_ORDERS");
                        System.out.println("Orders from server:");
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.equals("END")) {
                                break;
                            }
                            String[] parts = line.split(";");
                            if (parts.length == 6) {
                                String orderNumber  = parts[0];
                                String orderDate    = parts[1];
                                String guests       = parts[2];
                                String confCode     = parts[3];
                                String subscriberId = parts[4];
                                String placingDate  = parts[5];
                                System.out.println(
                                        "Order #" + orderNumber +
                                        " | Date: " + orderDate +
                                        " | Guests: " + guests +
                                        " | Confirmation code: " + confCode +
                                        " | Subscriber ID: " + subscriberId +
                                        " | Placed: " + placingDate
                                );
                            } else {
                                System.out.println("RAW: " + line);
                            }
                        }
                        break;
                    case "2":
                        //Update reservation
                        System.out.print("Enter order number: ");
                        String orderNumStr = scanner.nextLine().trim();
                        System.out.print("Enter new date (YYYY-MM-DD): ");
                        String dateStr = scanner.nextLine().trim();
                        System.out.print("Enter new number of guests: ");
                        String guestsStr = scanner.nextLine().trim();
                        String msg = "UPDATE;" + orderNumStr + ";" + dateStr + ";" + guestsStr;
                        out.println(msg);
                        String response = in.readLine();
                        System.out.println("Server response: " + response);
                        break;
                    case "3":
                        out.println("QUIT");
                        running = false;
                        System.out.println("Closing client...");
                        break;
                    default:
                        System.out.println("Unknown option");
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}