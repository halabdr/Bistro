package server;

public class ServerUI {

    public static void main(String[] args) {
        int port = 5555; // כמו אצלך קודם
        EchoServer server = new EchoServer(port);
        server.start();
    }
}