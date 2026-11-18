//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.io.Serializable for use in this source file.
import java.io.Serializable;
//  Imports java.util.* for use in this source file.
import java.util.*;
/** Persisted state. LinkedList supplies chronological history and Queue behavior. */
//  Declares this domain type and its inheritance or interface relationships.
public class BankState implements Serializable {
    //  Declares encapsulated instance state or a constant used by this class.
    private static final long serialVersionUID=1L;
    //  Defines a constructor or method that provides this class’s documented behavior.
    public final Map<String,BankProfile> profiles=new LinkedHashMap<>();
    //  Defines a constructor or method that provides this class’s documented behavior.
    public final LinkedList<Transaction> history=new LinkedList<>();
    //  Defines a constructor or method that provides this class’s documented behavior.
    public long nextAccount=10000001;
    //  Defines a constructor or method that provides this class’s documented behavior.
    public long nextTransaction=1;
//  Opens or closes the current Java block or declaration.
}
