//  Declares the Java package shared by the banking application classes.
package bank;
/** Result object keeps GUI separate from business logic and exception details. */
//  Declares this domain type and its inheritance or interface relationships.
public record OperationResult(boolean success, String message) {
    //  Defines a constructor or method that provides this class’s documented behavior.
    public static OperationResult ok(String message){return new OperationResult(true,message);}
    //  Defines a constructor or method that provides this class’s documented behavior.
    public static OperationResult fail(String message){return new OperationResult(false,message);}
//  Opens or closes the current Java block or declaration.
}
