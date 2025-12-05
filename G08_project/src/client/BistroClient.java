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

    public static void setHost(String h) {
        host = (h == null || h.isEmpty()) ? "localhost" : h;
    }

    public static String getHost() {
        return host;
    }

    public static void connect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return; // כבר מחובר
        }
        socket = new Socket(host, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Client connected to " + host + ":" + PORT);
    }

    public static PrintWriter getOut() {
        return out;
    }

    public static BufferedReader getIn() {
        return in;
    }

    public static void close() {
        try {
            if (out != null) {
                out.println("QUIT");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Client socket closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 