package bank;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
/**
 * T13: saves the bank as a CSV text file, one record per line. The first column says what kind
 * of record the line is:
 * <pre>
 * BANKSTATE,1                                   format version (always first)
 * COUNTERS,nextAccount,nextTransaction
 * CUSTOMER,ssn,first,last,middle,address,dob,email,phone,sex,marital
 * ACCOUNT,ownerSsn,number,CHECKING|SAVINGS,balance
 * TXN,id,timestamp,action,source,destination,amount
 * </pre>
 * Fields that contain a comma, quote or line break are wrapped in double quotes, with inner
 * quotes doubled (standard CSV), so any text survives a save and load unchanged.
 * T15: any malformed line stops the load with the line number, and the file is left untouched.
 * Do not store real customer data: classroom project only.
 */
public class CsvStorage implements Storage {
    private static final String HEADER="BANKSTATE";
    private static final String VERSION="1";
    private final Path file;
    public CsvStorage(Path directory) throws IOException {
        Files.createDirectories(directory);
        file=directory.resolve("bank-state.csv");
        if(!Files.exists(file)) Files.createFile(file); // instructor-provided startup setup
    }

    /** Writes to a temporary file first, then swaps it in, so a failed write cannot corrupt the real file (T12). */
    @Override public void save(BankState state) throws IOException {
        Path temp=file.resolveSibling(file.getFileName()+".tmp");
        try(BufferedWriter w=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){
            line(w,HEADER,VERSION);
            line(w,"COUNTERS",String.valueOf(state.nextAccount),String.valueOf(state.nextTransaction));
            for(BankProfile p:state.profiles.values()){
                Customer c=p.getCustomer();
                line(w,"CUSTOMER",c.ssn(),c.firstName(),c.lastName(),c.middleInitial(),c.address(),
                    c.dob().toString(),c.email(),c.phone(),c.sex(),c.maritalStatus());
            }
            for(BankProfile p:state.profiles.values())
                for(BankAccount a:p.getAccounts().values())
                    line(w,"ACCOUNT",p.getCustomer().ssn(),a.getNumber(),a.getType().name(),a.getBalance().toPlainString());
            for(Transaction t:state.history)
                line(w,"TXN",t.id(),t.at().toString(),t.action(),t.source(),t.destination(),t.amount().toPlainString());
        } catch(IOException e){
            Files.deleteIfExists(temp);
            throw new IOException("Could not write "+file.getFileName()+": "+e.getMessage(),e);
        }
        StorageFiles.replace(temp,file);
    }

