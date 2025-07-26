import example.Account;
import example.User;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;

public class BankApp {
    public static void main(String[] args) {
        try {
            createUser("Alice", 100.0);
            deposit("Alice", 50.0);
            withdraw("Alice", 30.0);
            showBalance("Alice");
        } finally {
            FenixFramework.shutdown();
        }
    }

    @Atomic
    public static void createUser(String name, double initialDeposit) {
        DomainRoot root = FenixFramework.getDomainRoot();
        User user = new User(name, root);
        new Account(initialDeposit, user);
    }

    @Atomic
    public static void deposit(String userName, double amount) {
        DomainRoot root = FenixFramework.getDomainRoot();
        for (User u : root.getUsersSet()) {
            if (u.getName().equals(userName)) {
                // For simplicity, use the first account
                Account acc = u.getAccountsSet().iterator().next();
                acc.setBalance(acc.getBalance() + amount);
                return;
            }
        }
    }

    @Atomic
    public static void withdraw(String userName, double amount) {
        DomainRoot root = FenixFramework.getDomainRoot();
        for (User u : root.getUsersSet()) {
            if (u.getName().equals(userName)) {
                Account acc = u.getAccountsSet().iterator().next();
                acc.setBalance(acc.getBalance() - amount);
                return;
            }
        }
    }

    @Atomic
    public static void showBalance(String userName) {
        DomainRoot root = FenixFramework.getDomainRoot();
        for (User u : root.getUsersSet()) {
            if (u.getName().equals(userName)) {
                Account acc = u.getAccountsSet().iterator().next();
                System.out.println("Balance for " + userName + ": " + acc.getBalance());
                return;
            }
        }
    }
}