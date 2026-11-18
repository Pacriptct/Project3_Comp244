//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.io.Serializable for use in this source file.
import java.io.Serializable;
//  Imports java.util.* for use in this source file.
import java.util.*;
/** Aggregate root: one customer owns zero, one, or two accounts. */
//  Declares this domain type and its inheritance or interface relationships.
public class BankProfile implements Serializable {
    //  Declares encapsulated instance state or a constant used by this class.
    private static final long serialVersionUID=1L;
    //  Declares encapsulated instance state or a constant used by this class.
    private final Customer customer;
    //  Declares encapsulated instance state or a constant used by this class.
    private final EnumMap<AccountType,BankAccount> accounts=new EnumMap<>(AccountType.class);
    //  Defines a constructor or method that provides this class’s documented behavior.
    public BankProfile(Customer customer){this.customer=customer;}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public Customer getCustomer(){return customer;}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public Map<AccountType,BankAccount> getAccounts(){return Collections.unmodifiableMap(accounts);}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public void addAccount(BankAccount account){accounts.put(account.getType(),account);}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public BankAccount get(AccountType type){return accounts.get(type);}
//  Opens or closes the current Java block or declaration.
}
