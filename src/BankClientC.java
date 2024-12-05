public class BankClientC {
    private static final String CLIENT_ID = "CLIENT_C";

    public static void main(String[] args) {
        BankClient bankClient = new BankClient(CLIENT_ID);
        bankClient.start();
    }
}
