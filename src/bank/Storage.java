//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.io.IOException for use in this source file.
import java.io.IOException;
/** Strategy pattern: implementations use CSV or Java serialization. */
//  Declares a contract that implementing classes must satisfy.
public interface Storage {
    //  Performs this statement as part of the surrounding method or GUI setup.
    void save(BankState state) throws IOException;
    //  Performs this statement as part of the surrounding method or GUI setup.
    BankState load() throws IOException, ClassNotFoundException;
    //  Performs this statement as part of the surrounding method or GUI setup.
    String description();
//  Opens or closes the current Java block or declaration.
}
