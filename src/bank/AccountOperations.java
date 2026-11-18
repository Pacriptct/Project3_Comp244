//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.math.BigDecimal for use in this source file.
import java.math.BigDecimal;
//  Imports java.util.List for use in this source file.
import java.util.List;
/** Contract for banking operations. */
//  Declares a contract that implementing classes must satisfy.
public interface AccountOperations {
    //  Performs this statement as part of the surrounding method or GUI setup.
    OperationResult signup(Customer customer, boolean checking, boolean savings);
    //  Performs this statement as part of the surrounding method or GUI setup.
    OperationResult deposit(String accountNumber, BigDecimal amount);
    //  Performs this statement as part of the surrounding method or GUI setup.
    OperationResult withdraw(String accountNumber, BigDecimal amount);
    //  Performs this statement as part of the surrounding method or GUI setup.
    OperationResult transfer(String from, String to, BigDecimal amount);
    //  Performs this statement as part of the surrounding method or GUI setup.
    BigDecimal balance(String accountNumber);
    //  Performs this statement as part of the surrounding method or GUI setup.
    List<Transaction> recentTransactions();
//  Opens or closes the current Java block or declaration.
}
