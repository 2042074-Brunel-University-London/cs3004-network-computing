public class BankClientB {
    private static final String CLIENT_ID = "CLIENT_B";

    public static void main(String[] args) {
        BankClient bankClient = new BankClient(CLIENT_ID);
        bankClient.start();
    }
}
