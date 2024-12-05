import message.MessageHandler;
import message.MessageType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class BankState {
    private static final String ANSI_DIM = "\033[2m";
    private static final String ANSI_RESET = "\033[0m";

    private final HashMap<String, Account> accounts = new HashMap<>();
    private final ReentrantLock lock;

    public BankState(double initialBalance, Set<String> accountIds) {
        for (String accountId : accountIds) {
            accounts.put(accountId.toLowerCase(), new Account(accountId, initialBalance));
        }
        this.lock = new ReentrantLock();
    }

    public double addMoney(String accountId, double amount) {
        Account account = getAccount(accountId);

        double newBalance = account.deposit(amount);
        System.out.println("Added " + amount + " to account " + accountId + ". New balance: " + newBalance);

        return newBalance;
    }

    public double subMoney(String accountId, double amount) {
        Account account = getAccount(accountId);

        double newBalance = account.withdraw(amount);
        System.out.println("Subtracted " + amount + " from account " + accountId + ". New balance: " + newBalance);

        return newBalance;
    }

    public double transferMoney(String fromAccountId, String toAccountId, double amount) {
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);

        fromAccount.transfer(amount, toAccount);
        System.out.println("Transferred " + amount + " from account " + fromAccountId + "(" + fromAccount.getBalance() + ") to " + toAccountId + "(" + toAccount.getBalance() + ")");

        return fromAccount.getBalance();
    }

    private Account getAccount(String accountId) {
        Account account = accounts.get(accountId.toLowerCase());
        if (account == null) {
            throw new IllegalArgumentException("Invalid account id: " + accountId);
        }
        return account;
    }

    private void acquireLock(MessageHandler messageHandler, String threadName, String input) {
        System.out.println(ANSI_DIM + "[LOCK] " + threadName + " attempting to acquire the lock for input: " + input + ANSI_RESET);

        boolean isWaitForLockMessageSent = false;
        while (!lock.tryLock()) {
            if (!isWaitForLockMessageSent) {
                System.out.println(ANSI_DIM + "[LOCK] " + threadName + " is waiting for the lock" + ANSI_RESET);
                messageHandler.sendMessage(MessageType.WAIT, "Waiting for lock to be released...");
                isWaitForLockMessageSent = true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println(ANSI_DIM + "[LOCK] " + threadName + " acquired the lock" + ANSI_RESET);
    }

    private void releaseLock(String threadName) {
        System.out.println(ANSI_DIM + "[LOCK] " + threadName + " is releasing the lock" + ANSI_RESET);
        lock.unlock();
    }

    public void processInput(String threadName, String input, MessageHandler messageHandler) throws IOException {
        switch (input.toLowerCase()) {
            case "add": {
                try {
                    acquireLock(messageHandler, threadName, input);

                    double amount = Double.parseDouble(messageHandler.sendInputRequest("Enter amount to add:"));
                    System.out.println("Amount entered: " + amount);

                    double newBalance = this.addMoney(threadName, amount);
                    messageHandler.sendMessage("Added " + amount + " to account " + threadName + ". New balance: " + newBalance);

                    releaseLock(threadName);
                } catch (IllegalArgumentException e) {
                    messageHandler.sendMessage(MessageType.ERROR, e.getMessage());
                }
                break;
            }
            case "subtract":
            case "sub": {
                try {
                    acquireLock(messageHandler, threadName, input);

                    double amount = Double.parseDouble(messageHandler.sendInputRequest("Enter amount to subtract:"));
                    System.out.println("Amount entered: " + amount);

                    double newBalance = this.subMoney(threadName, amount);
                    messageHandler.sendMessage("Subtracted " + amount + " from account " + threadName + ". New balance: " + newBalance);

                    releaseLock(threadName);
                } catch (IllegalArgumentException e) {
                    messageHandler.sendMessage(MessageType.ERROR, e.getMessage());
                }
                break;
            }
            case "transfer": {
                try {
                    acquireLock(messageHandler, threadName, input);

                    String receiverId = messageHandler.sendInputRequest("Enter the receiver's account ID:");
                    System.out.println("Receiver entered: " + receiverId);

                    double amount = Double.parseDouble(messageHandler.sendInputRequest("Enter amount to transfer:"));
                    System.out.println("Amount entered: " + amount);

                    double senderBalance = this.transferMoney(threadName, receiverId, amount);
                    messageHandler.sendMessage("Transferred " + amount + " from " + threadName + " to " + receiverId + ". Sender's new balance: " + senderBalance);

                    releaseLock(threadName);
                } catch (IllegalArgumentException e) {
                    messageHandler.sendMessage(MessageType.ERROR, e.getMessage());
                }
                break;
            }
            default: {
                messageHandler.sendMessage(MessageType.ERROR, "Unknown command \"" + input + "\". Supported commands are: add, sub, transfer.");
                break;
            }
        }
        messageHandler.sendMessage(MessageType.END);
    }
}

