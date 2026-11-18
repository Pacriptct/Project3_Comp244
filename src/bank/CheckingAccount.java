//  Declares the Java package shared by the banking application classes.
package bank;
/** Inheritance demonstration: CheckingAccount extends BankAccount. */
//  Declares this domain type and its inheritance or interface relationships.
public class CheckingAccount extends BankAccount {
    //  Defines a constructor or method that provides this class’s documented behavior.
    public CheckingAccount(String number) { super(number,AccountType.CHECKING); }
    // TODO T03/T04: override a polymorphic account rule (e.g., minimum balance)
    // and explain dynamic dispatch; instructor reference uses shared base behavior.
//  Opens or closes the current Java block or declaration.
}