    /** An empty file means a brand-new bank. Otherwise every line is checked as it is read (T15). */
    @Override public BankState load() throws IOException,ClassNotFoundException {
        String text=Files.readString(file,StandardCharsets.UTF_8);
        if(text.isBlank()) return new BankState();
        List<List<String>> rows=parse(text);
        BankState state=new BankState();
        Set<String> accountNumbers=new HashSet<>();
        Set<String> txnIds=new HashSet<>();
        boolean counters=false;
        for(int i=0;i<rows.size();i++){
            List<String> r=rows.get(i);
            int lineNo=i+1;
            String kind=r.get(0);
            try {
                if(i==0){
                    if(!kind.equals(HEADER)||r.size()!=2) fail(lineNo,"file does not start with a "+HEADER+" header");
                    if(!r.get(1).equals(VERSION)) fail(lineNo,"unsupported format version "+r.get(1));
                    continue;
                }
                switch(kind){
                    case "COUNTERS" -> {
                        size(r,3,lineNo);
                        state.nextAccount=Long.parseLong(r.get(1));
                        state.nextTransaction=Long.parseLong(r.get(2));
                        counters=true;
                    }
                    case "CUSTOMER" -> {
                        size(r,11,lineNo);
                        Customer c=new Customer(r.get(2),r.get(3),r.get(4),r.get(1),r.get(5),
                            LocalDate.parse(r.get(6)),r.get(7),r.get(8),r.get(9),r.get(10));
                        if(state.profiles.containsKey(c.ssn())) fail(lineNo,"duplicate customer "+c.maskedSsn());
                        state.profiles.put(c.ssn(),new BankProfile(c));
                    }
                    case "ACCOUNT" -> {
                        size(r,5,lineNo);
                        BankProfile owner=state.profiles.get(r.get(1));
                        if(owner==null) fail(lineNo,"account "+r.get(2)+" belongs to an unknown customer");
                        String number=r.get(2);
                        if(!number.matches("\\d+")) fail(lineNo,"account number must be digits");
                        if(!accountNumbers.add(number)) fail(lineNo,"duplicate account number "+number);
                        AccountType type=AccountType.valueOf(r.get(3));
                        if(owner.get(type)!=null) fail(lineNo,"customer already has a "+type+" account");
                        BankAccount a=type==AccountType.CHECKING?new CheckingAccount(number):new SavingsAccount(number);
                        a.setBalance(new BigDecimal(r.get(4)));
                        owner.addAccount(a);
                    }
                    case "TXN" -> {
                        size(r,7,lineNo);
                        if(!txnIds.add(r.get(1))) fail(lineNo,"duplicate transaction "+r.get(1));
                        state.history.add(new Transaction(r.get(1),LocalDateTime.parse(r.get(2)),r.get(3),
                            r.get(4),r.get(5),new BigDecimal(r.get(6))));
                    }
                    default -> fail(lineNo,"unknown record type '"+kind+"'");
                }
            } catch(NumberFormatException|DateTimeParseException|ArithmeticException e){
                fail(lineNo,"bad number or date ("+e.getMessage()+")");
            } catch(IllegalArgumentException e){
                fail(lineNo,e.getMessage());
            }
        }
        if(!counters) throw new IOException(file.getFileName()+" is missing its COUNTERS line. It was left untouched.");
        return state;
    }
    @Override public String description(){return "CsvStorage: "+file;}

    private void size(List<String> r,int expected,int lineNo) throws IOException {
        if(r.size()!=expected) fail(lineNo,r.get(0)+" needs "+expected+" fields but has "+r.size());
    }
    private void fail(int record,String why) throws IOException {
        throw new IOException(file.getFileName()+" record "+record+": "+why+". The file was left untouched.");
    }

    // ----------------------------------------------------------------- CSV encoding

    private static void line(BufferedWriter w,String... fields) throws IOException {
        for(int i=0;i<fields.length;i++){
            if(i>0) w.write(',');
            w.write(escape(fields[i]));
        }
        w.newLine();
    }

    /** Quotes a field only when it needs it. */
    static String escape(String s){
        if(s==null) s="";
        if(s.contains(",")||s.contains("\"")||s.contains("\n")||s.contains("\r"))
            return "\""+s.replace("\"","\"\"")+"\"";
        return s;
    }

    /**
     * Splits CSV text into records and fields. Reads character by character so a quoted field
     * may contain commas, doubled quotes and line breaks. Blank lines are skipped.
     */
    static List<List<String>> parse(String text) throws IOException {
        List<List<String>> rows=new ArrayList<>();
        List<String> row=new ArrayList<>();
        StringBuilder field=new StringBuilder();
        boolean quoted=false, fieldStarted=false;
        for(int i=0;i<text.length();i++){
            char ch=text.charAt(i);
            if(quoted){
                if(ch=='"'){
                    if(i+1<text.length()&&text.charAt(i+1)=='"'){ field.append('"'); i++; }
                    else quoted=false;
                } else field.append(ch);
            } else if(ch=='"'){
                if(field.length()>0) throw new IOException("Malformed CSV: quote in the middle of a field (record "+(rows.size()+1)+").");
                quoted=true; fieldStarted=true;
            } else if(ch==','){
                row.add(field.toString()); field.setLength(0); fieldStarted=true;
            } else if(ch=='\n'||ch=='\r'){
                if(ch=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n') i++;
                if(fieldStarted||field.length()>0||!row.isEmpty()){ row.add(field.toString()); rows.add(row); }
                row=new ArrayList<>(); field.setLength(0); fieldStarted=false;
            } else field.append(ch);
        }
        if(quoted) throw new IOException("Malformed CSV: a quoted field is never closed.");
        if(fieldStarted||field.length()>0||!row.isEmpty()){ row.add(field.toString()); rows.add(row); }
        return rows;
    }
}
