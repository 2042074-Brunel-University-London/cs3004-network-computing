import message.MessageHandler;

import java.net.*;
import java.io.*;


public class BankServerThread extends Thread {
    private Socket clientSocket = null;
    private final BankState state;
    private final String clientId;

    private final MessageHandler messageHandler;

    public BankServerThread(Socket clientSocket, String clientId, BankState state, MessageHandler messageHandler) {
        super(clientId);

        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.state = state;
        this.messageHandler = messageHandler;
    }

    public void run() {
        System.out.println("Client[id:" + clientId + "] is initialising.");

        try {
            String inputLine;
            while ((inputLine = messageHandler.receiveMessage()) != null) {
                state.processInput(clientId, inputLine, messageHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client[id:" + clientId + "] has disconnected.");
            } catch (IOException e) {
                System.err.println("Error closing socket for Client[id:" + clientId + "]: " + e.getMessage());
            }
        }
    }
}