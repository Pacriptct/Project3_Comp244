package bank;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
/**
 * T16: unit and integration tests. Plain Java with no libraries, so it runs anywhere:
 * <pre>
 * javac -d out src/bank/*.java test/bank/*.java
 * java -cp out bank.BankTests
 * </pre>
 * Each test uses its own temporary folder, so the real data/ folder is never touched.
 */
public class BankTests {
    private static int passed=0, failed=0;

    public static void main(String[] args) throws Exception {
        // T01 registration and validation
        test("signup opens both accounts with different numbers", BankTests::signupBoth);
        test("signup rejects bad input", BankTests::signupValidation);
        test("signup rejects duplicate SSN", BankTests::duplicateSsn);
        // T03/T04 inheritance and polymorphism
        test("accounts are the right subclasses", BankTests::subclasses);
        test("savings limit is enforced through BankAccount", BankTests::savingsLimit);
        // T05-T08 money and lookup
        test("deposit updates balance", BankTests::depositOk);
        test("deposit rejects bad amounts", BankTests::depositBad);
        test("withdraw boundaries", BankTests::withdrawBoundaries);
        test("transfer both directions", BankTests::transferBothWays);
        test("transfer rejects invalid pairs without changing balances", BankTests::transferRejected);
        test("unknown account balance is null", BankTests::unknownAccount);
        // T09-T11 data structures
        test("history is chronological and a copy", BankTests::historyOrder);
        test("sorted history is by amount and does not change history", BankTests::sortedHistory);
        test("undo reverses deposit, transfer and signup", BankTests::undoWorks);
        test("undo on empty stack fails safely", BankTests::undoEmpty);
        test("undone account number is never reused", BankTests::noReuseAfterUndo);
        // T12-T15 persistence and errors
        test("CSV state survives restart", () -> restart(StorageMode.CSV));
        test("serialized state survives restart", () -> restart(StorageMode.SERIALIZED));
        test("CSV keeps commas and quotes in text", BankTests::csvEscaping);
        test("damaged CSV is reported and left untouched", BankTests::badCsv);
        test("damaged .ser is reported and left untouched", BankTests::badSer);
        test("failed save cancels the change", BankTests::saveFailureRollsBack);

        System.out.println();
        System.out.println(passed+" passed, "+failed+" failed");
        if(failed>0) System.exit(1);
    }

    // ----------------------------------------------------------------- tests

    static void signupBoth() throws Exception {
        BankController b=bank(StorageMode.CSV);
        ok(b.signup(person("111223333"),true,true));
        BankProfile p=b.profiles().iterator().next();
        String c=p.get(AccountType.CHECKING).getNumber(), s=p.get(AccountType.SAVINGS).getNumber();
        check(!c.equals(s),"numbers differ");
        eq(new BigDecimal("0.00"),b.balance(c));
    }

    static void signupValidation() throws Exception {
        BankController b=bank(StorageMode.CSV);
        LocalDate adult=LocalDate.of(2000,1,1);
        bad(b.signup(customer("","Doe","111223333",adult,"a@b.com","5551234567"),true,true));
        bad(b.signup(customer("Ann","Doe","12345",adult,"a@b.com","5551234567"),true,true));
        bad(b.signup(customer("Ann","Doe","12345678a",adult,"a@b.com","5551234567"),true,true));
        bad(b.signup(customer("Ann","Doe","111223333",LocalDate.now().plusDays(1),"a@b.com","5551234567"),true,true));
        bad(b.signup(customer("Ann","Doe","111223333",LocalDate.now().minusYears(17),"a@b.com","5551234567"),true,true));
        bad(b.signup(customer("Ann","Doe","111223333",adult,"not-an-email","5551234567"),true,true));
        bad(b.signup(customer("Ann","Doe","111223333",adult,"a@b.com","555"),true,true));
        bad(b.signup(person("111223333"),false,false));
        // Boundary: exactly 18 today is allowed.
        ok(b.signup(customer("Ann","Doe","111223333",LocalDate.now().minusYears(18),"a@b.com","(555) 123-4567"),true,false));
    }

