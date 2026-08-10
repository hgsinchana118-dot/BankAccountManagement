import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class Bank {
    private Map<String, BankAccount> accounts = new HashMap<>();
    private int accountCounter = 1001;

    public String createAccount(String holderName, double initialDeposit) {
        if (holderName == null || holderName.trim().isEmpty())
            return "ERROR: Holder name cannot be empty.";
        if (initialDeposit < 0)
            return "ERROR: Initial deposit cannot be negative.";
        String accNo = "ACC" + accountCounter++;
        accounts.put(accNo, new BankAccount(accNo, holderName.trim(), initialDeposit));
        return "SUCCESS: Account " + accNo + " created for " + holderName.trim() + ".";
    }

    public BankAccount getAccount(String accountNumber) {
        return accounts.get(accountNumber.trim().toUpperCase());
    }

    public Collection<BankAccount> getAllAccounts() {
        return accounts.values();
    }

    public double getTotalBalance() {
        return accounts.values().stream().mapToDouble(BankAccount::getBalance).sum();
    }
}