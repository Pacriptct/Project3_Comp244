//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.io.Serializable for use in this source file.
import java.io.Serializable;
//  Imports java.math.BigDecimal for use in this source file.
import java.math.BigDecimal;
//  Imports java.time.LocalDateTime for use in this source file.
import java.time.LocalDateTime;
/** Transaction audit record; includes transfers, deposits and withdrawals. */
//  Declares this domain type and its inheritance or interface relationships.
public record Transaction(String id, LocalDateTime at, String action,
    //  Performs this statement as part of the surrounding method or GUI setup.
    String source, String destination, BigDecimal amount) implements Serializable {
    //  Declares encapsulated instance state or a constant used by this class.
    private static final long serialVersionUID=1L;
//  Opens or closes the current Java block or declaration.
}