    static void duplicateSsn() throws Exception {
        BankController b=bank(StorageMode.CSV);
        ok(b.signup(person("111223333"),true,false));
        bad(b.signup(customer("Other","Name","111223333",LocalDate.of(1990,5,5),"x@y.com","5559998888"),false,true));
        eq(1,b.profiles().size());
    }

    static void subclasses() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        check(b.find(n[0]) instanceof CheckingAccount,"checking subclass");
        check(b.find(n[1]) instanceof SavingsAccount,"savings subclass");
    }

    static void savingsLimit() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        ok(b.deposit(n[1],money("5000")));
        ok(b.withdraw(n[1],money("1000.00")));      // exactly at the limit
        bad(b.withdraw(n[1],money("1000.01")));     // just over
        bad(b.transfer(n[1],n[0],money("1500")));   // limit applies to transfers too
        ok(b.deposit(n[0],money("5000")));
        ok(b.withdraw(n[0],money("1500")));         // checking has no such limit
    }

    static void depositOk() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        ok(b.deposit(n[0],money("100.5")));
        eq(money("100.50"),b.balance(n[0]));
    }

    static void depositBad() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        bad(b.deposit(n[0],money("0")));
        bad(b.deposit(n[0],money("-5")));
        bad(b.deposit(n[0],money("1.005")));
        bad(b.deposit(n[0],money("1000000.01")));
        bad(b.deposit("99999999",money("5")));
        bad(b.deposit("",money("5")));
        bad(b.deposit(n[0],null));
        ok(b.deposit(n[0],money("0.01")));          // smallest valid amount
        ok(b.deposit(n[0],money("1.500")));         // trailing zero is still two decimals
        eq(money("1.51"),b.balance(n[0]));
    }

    static void withdrawBoundaries() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        ok(b.deposit(n[0],money("50")));
        bad(b.withdraw(n[0],money("50.01")));
        eq(money("50.00"),b.balance(n[0]));
        ok(b.withdraw(n[0],money("50")));
        eq(money("0.00"),b.balance(n[0]));
        bad(b.withdraw(n[0],money("0.01")));
    }

    static void transferBothWays() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        ok(b.deposit(n[0],money("200")));
        ok(b.transfer(n[0],n[1],money("75")));
        eq(money("125.00"),b.balance(n[0])); eq(money("75.00"),b.balance(n[1]));
        ok(b.transfer(n[1],n[0],money("25")));
        eq(money("150.00"),b.balance(n[0])); eq(money("50.00"),b.balance(n[1]));
    }

    static void transferRejected() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] mine=open(b);
        ok(b.signup(customer("Bob","Roe","444556666",LocalDate.of(1985,3,3),"bob@x.com","5550001111"),true,true));
        String theirs=accounts(b,"444556666")[1];
        ok(b.deposit(mine[0],money("100")));
        bad(b.transfer(mine[0],mine[0],money("10")));   // same account
        bad(b.transfer(mine[0],theirs,money("10")));    // different customer
        bad(b.transfer(mine[0],mine[1],money("100.01")));// more than balance
        bad(b.transfer(mine[0],"12345",money("10")));   // unknown account
        eq(money("100.00"),b.balance(mine[0]));
        eq(money("0.00"),b.balance(mine[1]));
        eq(money("0.00"),b.balance(theirs));
    }

    static void unknownAccount() throws Exception {
        BankController b=bank(StorageMode.CSV);
        check(b.balance("00000000")==null,"null balance");
        check(b.find(null)==null,"null find");
    }

    static void historyOrder() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        ok(b.deposit(n[0],money("10")));
        ok(b.withdraw(n[0],money("3")));
        List<Transaction> h=b.recentTransactions();
        eq(4,h.size()); // two OPEN records, deposit, withdraw
        eq("DEPOSIT",h.get(2).action());
        eq("WITHDRAW",h.get(3).action());
        try { h.clear(); } catch(UnsupportedOperationException ignored){}
        eq(4,b.recentTransactions().size());
    }

    static void sortedHistory() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        ok(b.deposit(n[0],money("10")));
        ok(b.deposit(n[0],money("300")));
        ok(b.deposit(n[0],money("45")));
        List<Transaction> s=b.sortedTransactions();
        eq(money("300.00"),s.get(0).amount());
        eq(money("45.00"),s.get(1).amount());
        eq(money("10.00"),s.get(2).amount());
        eq("DEPOSIT",b.recentTransactions().get(2).action());
        eq(money("10.00"),b.recentTransactions().get(2).amount()); // original order kept
    }

    static void undoWorks() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String[] n=open(b);
        ok(b.deposit(n[0],money("100")));
        ok(b.transfer(n[0],n[1],money("40")));
        ok(b.undo());
        eq(money("100.00"),b.balance(n[0])); eq(money("0.00"),b.balance(n[1]));
        ok(b.undo());
        eq(money("0.00"),b.balance(n[0]));
        eq(2,b.recentTransactions().size());
        ok(b.undo());
        eq(0,b.profiles().size());
        eq(0,b.recentTransactions().size());
    }

    static void undoEmpty() throws Exception {
        bad(bank(StorageMode.CSV).undo());
    }

    static void noReuseAfterUndo() throws Exception {
        BankController b=bank(StorageMode.CSV);
        String first=open(b)[0];
        ok(b.undo());
        String again=open(b)[0];
        check(!first.equals(again),"new number after undo");
    }

    static void restart(StorageMode mode) throws Exception {
        Path dir=Files.createTempDirectory("bank");
        BankController b=new BankController(storage(mode,dir));
        String[] n=open(b);
        ok(b.deposit(n[0],money("250.75")));
        ok(b.transfer(n[0],n[1],money("50.25")));

        BankController reopened=new BankController(storage(mode,dir));
        eq(money("200.50"),reopened.balance(n[0]));
        eq(money("50.25"),reopened.balance(n[1]));
        eq(b.recentTransactions(),reopened.recentTransactions());
        check(reopened.find(n[1]) instanceof SavingsAccount,"type restored");
        ok(reopened.signup(customer("Cy","Poe","777889999",LocalDate.of(1970,7,7),"cy@x.com","5552223333"),true,false));
        String next=accounts(reopened,"777889999")[0];
        check(!next.equals(n[0])&&!next.equals(n[1]),"numbers continue after restart");
    }

    static void csvEscaping() throws Exception {
        Path dir=Files.createTempDirectory("bank");
        BankController b=new BankController(new CsvStorage(dir));
        String address="12 Main St, Apt \"B\", Springfield";
        ok(b.signup(new Customer("Ann","O'Neil","Q","123456789",address,LocalDate.of(1999,9,9),
            "ann@x.com","5551112222","Female","Single"),true,false));
        BankController reopened=new BankController(new CsvStorage(dir));
        eq(address,reopened.profiles().iterator().next().getCustomer().address());
        eq("O'Neil",reopened.profiles().iterator().next().getCustomer().lastName());
    }

    static void badCsv() throws Exception {
        Path dir=Files.createTempDirectory("bank");
        Path f=dir.resolve("bank-state.csv");
        String[] broken={
            "garbage",
            "BANKSTATE,1\nCOUNTERS,10000001\n",
            "BANKSTATE,1\nCOUNTERS,10000001,1\nACCOUNT,999999999,10000001,CHECKING,5.00\n",
            "BANKSTATE,1\nCOUNTERS,x,1\n",
            "BANKSTATE,1\nCOUNTERS,10000001,1\nSOMETHING,1\n",
            "BANKSTATE,1\nCOUNTERS,10000001,1\nCUSTOMER,\"unclosed\n",
        };
        for(String content:broken){
            Files.writeString(f,content);
            try { new BankController(new CsvStorage(dir)); fail("loaded broken CSV: "+content); }
            catch(IOException expected){}
            eq(content,Files.readString(f));
        }
    }

    static void badSer() throws Exception {
        Path dir=Files.createTempDirectory("bank");
        Path f=dir.resolve("bank-state.ser");
        Files.writeString(f,"definitely not serialized data");
        try { new BankController(new SerializedStorage(dir)); fail("loaded broken .ser"); }
        catch(IOException expected){}
        eq("definitely not serialized data",Files.readString(f));
    }

    static void saveFailureRollsBack() throws Exception {
        boolean[] broken={false};
        Storage flaky=new Storage(){
            public void save(BankState s) throws IOException { if(broken[0]) throw new IOException("disk full"); }
            public BankState load(){ return new BankState(); }
            public String description(){ return "test"; }
        };
        BankController b=new BankController(flaky);
        String[] n=open(b);
        ok(b.deposit(n[0],money("10")));
        broken[0]=true;
        bad(b.deposit(n[0],money("5")));
        bad(b.transfer(n[0],n[1],money("5")));
        eq(money("10.00"),b.balance(n[0]));
        eq(money("0.00"),b.balance(n[1]));
        eq(3,b.recentTransactions().size());
        bad(b.undo());                       // undo cannot be saved either
        eq(money("10.00"),b.balance(n[0]));
        broken[0]=false;
        ok(b.undo());                        // still available once saving works again
        eq(money("0.00"),b.balance(n[0]));
    }

    // ----------------------------------------------------------------- helpers

    interface Body { void run() throws Exception; }

    static void test(String name,Body body){
        try { body.run(); passed++; System.out.println("PASS  "+name); }
        catch(Throwable t){ failed++; System.out.println("FAIL  "+name+"\n      "+t); }
    }
    static BankController bank(StorageMode mode) throws IOException {
        return new BankController(storage(mode,Files.createTempDirectory("bank")));
    }
    static Storage storage(StorageMode mode,Path dir) throws IOException {
        return mode==StorageMode.CSV?new CsvStorage(dir):new SerializedStorage(dir);
    }
    /** Signs up a default customer with both accounts; returns {checking, savings}. */
    static String[] open(BankController b){
        String ssn=String.valueOf(100000000+new Random().nextInt(899999999));
        ok(b.signup(person(ssn),true,true));
        return accounts(b,ssn);
    }
    static String[] accounts(BankController b,String ssn){
        for(BankProfile p:b.profiles()) if(p.getCustomer().ssn().equals(ssn)){
            BankAccount c=p.get(AccountType.CHECKING), s=p.get(AccountType.SAVINGS);
            return new String[]{c==null?null:c.getNumber(),s==null?null:s.getNumber()};
        }
        throw new AssertionError("no customer "+ssn);
    }
    static Customer person(String ssn){
        return customer("Test","Person",ssn,LocalDate.of(1995,6,15),"test@example.com","555-123-4567");
    }
    static Customer customer(String first,String last,String ssn,LocalDate dob,String email,String phone){
        return new Customer(first,last,"",ssn,"1 Test Way",dob,email,phone,"Female","Single");
    }
    static BigDecimal money(String s){return new BigDecimal(s);}
    static void ok(OperationResult r){ if(!r.success()) throw new AssertionError("expected success: "+r.message()); }
    static void bad(OperationResult r){ if(r.success()) throw new AssertionError("expected failure: "+r.message()); }
    static void check(boolean c,String what){ if(!c) throw new AssertionError(what); }
    static void fail(String why){ throw new AssertionError(why); }
    static void eq(Object expected,Object actual){
        if(!Objects.equals(expected,actual)) throw new AssertionError("expected <"+expected+"> but was <"+actual+">");
    }
}
