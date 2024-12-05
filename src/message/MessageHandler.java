package message;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler {
    private final PrintWriter out;
    private final BufferedReader in;

    private static final String ANSI_DIM = "\033[2m";
    private static final String ANSI_RESET = "\033[0m";

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^\\[(.*?)\\](.*)$");

    public MessageHandler(Socket socket) throws IOException {
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(MessageType type, String content) {
        if (MessageType.NONE.equals(type)) {
            out.println(content);
            return;
        }

        out.println("[" + type.name() + "] " + content);
    }

    public void sendMessage(String content) {
        sendMessage(MessageType.MESSAGE, content);
    }

    public void sendMessage(MessageType type) {
        sendMessage(type, "");
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public ParsedMessage receiveParsedMessage() throws IOException {
        String rawMessage = receiveMessage();
        // System.out.println(ANSI_DIM + "[Parsing raw message] " + rawMessage + ANSI_RESET);
        return parseMessage(rawMessage);
    }

    public String sendInputRequest(String content) throws IOException {
        sendMessage(MessageType.INPUT, content);
        return receiveMessage();
    }

    public void close() throws IOException {
        in.close();
        out.close();
    }

    public ParsedMessage parseMessage(String message) throws IOException {
        if (message == null) {
            return new ParsedMessage(MessageType.UNKNOWN, null);
        }

        Matcher matcher = MESSAGE_PATTERN.matcher(message);

        if (!matcher.matches()) {
            return new ParsedMessage(MessageType.UNKNOWN, message);
        }

        String type = matcher.group(1).toLowerCase();
        String content = matcher.group(2).trim();
        return new ParsedMessage(MessageType.fromString(type), content);
    }

    public record ParsedMessage(MessageType type, String content) {
    }
}