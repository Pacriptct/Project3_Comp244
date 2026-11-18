package bank;
import java.math.BigDecimal;
/** Checking account (T03). Rule (T04): no overdraft, so the whole balance is available. */
public class CheckingAccount extends BankAccount {
    private static final long serialVersionUID = 1L;
    public CheckingAccount(String number) { super(number,AccountType.CHECKING); }
    @Override protected BigDecimal availableFunds(){return getBalance();}
}
