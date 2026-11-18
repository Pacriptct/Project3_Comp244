package bank;
import java.io.*;
import java.nio.file.*;
/**
 * T14: saves the whole BankState object graph with Java serialization.
 * Every class in the graph (BankState, BankProfile, Customer, accounts, Transaction) implements
 * Serializable, so one writeObject call stores customers, accounts, history and counters.
 * Do not store real customer data: classroom project only.
 */
public class SerializedStorage implements Storage {
    private final Path file;
    public SerializedStorage(Path directory) throws IOException {
        Files.createDirectories(directory);
        file=directory.resolve("bank-state.ser");
        if(!Files.exists(file)) Files.createFile(file); // instructor-provided startup setup
    }

    /** Writes to a temporary file first, then swaps it in, so a crash mid-write cannot corrupt the real file (T12). */
    @Override public void save(BankState state) throws IOException {
        Path temp=file.resolveSibling(file.getFileName()+".tmp");
        try(ObjectOutputStream out=new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(temp)))){
            out.writeObject(state);
        } catch(IOException e){
            Files.deleteIfExists(temp);
            throw new IOException("Could not write "+file.getFileName()+": "+e.getMessage(),e);
        }
        StorageFiles.replace(temp,file);
    }

    /** An empty file means a brand-new bank. Anything unreadable is reported, never ignored (T15). */
    @Override public BankState load() throws IOException,ClassNotFoundException {
        if(!Files.exists(file)||Files.size(file)==0) return new BankState();
        try(ObjectInputStream in=new ObjectInputStream(new BufferedInputStream(Files.newInputStream(file)))){
            Object o=in.readObject();
            if(!(o instanceof BankState s)) throw new IOException(file.getFileName()+" does not contain bank data.");
            return s;
        } catch(StreamCorruptedException|EOFException|InvalidClassException e){
            throw new IOException(file.getFileName()+" is damaged or from an incompatible version ("+e.getMessage()+"). It was left untouched.",e);
        }
    }
    @Override public String description(){return "SerializedStorage: "+file;}
}
