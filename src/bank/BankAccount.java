package bank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
/**
 * Base class for every account (T03 inheritance, T04 encapsulation and polymorphism).
 *
 * <p>The balance is private and can only change through {@link #deposit} and {@link #withdraw},
 * so no caller can put an account into an invalid state. The class is abstract because a plain
 * "account" is never opened; only a CheckingAccount or SavingsAccount is.
 *
 * <p>Polymorphism: {@link #withdraw} calls {@link #availableFunds()} and {@link #withdrawalLimit()}.
 * Those calls are dispatched at run time to the subclass, so the controller can handle any
 * BankAccount the same way and still get checking- or savings-specific rules.
 */
public abstract class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String number;
    private final AccountType type;
    private BigDecimal balance;

    protected BankAccount(String number, AccountType type) {
        this.number=number; this.type=type; this.balance=BigDecimal.ZERO.setScale(2);
    }
    public String getNumber(){return number;}
    public AccountType getType(){return type;}
    public BigDecimal getBalance(){return balance;}

    /** Package-private: only storage code uses this, to restore a saved balance on load. */
    void setBalance(BigDecimal value){
        if(value==null||value.signum()<0) throw new IllegalArgumentException("Balance cannot be negative.");
        balance=value.setScale(2,RoundingMode.UNNECESSARY);
    }

    /** Adds a positive amount. The controller validates format before calling. */
    public void deposit(BigDecimal amount){
        requirePositive(amount);
        balance=balance.add(amount).setScale(2,RoundingMode.UNNECESSARY);
    }

    /** Removes a positive amount if this account type's rules allow it. */
    public void withdraw(BigDecimal amount){
        String problem=withdrawalProblem(amount);
        if(problem!=null) throw new IllegalArgumentException(problem);
        balance=balance.subtract(amount).setScale(2,RoundingMode.UNNECESSARY);
    }

    /** Returns null when the withdrawal is allowed, otherwise a message explaining why not. */
    public String withdrawalProblem(BigDecimal amount){
        requirePositive(amount);
        BigDecimal limit=withdrawalLimit();
        if(limit!=null&&amount.compareTo(limit)>0)
            return type+" account "+number+" allows at most $"+limit+" per withdrawal or transfer.";
        if(amount.compareTo(availableFunds())>0)
            return "Insufficient funds in "+type+" account "+number+": available $"+availableFunds()+".";
        return null;
    }

    /** How much can be taken out right now. Each subclass defines this (T04). */
    protected abstract BigDecimal availableFunds();

    /** Maximum single withdrawal, or null for no limit. Subclasses may override (T04). */
    protected BigDecimal withdrawalLimit(){return null;}

    private static void requirePositive(BigDecimal amount){
        if(amount==null||amount.signum()<=0) throw new IllegalArgumentException("Amount must be greater than zero.");
    }
}
