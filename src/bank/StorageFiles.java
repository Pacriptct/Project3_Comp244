package bank;
import java.io.IOException;
import java.nio.file.*;
/** Shared file helper for both storage strategies. */
final class StorageFiles {
    private StorageFiles(){}
    /** Replaces target with temp in one step where the file system supports it. */
    static void replace(Path temp,Path target) throws IOException {
        try { Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
        catch(AtomicMoveNotSupportedException e){ Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING); }
    }
}
