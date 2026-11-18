//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.io.Serializable for use in this source file.
import java.io.Serializable;
//  Imports java.time.LocalDate for use in this source file.
import java.time.LocalDate;
/** Immutable identity record. Use fictional data ONLY in classroom demos. */
//  Declares this domain type and its inheritance or interface relationships.
public record Customer(String firstName, String lastName, String middleInitial,
    //  Performs this statement as part of the surrounding method or GUI setup.
    String ssn, String address, LocalDate dob, String email, String phone,
    //  Performs this statement as part of the surrounding method or GUI setup.
    String sex, String maritalStatus) implements Serializable {
    //  Declares encapsulated instance state or a constant used by this class.
    private static final long serialVersionUID = 1L;
    //  Defines a constructor or method that provides this class’s documented behavior.
    public String fullName() { return firstName + " " + (middleInitial.isBlank()?"":middleInitial+" ") + lastName; }
    //  Defines a constructor or method that provides this class’s documented behavior.
    public String maskedSsn() { return "***-**-" + ssn.substring(Math.max(0,ssn.length()-4)); }
//  Opens or closes the current Java block or declaration.
}
