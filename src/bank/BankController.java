package bank;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
/**
 * Business logic for the bank. The GUI calls these methods and shows the returned
 * {@link OperationResult}, so no banking rule lives in the GUI (MVC separation).
 *
 * <p>T03/T04: implements the {@link AccountOperations} interface, so the GUI and tests depend on
 * the contract rather than on this class's internals.
 *
 * <p>Every change to the bank goes through {@link #apply}, which:
 * <ol>
 *   <li>takes a deep snapshot of the current state,</li>
 *   <li>runs the change,</li>
 *   <li>saves with the selected Storage strategy (T12),</li>
 *   <li>if anything fails, restores the snapshot, so nothing is left half-done (T07, T15),</li>
 *   <li>on success, pushes the snapshot onto the undo stack (T11).</li>
 * </ol>
 */
public class BankController implements AccountOperations {
    /** Largest amount accepted in one operation; stops typos like 10000000. */
    static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z '\\-]{0,49}");
    private static final Pattern SSN = Pattern.compile("\\d{9}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern PHONE_CHARS = Pattern.compile("[0-9()\\-. +]+");

    private final Storage storage;
    private final BankState state=new BankState();
    /** T11: Deque used as a stack (push/pop at the head) of states from before each change. */
    private final Deque<BankState> undoStack=new ArrayDeque<>();

    /**
     * T12: loads the saved state from the selected backend at startup.
     * If the file is damaged this throws instead of starting empty, so the existing
     * data is never overwritten by mistake (T15). The GUI shows the message.
     */
    public BankController(Storage storage) throws IOException {
        this.storage=storage;
        BankState loaded;
        try { loaded=storage.load(); }
        catch(ClassNotFoundException e){ throw new IOException("Saved data uses an unknown class: "+e.getMessage(),e); }
        if(loaded!=null) copyInto(loaded,state);
        // T02: make sure the counter is past every number already in use, even if the file was edited.
        for(BankProfile p:state.profiles.values())
            for(BankAccount a:p.getAccounts().values())
                state.nextAccount=Math.max(state.nextAccount,Long.parseLong(a.getNumber())+1);
    }

    public String storageDescription(){return storage.description();}
    public Collection<BankProfile> profiles(){return Collections.unmodifiableCollection(state.profiles.values());}

    // ----------------------------------------------------------------- T08 searching

    /**
     * T08: linear search through every profile's account map. O(p) for p profiles, since each
     * profile holds at most two accounts. Returns null when not found.
     */
    public BankAccount find(String number){
        BankProfile owner=findOwner(number);
        return owner==null?null:owner.getAccounts().values().stream()
            .filter(a->a.getNumber().equals(number.trim())).findFirst().orElse(null);
    }

    /** Finds the profile that owns an account number, or null. */
    private BankProfile findOwner(String number){
        if(number==null||number.isBlank()) return null;
        String n=number.trim();
        for(BankProfile p:state.profiles.values())
            for(BankAccount a:p.getAccounts().values())
                if(a.getNumber().equals(n)) return p;
        return null;
    }

    /** T08: current balance, or null for an unknown account (never a made-up value). */
    public BigDecimal balance(String number){
        BankAccount a=find(number);
        return a==null?null:a.getBalance();
    }

    // ----------------------------------------------------------------- T01-T04 signup

    /**
     * T01-T04: validates the customer, rejects duplicates, opens the chosen accounts.
     * Duplicate rule (T01): the SSN identifies a person, so a second signup with an SSN that
     * already exists is rejected, even if the name differs.
     */
    public OperationResult signup(Customer c,boolean checking,boolean savings){
        String problem=validateCustomer(c);
        if(problem!=null) return OperationResult.fail(problem);
        if(!checking&&!savings) return OperationResult.fail("Select at least one account to open.");
        if(state.profiles.containsKey(c.ssn()))
            return OperationResult.fail("A customer with SSN "+c.maskedSsn()+" already exists.");
        return apply(()->{
            BankProfile profile=new BankProfile(c);
            List<String> opened=new ArrayList<>();
            // T03: each account is a subclass object, stored through its BankAccount supertype.
            if(checking){ BankAccount a=new CheckingAccount(nextAccountNumber()); profile.addAccount(a); opened.add("checking "+a.getNumber()); }
            if(savings){ BankAccount a=new SavingsAccount(nextAccountNumber()); profile.addAccount(a); opened.add("savings "+a.getNumber()); }
            state.profiles.put(c.ssn(),profile);
            for(BankAccount a:profile.getAccounts().values())
                record("OPEN_"+a.getType(),"NEW",a.getNumber(),BigDecimal.ZERO.setScale(2));
            return "Welcome "+c.fullName()+". Opened "+String.join(" and ",opened)+".";
        });
    }

    /** T01: returns null when valid, otherwise the first problem found. */
    static String validateCustomer(Customer c){
        if(c==null) return "Customer details are missing.";
        if(c.firstName()==null||!NAME.matcher(c.firstName()).matches())
            return "First name is required (letters, spaces, hyphens or apostrophes, max 50).";
        if(c.lastName()==null||!NAME.matcher(c.lastName()).matches())
            return "Last name is required (letters, spaces, hyphens or apostrophes, max 50).";
        if(c.middleInitial()==null||!c.middleInitial().matches("[A-Za-z]?"))
            return "Middle initial must be blank or a single letter.";
        if(c.ssn()==null||!SSN.matcher(c.ssn()).matches()) return "SSN must be exactly 9 digits, no dashes.";
        if(c.ssn().equals("000000000")) return "SSN cannot be all zeros.";
        if(c.address()==null||c.address().isBlank()) return "Address is required.";
        if(c.address().length()>120) return "Address must be 120 characters or fewer.";
        if(c.dob()==null) return "Date of birth is required.";
        LocalDate today=LocalDate.now();
        if(c.dob().isAfter(today)) return "Date of birth cannot be in the future.";
        int age=Period.between(c.dob(),today).getYears();
        if(age<18) return "Customer must be at least 18 years old.";
        if(age>120) return "Date of birth is not realistic (over 120 years ago).";
        if(c.email()==null||!EMAIL.matcher(c.email()).matches()) return "Email must look like name@example.com.";
        if(c.phone()==null||!PHONE_CHARS.matcher(c.phone()).matches()||c.phone().replaceAll("\\D","").length()!=10)
            return "Phone must contain exactly 10 digits.";
        return null;
    }

    /** T02: account numbers come from a counter that is saved with the state, so they never repeat. */
    private String nextAccountNumber(){return String.valueOf(state.nextAccount++);}

    // ----------------------------------------------------------------- T05-T07 money

    /** T05: deposit a positive amount with at most two decimal places. */
    public OperationResult deposit(String number,BigDecimal amount){
        String problem=validateAmount(amount);
        if(problem!=null) return OperationResult.fail(problem);
        BigDecimal value=amount.setScale(2);
        if(find(number)==null) return OperationResult.fail("Account not found: "+show(number));
        return apply(()->{
            BankAccount a=find(number);
            a.deposit(value);
            record("DEPOSIT","CASH",a.getNumber(),value);
            return "Deposited $"+value+" to "+a.getNumber()+". New balance $"+a.getBalance()+".";
        });
    }

    /** T06: withdrawal, checked against the account type's own rules (T04 polymorphism). */
    public OperationResult withdraw(String number,BigDecimal amount){
        String problem=validateAmount(amount);
        if(problem!=null) return OperationResult.fail(problem);
        BigDecimal value=amount.setScale(2);
        BankAccount account=find(number);
        if(account==null) return OperationResult.fail("Account not found: "+show(number));
        String rule=account.withdrawalProblem(value);
        if(rule!=null) return OperationResult.fail(rule);
        return apply(()->{
            BankAccount a=find(number);
            a.withdraw(value);
            record("WITHDRAW",a.getNumber(),"CASH",value);
            return "Withdrew $"+value+" from "+a.getNumber()+". New balance $"+a.getBalance()+".";
        });
    }

    /**
     * T07: move money between one customer's checking and savings, either direction.
     * Everything is checked before any balance changes, and apply() rolls back if a step fails.
     */
    public OperationResult transfer(String from,String to,BigDecimal amount){
        String problem=validateAmount(amount);
        if(problem!=null) return OperationResult.fail(problem);
        BigDecimal value=amount.setScale(2);
        BankAccount source=find(from), dest=find(to);
        if(source==null) return OperationResult.fail("Source account not found: "+show(from));
        if(dest==null) return OperationResult.fail("Destination account not found: "+show(to));
        if(source.getNumber().equals(dest.getNumber())) return OperationResult.fail("Source and destination must be different accounts.");
        if(findOwner(from)!=findOwner(to)) return OperationResult.fail("Transfers are only allowed between the same customer's accounts.");
        if(source.getType()==dest.getType()) return OperationResult.fail("Transfers must be between a checking and a savings account.");
        String rule=source.withdrawalProblem(value);
        if(rule!=null) return OperationResult.fail(rule);
        return apply(()->{
            BankAccount s=find(from), d=find(to);
            s.withdraw(value);
            d.deposit(value);
            record("TRANSFER",s.getNumber(),d.getNumber(),value);
            return "Transferred $"+value+" from "+s.getNumber()+" to "+d.getNumber()+".";
        });
    }

    /** T05/T06: null when the amount is valid. */
    static String validateAmount(BigDecimal amount){
        if(amount==null) return "Amount is required.";
        if(amount.signum()<=0) return "Amount must be greater than zero.";
        if(amount.stripTrailingZeros().scale()>2) return "Amount can have at most two decimal places.";
        if(amount.compareTo(MAX_AMOUNT)>0) return "Amount cannot exceed $"+MAX_AMOUNT+" per operation.";
        return null;
    }

    private static String show(String s){return s==null||s.isBlank()?"(none selected)":s;}

    // ----------------------------------------------------------------- T09-T10 history

    /** T09: adds a record to the end of the LinkedList history (chronological order). */
    private void record(String action,String source,String destination,BigDecimal amount){
        String id=String.format("T%06d",state.nextTransaction++);
        state.history.addLast(new Transaction(id,LocalDateTime.now(),action,source,destination,amount));
    }

    /**
     * T09: oldest-first copy of the history. The copy is processed as a Queue (FIFO):
     * poll() always removes the earliest remaining record, so output order is chronological.
     * Callers get a new list and cannot change the real history.
     */
    public List<Transaction> recentTransactions(){
        Queue<Transaction> queue=new LinkedList<>(state.history);
        List<Transaction> out=new ArrayList<>(queue.size());
        while(!queue.isEmpty()) out.add(queue.poll());
        return out;
    }

    /**
     * T10: history sorted by amount, largest first; ties by time (earliest first), then by ID.
     * Chronological order is already what "Show history" gives, so sorting by amount is what
     * makes this view useful. Sorts a copy, so the real history is untouched.
     * List.sort is a merge sort (TimSort): O(n log n) time, O(n) extra space, and stable.
     */
    public List<Transaction> sortedTransactions(){
        List<Transaction> copy=new ArrayList<>(state.history);
        copy.sort(Comparator.comparing(Transaction::amount).reversed()
            .thenComparing(Transaction::at)
            .thenComparing(Transaction::id));
        return copy;
    }

    // ----------------------------------------------------------------- T11 undo

    /**
     * T11: pops the most recent snapshot and restores it, which reverses balances, accounts
     * and history together. Counters are not rolled back, so an undone account number or
     * transaction ID is never handed out again (T02). The undo stack lasts for this run only.
     */
    public OperationResult undo(){
        if(undoStack.isEmpty()) return OperationResult.fail("Nothing to undo.");
        BankState current=deepCopy(state);
        BankState previous=undoStack.pop();
        previous.nextAccount=Math.max(previous.nextAccount,current.nextAccount);
        previous.nextTransaction=Math.max(previous.nextTransaction,current.nextTransaction);
        copyInto(previous,state);
        try { storage.save(state); }
        catch(IOException|RuntimeException e){
            copyInto(current,state);
            undoStack.push(previous);
            return OperationResult.fail("Undo could not be saved, nothing changed: "+e.getMessage());
        }
        return OperationResult.ok("Undid the last change.");
    }

    // ----------------------------------------------------------------- T12/T15 commit

    /**
     * Runs one change as a unit: snapshot, change, save. If the change or the save fails,
     * the snapshot is restored, so memory and the file always match (T12, T15).
     */
    private OperationResult apply(Supplier<String> change){
        BankState before=deepCopy(state);
        String message;
        try { message=change.get(); }
        catch(RuntimeException e){ copyInto(before,state); return OperationResult.fail(e.getMessage()); }
        try { storage.save(state); }
        catch(IOException|RuntimeException e){
            copyInto(before,state);
            return OperationResult.fail("Could not save, the change was cancelled: "+e.getMessage());
        }
        undoStack.push(before);
        return OperationResult.ok(message);
    }

    /** Deep copy using in-memory serialization: every account and record is a new object. */
    static BankState deepCopy(BankState s){
        try {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            try(ObjectOutputStream out=new ObjectOutputStream(bytes)){ out.writeObject(s); }
            try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){
                return (BankState)in.readObject();
            }
        } catch(IOException|ClassNotFoundException e){ throw new IllegalStateException("Could not copy bank state",e); }
    }

    /** BankState's collections are final, so restoring means replacing their contents. */
    private static void copyInto(BankState from,BankState to){
        to.profiles.clear(); to.profiles.putAll(from.profiles);
        to.history.clear(); to.history.addAll(from.history);
        to.nextAccount=from.nextAccount;
        to.nextTransaction=from.nextTransaction;
    }
}
