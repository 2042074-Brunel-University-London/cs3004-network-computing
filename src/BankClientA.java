public class BankClientA {
    private static final String CLIENT_ID = "CLIENT_A";

    public static void main(String[] args) {
        BankClient bankClient = new BankClient(CLIENT_ID);
        bankClient.start();
    }
}
