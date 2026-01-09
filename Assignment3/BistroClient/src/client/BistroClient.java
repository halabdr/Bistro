package client;

import ocsf.client.AbstractClient;

public class BistroClient extends AbstractClient {

    private ClientController controller;

    public BistroClient(String host, int port) {
        super(host, port);
    }

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (controller != null) controller.deliver(msg);
    }

    @Override
    protected void connectionClosed() {
        if (controller != null) controller.deliver("DISCONNECTED:closed");
    }
}
