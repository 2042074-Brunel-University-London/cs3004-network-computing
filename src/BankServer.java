import message.MessageHandler;
import message.MessageType;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BankServer {
    private static final String SERVER_NAME = "WLFB_BANK_SERVER";
    private static final int SERVER_PORT = 4545;

    private static final int INITIAL_BALANCE = 1000;

    private static final Set<String> CLIENT_IDS = new HashSet<>(Arrays.asList("CLIENT_A", "CLIENT_B", "CLIENT_C"));


    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        boolean isListening = true;

        BankState state = new BankState(INITIAL_BALANCE, CLIENT_IDS);

        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println(SERVER_NAME + " started on port " + SERVER_PORT);

            System.out.println("Waiting for clients (" + CLIENT_IDS + ") to connect...");
            while (isListening) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from: " + clientSocket.getInetAddress());

                MessageHandler messageHandler = new MessageHandler(clientSocket);

                MessageHandler.ParsedMessage authMessage = messageHandler.receiveParsedMessage();
                String clientId = authMessage.content();

                if (authMessage.type() == MessageType.CLIENT_ID && CLIENT_IDS.contains(clientId)) {
                    System.out.println("Client[id:" + clientId + "] connected from " + clientSocket.getInetAddress());
                    new BankServerThread(clientSocket, clientId, state, messageHandler).start();
                } else {
                    System.out.println("Rejected client with invalid id: \"" + clientId + "\"");
                    messageHandler.sendMessage(MessageType.MESSAGE, "Connection refused: Invalid client ID.");
                    messageHandler.close();
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start " + SERVER_NAME + " on port " + SERVER_PORT + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                System.out.println(SERVER_NAME + " shut down.");
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
}