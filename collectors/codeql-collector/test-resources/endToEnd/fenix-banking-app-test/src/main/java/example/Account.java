package example;

public class Account extends Account_Base {
    public Account(double initialBalance, User user) {
        setBalance(initialBalance);
        setUser(user);
    }
}
