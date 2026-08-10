public class BankAccount {
    private String accountNumber;
    private String holderName;
    private double balance;

    public BankAccount(String accountNumber, String holderName, double initialBalance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = initialBalance;
    }

    public String deposit(double amount) {
        if (amount <= 0) return "ERROR: Deposit amount must be positive.";
        balance += amount;
        return String.format("SUCCESS: Rs.%.2f deposited. New Balance: Rs.%.2f", amount, balance);
    }

    public String withdraw(double amount) {
        if (amount <= 0) return "ERROR: Withdrawal amount must be positive.";
        if (amount > balance) return String.format("ERROR: Insufficient funds! Available: Rs.%.2f", balance);
        balance -= amount;
        return String.format("SUCCESS: Rs.%.2f withdrawn. New Balance: Rs.%.2f", amount, balance);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName()    { return holderName; }
    public double getBalance()       { return balance; }

    public String getSummary() {
        return String.format("<html><b>%s</b><br><font color='gray'>%s</font></html>", holderName, accountNumber);
    }
}