import message.MessageHandler;
import message.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class BankClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 4545;

    private static final String ANSI_DIM = "\033[2m";
    private static final String ANSI_RESET = "\033[0m";

    private final String clientId;

    public BankClient(String clientId) {
        this.clientId = clientId;
    }

    private String getUserInput(BufferedReader userInput, String prompt) throws IOException {
        System.out.println(prompt);
        System.out.print("> ");
        return userInput.readLine();
    }

    private boolean handleServerInput(BufferedReader userInput, MessageHandler.ParsedMessage fromServer, MessageHandler messageHandler) throws IOException {
        String serverMessage = fromServer.content();
        MessageType messageType = fromServer.type();

        if ((MessageType.UNKNOWN.equals(messageType)) && serverMessage == null) {
            return true;
        }

        switch (messageType) {
            case INPUT: {
                System.out.println(ANSI_DIM + "[Server input message] " + serverMessage + ANSI_RESET);
                String input = getUserInput(userInput, serverMessage);

                messageHandler.sendMessage(MessageType.NONE, input);
                break;
            }
            case MESSAGE: {
                System.out.println(ANSI_DIM + "[Server message] " + serverMessage + ANSI_RESET);
                break;
            }
            case ERROR: {
                System.out.println(ANSI_DIM + "[Error] " + serverMessage + ANSI_RESET);
                break;
            }
            case END: {
                return true;
            }
            case UNKNOWN: {
                System.out.println(ANSI_DIM + "[Unknown server message type] " + serverMessage + ANSI_RESET);
            }
            default: {
                System.out.println(ANSI_DIM + "[" + messageType + "] " + serverMessage + ANSI_RESET);
                break;
            }
        }
        return false;
    }

    public void start() {
        try (
                Socket clientSocket = new Socket();
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            clientSocket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), 5000);

            MessageHandler messageHandler = new MessageHandler(clientSocket);

            messageHandler.sendMessage(MessageType.CLIENT_ID, clientId); // Fire clientId for server identification
            System.out.println("Client " + clientId + " connected to server at " + SERVER_HOST + ":" + SERVER_PORT);

            while (true) {
                String command = getUserInput(userInput, "Enter command (add, sub, transfer or quit):");

                if (command.equalsIgnoreCase("quit")) {
                    System.out.println("Client " + clientId + " exiting.");
                    break;
                }

                messageHandler.sendMessage(MessageType.NONE, command);

                MessageHandler.ParsedMessage fromServer;
                try {
                    while ((fromServer = messageHandler.receiveParsedMessage()) != null) {
                        if (handleServerInput(userInput, fromServer, messageHandler)) {
                            break;
                        }
                    }

                    if (clientSocket.isClosed() || !clientSocket.isConnected()) {
                        System.err.println("Unable to connect to the server. Terminating session.");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    System.err.println("Server response timed out. Terminating session.");
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Connection attempt timed out. Is the server running?");
        } catch (ConnectException e) {
            System.err.println("Unable to connect to the server. It might be down.");
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_HOST);
        } catch (IOException e) {
            System.err.println("I/O error in communication with server: " + e.getMessage());
        }
    }
}
