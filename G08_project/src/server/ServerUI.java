package server;

public class ServerUI {
	//main for serverUI
    public static void main(String[] args) {
        int port = 5555; // כמו אצלך קודם
        EchoServer server = new EchoServer(port);
        server.start();
    }
}