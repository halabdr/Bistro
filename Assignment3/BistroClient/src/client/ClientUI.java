package client;

import common.ChatIF;

public class ClientUI implements ChatIF {
    @Override
    public void display(String message) {
        System.out.println(message);
    }
}
