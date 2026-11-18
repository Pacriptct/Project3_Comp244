package bank;
import java.math.BigDecimal;
/**
 * Savings account (T03). Rules (T04): no overdraft, and each withdrawal or outgoing
 * transfer is capped at $1,000.00, which overrides the base class's "no limit" default.
 */
public class SavingsAccount extends BankAccount {
    private static final long serialVersionUID = 1L;
    public static final BigDecimal MAX_WITHDRAWAL = new BigDecimal("1000.00");
    public SavingsAccount(String number) { super(number,AccountType.SAVINGS); }
    @Override protected BigDecimal availableFunds(){return getBalance();}
    @Override protected BigDecimal withdrawalLimit(){return MAX_WITHDRAWAL;}
}
