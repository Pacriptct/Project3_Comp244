//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.io.Serializable for use in this source file.
import java.io.Serializable;
//  Imports java.math.BigDecimal for use in this source file.
import java.math.BigDecimal;
/** Mutable bank account; balance changes only through controller operations. */
//  Declares this domain type and its inheritance or interface relationships.
public class BankAccount implements Serializable {
    //  Declares encapsulated instance state or a constant used by this class.
    private static final long serialVersionUID = 1L;
    //  Declares encapsulated instance state or a constant used by this class.
    private final String number;
    //  Declares encapsulated instance state or a constant used by this class.
    private final AccountType type;
    //  Declares encapsulated instance state or a constant used by this class.
    private BigDecimal balance;
    //  Defines a constructor or method that provides this class’s documented behavior.
    public BankAccount(String number, AccountType type) {
        //  Performs this statement as part of the surrounding method or GUI setup.
        this.number=number; this.type=type; this.balance=BigDecimal.ZERO.setScale(2);
    //  Opens or closes the current Java block or declaration.
    }
    //  Defines a constructor or method that provides this class’s documented behavior.
    public String getNumber(){return number;}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public AccountType getType(){return type;}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public BigDecimal getBalance(){return balance;}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public void setBalance(BigDecimal value){balance=value;}
//  Opens or closes the current Java block or declaration.
}
