package message;

public enum MessageType {
    CLIENT_ID, INPUT, MESSAGE, END, WAIT, ERROR, NONE, UNKNOWN;

    public static MessageType fromString(String type) {
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}