//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports java.io.* for use in this source file.
import java.io.*;
//  Imports java.nio.file.* for use in this source file.
import java.nio.file.*;
/** STUDENT TASK: implement persistence read/write using BufferedReader/BufferedWriter and CSV parsing.
 * TODO T13/T15: handle malformed input, close resources, and reconstruct all object relationships.
 * Do not store real customer data: classroom project only. */
//  Declares this domain type and its inheritance or interface relationships.
public class CsvStorage implements Storage {
    //  Declares encapsulated instance state or a constant used by this class.
    private final Path file;
    //  Defines a constructor or method that provides this class’s documented behavior.
    public CsvStorage(Path directory) throws IOException {
        //  Ensures the storage directory exists before accessing its files.
        Files.createDirectories(directory);
        //  Performs this statement as part of the surrounding method or GUI setup.
        file=directory.resolve("bank-state.csv");
        //  Creates the initially empty storage file only when it is missing.
        if(!Files.exists(file)) Files.createFile(file); // instructor-provided startup setup
    //  Opens or closes the current Java block or declaration.
    }
    //  Marks this method as an implementation of an inherited or interface method.
    @Override public void save(BankState state) throws IOException {
        // TODO T13: Open the appropriate output stream with try-with-resources.
        // TODO T13: Write customers, accounts, transaction history, and ID counter.
        // TODO T12/T13: Commit the complete state safely, avoiding partial-file corruption.
        // TODO T15: Convert write errors into clear, recoverable failures.
        //  Signals that the student has not yet implemented this persistence operation.
        throw new UnsupportedOperationException("TODO: implement CsvStorage.save");
    //  Opens or closes the current Java block or declaration.
    }
    //  Marks this method as an implementation of an inherited or interface method.
    @Override public BankState load() throws IOException,ClassNotFoundException {
        // TODO T13: Treat an empty initial file as an empty BankState.
        // TODO T13: Read the chosen format using the matching input stream.
        // TODO T13: Reconstruct profiles, accounts, history, and next account ID.
        // TODO T15: Detect malformed content and report an IOException.
        //  Signals that the student has not yet implemented this persistence operation.
        throw new UnsupportedOperationException("TODO: implement CsvStorage.load");
    //  Opens or closes the current Java block or declaration.
    }
    //  Marks this method as an implementation of an inherited or interface method.
    @Override public String description(){return "CsvStorage: "+file;}
//  Opens or closes the current Java block or declaration.
}
