package client;

import common.Message;
import ocsf.client.AbstractClient;

/**
 * OCSF client implementation for the Bistro system.
 * Receives messages from the server and forwards them to the ClientController.
 */
public class BistroClient extends AbstractClient {

    private ClientController controller;

    /**
     * Creates an OCSF client for the given host and port.
     *
     * @param host server host
     * @param port server port
     */
    public BistroClient(String host, int port) {
        super(host, port);
    }

    /**
     * Sets the controller that will receive incoming messages.
     *
     * @param controller client controller (facade)
     */
    public void setController(ClientController controller) {
        this.controller = controller;
    }

    /**
     * Called by OCSF when an object is received from the server.
     *
     * @param msg the received object
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (controller == null) return;

        if (msg instanceof Message m) {
            controller.deliver(m);
        } else {
            controller.deliver(Message.fail("UNEXPECTED_MESSAGE", "Server sent unsupported object: " + msg));
        }
    }

    /**
     * Called by OCSF when the connection is closed.
     */
    @Override
    protected void connectionClosed() {
        if (controller != null) {
            controller.deliver(Message.fail("DISCONNECTED", "Connection was closed"));
        }
    }
}