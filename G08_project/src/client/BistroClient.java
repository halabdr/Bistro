package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BistroClient {

    private static String host = "localhost";
    public static final int PORT = 5555;

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;

    //Sets the target host
    public static void setHost(String h) {
        host = (h == null || h.isEmpty()) ? "localhost" : h;
    }

    public static String getHost() {
        return host;
    }

    //Opens a connection to the server if not already connected
    //Creates the socket and I/O streams
    public static void connect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return; //Already connected
        }
        socket = new Socket(host, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Client connected to " + host + ":" + PORT);
    }
    
    //Returns the PrintWriter used to send messages to the server
    public static PrintWriter getOut() {
        return out;
    }
    
    //Returns the BufferedReader used to get messages from the server
    public static BufferedReader getIn() {
        return in;
    }
    
    //Close the connection, sends a QUIT command to the server and closes the socket
    public static void close() {
        try {
        	//Check if the client wants to quit and inform the server
            if (out != null) {
                out.println("QUIT");
                out.flush();
            }
            //Close the socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Client disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}