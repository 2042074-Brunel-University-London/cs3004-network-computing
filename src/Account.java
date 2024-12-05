public class Account {
    private final String id;
    private double balance;

    public Account(String id, double initialBalance) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }

        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        this.id = id.trim().toLowerCase();
        this.balance = initialBalance;
    }

    public String getId() {
        return this.id;
    }

    public double getBalance() {
        return this.balance;
    }

    public double deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        this.balance += amount;
        return this.balance;
    }

    public double withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        this.balance -= amount;
        return this.balance;
    }

    public void transfer(double amount, Account destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Recipient account cannot be null");
        }

        if (this == destination) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        if (amount > this.balance) {
            System.out.println("Warning: amount sent is greater than sender's balance");
        }

        this.withdraw(amount);
        destination.deposit(amount);
    }

    @Override
    public String toString() {
        return "Account [id=" + id + ", balance=" + balance + "]";
    }
}
