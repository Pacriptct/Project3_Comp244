//  Declares the Java package shared by the banking application classes.
package bank;
//  Imports javax.swing.* for use in this source file.
import javax.swing.*;
//  Imports javax.swing.border.* for use in this source file.
import javax.swing.border.*;
//  Imports java.awt.* for use in this source file.
import java.awt.*;
//  Imports java.math.BigDecimal for use in this source file.
import java.math.BigDecimal;
//  Imports java.nio.file.* for use in this source file.
import java.nio.file.*;
//  Imports java.time.LocalDate for use in this source file.
import java.time.LocalDate;
//  Imports java.util.* for use in this source file.
import java.util.*;
/** Complete GUI and event dispatch; business rules belong in BankController. */
//  Declares this domain type and its inheritance or interface relationships.
public class BankApp extends JFrame {
    //  Declares encapsulated instance state or a constant used by this class.
    private final BankController controller;
    //  Declares encapsulated instance state or a constant used by this class.
    private final JTextArea output=new JTextArea(15,72);
    //  Declares encapsulated instance state or a constant used by this class.
    private final JComboBox<String> account=new JComboBox<>(),target=new JComboBox<>();
    //  Declares encapsulated instance state or a constant used by this class.
    private final JTextField amount=new JTextField("25.00",12);
    //  Declares encapsulated instance state or a constant used by this class.
    private final JTextField first=new JTextField(13),last=new JTextField(13),middle=new JTextField(3);
    //  Declares encapsulated instance state or a constant used by this class.
    private final JTextField ssn=new JTextField(13),address=new JTextField(22),dob=new JTextField("2000-01-01",12);
    //  Declares encapsulated instance state or a constant used by this class.
    private final JTextField email=new JTextField(18),phone=new JTextField(13);
    //  Declares encapsulated instance state or a constant used by this class.
    private final JComboBox<String> sex=new JComboBox<>(new String[]{"Female","Male"});
    //  Declares encapsulated instance state or a constant used by this class.
    private final JComboBox<String> marital=new JComboBox<>(new String[]{"Single","Married","Divorced","Widowed"});
    //  Declares encapsulated instance state or a constant used by this class.
    private final JCheckBox checking=new JCheckBox("Checking",true),savings=new JCheckBox("Savings",true);
    //  Defines a constructor or method that provides this class’s documented behavior.
    public BankApp(BankController controller){
        //  Performs this statement as part of the surrounding method or GUI setup.
        super("COMP 240 | OOP Bank Simulator (Educational Demo)");this.controller=controller;
        //  Performs this statement as part of the surrounding method or GUI setup.
        setDefaultCloseOperation(EXIT_ON_CLOSE);setLayout(new BorderLayout(10,10));
        //  Creates a text label and adds it to the interface layout.
        JLabel banner=new JLabel("  CAMPUS BANK  •  Fictional data only  •  "+controller.storageDescription());
        //  Performs this statement as part of the surrounding method or GUI setup.
        banner.setFont(new Font("SansSerif",Font.BOLD,14));banner.setBorder(new EmptyBorder(12,10,12,10));add(banner,BorderLayout.NORTH);
        //  Performs this statement as part of the surrounding method or GUI setup.
        JTabbedPane tabs=new JTabbedPane();tabs.addTab("Customer signup",signupPanel());tabs.addTab("Transactions",transactionPanel());
        //  Performs this statement as part of the surrounding method or GUI setup.
        add(tabs,BorderLayout.CENTER);
        //  Performs this statement as part of the surrounding method or GUI setup.
        output.setEditable(false);output.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
        //  Performs this statement as part of the surrounding method or GUI setup.
        output.setBorder(new TitledBorder("Event log / controller response"));
        //  Makes the event log scrollable within the main window.
        add(new JScrollPane(output),BorderLayout.SOUTH);
        //  Performs this statement as part of the surrounding method or GUI setup.
        pack();setMinimumSize(new Dimension(850,690));setLocationRelativeTo(null);refresh();
        //  Performs this statement as part of the surrounding method or GUI setup.
        log("Ready. GUI is complete. Student starter operations intentionally return TODO messages.");
    //  Opens or closes the current Java block or declaration.
    }
    //  Declares encapsulated instance state or a constant used by this class.
    private JPanel signupPanel(){
        //  Creates a Swing panel and establishes its layout.
        JPanel p=new JPanel(new GridBagLayout());p.setBorder(new EmptyBorder(16,18,16,18));GridBagConstraints g=new GridBagConstraints();
        //  Performs this statement as part of the surrounding method or GUI setup.
        g.insets=new Insets(5,6,5,6);g.anchor=GridBagConstraints.WEST;g.fill=GridBagConstraints.HORIZONTAL;
        //  Performs this statement as part of the surrounding method or GUI setup.
        addRow(p,g,0,"First name",first,"Last name",last);
        //  Performs this statement as part of the surrounding method or GUI setup.
        addRow(p,g,1,"Middle initial",middle,"SSN (9 digits)",ssn);
        //  Performs this statement as part of the surrounding method or GUI setup.
        addRow(p,g,2,"Address",address,"DOB (YYYY-MM-DD)",dob);
        //  Performs this statement as part of the surrounding method or GUI setup.
        addRow(p,g,3,"Email",email,"Phone",phone);
        //  Performs this statement as part of the surrounding method or GUI setup.
        addRow(p,g,4,"Sex",sex,"Marital status",marital);
        //  Creates a Swing panel and establishes its layout.
        g.gridy=5;g.gridx=0;g.gridwidth=4;JPanel choice=new JPanel(new FlowLayout(FlowLayout.LEFT));choice.add(new JLabel("Open:"));choice.add(checking);choice.add(savings);p.add(choice,g);
        //  Connects this GUI control to its event-driven callback.
        JButton create=new JButton("Create customer account(s)");create.addActionListener(e->signup());
        //  Performs this statement as part of the surrounding method or GUI setup.
        g.gridy=6;p.add(create,g);return p;
    //  Opens or closes the current Java block or declaration.
    }
    //  Declares encapsulated instance state or a constant used by this class.
    private void addRow(JPanel p,GridBagConstraints g,int row,String a,JComponent x,String b,JComponent y){
        //  Creates a text label and adds it to the interface layout.
        g.gridy=row;g.gridwidth=1;g.gridx=0;p.add(new JLabel(a),g);g.gridx=1;p.add(x,g);
        //  Creates a text label and adds it to the interface layout.
        g.gridx=2;p.add(new JLabel(b),g);g.gridx=3;p.add(y,g);
    //  Opens or closes the current Java block or declaration.
    }
    //  Declares encapsulated instance state or a constant used by this class.
    private JPanel transactionPanel(){
        //  Creates a Swing panel and establishes its layout.
        JPanel p=new JPanel(new BorderLayout(10,10));p.setBorder(new EmptyBorder(18,18,18,18));
        //  Creates a Swing panel and establishes its layout.
        JPanel fields=new JPanel(new GridLayout(3,2,10,10));fields.add(new JLabel("Source account"));fields.add(account);
        //  Creates a text label and adds it to the interface layout.
        fields.add(new JLabel("Destination account (transfer)"));fields.add(target);
        //  Creates a text label and adds it to the interface layout.
        fields.add(new JLabel("Amount (USD)"));fields.add(amount);p.add(fields,BorderLayout.NORTH);
        //  Creates a Swing panel and establishes its layout.
        JPanel buttons=new JPanel(new GridLayout(3,3,9,9));
        //  Performs this statement as part of the surrounding method or GUI setup.
        button(buttons,"Deposit",()->money("deposit"));button(buttons,"Withdraw",()->money("withdraw"));
        //  Performs this statement as part of the surrounding method or GUI setup.
        button(buttons,"Transfer",()->money("transfer"));button(buttons,"Check balance",this::balance);
        //  Performs this statement as part of the surrounding method or GUI setup.
        button(buttons,"Show history",this::history);button(buttons,"Sorted history",this::sortedHistory);
        //  Performs this statement as part of the surrounding method or GUI setup.
        button(buttons,"Undo",()->report(controller.undo()));button(buttons,"Refresh accounts",this::refresh);
        //  Performs this statement as part of the surrounding method or GUI setup.
        button(buttons,"About",()->JOptionPane.showMessageDialog(this,"COMP 240 educational banking simulator.\nNever enter real SSNs or financial information."));
        //  Performs this statement as part of the surrounding method or GUI setup.
        p.add(buttons,BorderLayout.CENTER);return p;
    //  Opens or closes the current Java block or declaration.
    }
    //  Declares encapsulated instance state or a constant used by this class.
    private void button(JPanel p,String title,Runnable action){JButton b=new JButton(title);b.addActionListener(e->{try{action.run();}catch(Exception ex){report(OperationResult.fail(ex.getMessage()));}});p.add(b);}
    //  Declares encapsulated instance state or a constant used by this class.
    private void signup(){
        //  Begins protected execution so input or controller failures can be handled.
        try {
            //  Performs this statement as part of the surrounding method or GUI setup.
            Customer c=new Customer(first.getText().trim(),last.getText().trim(),middle.getText().trim(),
                //  Performs this statement as part of the surrounding method or GUI setup.
                ssn.getText().trim(),address.getText().trim(),LocalDate.parse(dob.getText().trim()),
                //  Performs this statement as part of the surrounding method or GUI setup.
                email.getText().trim(),phone.getText().trim(),String.valueOf(sex.getSelectedItem()),String.valueOf(marital.getSelectedItem()));
            //  Performs this statement as part of the surrounding method or GUI setup.
            report(controller.signup(c,checking.isSelected(),savings.isSelected()));refresh();
        //  Returns a safe placeholder response while the numbered controller TODO remains incomplete.
        }catch(Exception ex){report(OperationResult.fail("Signup input error: "+ex.getMessage()));}
    //  Opens or closes the current Java block or declaration.
    }
    //  Declares encapsulated instance state or a constant used by this class.
    private String selected(JComboBox<String> c){return c.getSelectedItem()==null?"":String.valueOf(c.getSelectedItem());}
    //  Declares encapsulated instance state or a constant used by this class.
    private void money(String action){
        //  Begins protected execution so input or controller failures can be handled.
        try {
            //  Performs this statement as part of the surrounding method or GUI setup.
            BigDecimal value=new BigDecimal(amount.getText().trim());String a=selected(account),b=selected(target);
            //  Performs this statement as part of the surrounding method or GUI setup.
            OperationResult r=switch(action){
                //  Selects the corresponding banking operation in the action dispatch.
                case "deposit" -> controller.deposit(a,value);
                //  Selects the corresponding banking operation in the action dispatch.
                case "withdraw" -> controller.withdraw(a,value);
                //  Handles the default branch when no other action matches.
                default -> controller.transfer(a,b,value);
            //  Performs this statement as part of the surrounding method or GUI setup.
            };report(r);refresh();
        //  Returns a safe placeholder response while the numbered controller TODO remains incomplete.
        }catch(NumberFormatException ex){report(OperationResult.fail("Amount must be numeric."));}
    //  Opens or closes the current Java block or declaration.
    }
    //  Declares encapsulated instance state or a constant used by this class.
    private void balance(){BigDecimal b=controller.balance(selected(account));log(b==null?"Account not found / TODO":"Balance: $"+b);}
    //  Declares encapsulated instance state or a constant used by this class.
    private void history(){show(controller.recentTransactions());}
    //  Declares encapsulated instance state or a constant used by this class.
    private void sortedHistory(){show(controller.sortedTransactions());}
    //  Declares encapsulated instance state or a constant used by this class.
    private void show(java.util.List<Transaction> ts){if(ts.isEmpty())log("No transactions recorded / TODO.");else ts.forEach(t->log(t.at()+" "+t.action()+" "+t.source()+" -> "+t.destination()+" $"+t.amount()));}
    //  Declares encapsulated instance state or a constant used by this class.
    private void refresh(){
        //  Performs this statement as part of the surrounding method or GUI setup.
        String old=selected(account),old2=selected(target);account.removeAllItems();target.removeAllItems();
        //  Iterates over the collection to process each relevant item.
        for(BankProfile p:controller.profiles())for(BankAccount a:p.getAccounts().values()){
            //  Performs this statement as part of the surrounding method or GUI setup.
            account.addItem(a.getNumber());target.addItem(a.getNumber());
        //  Opens or closes the current Java block or declaration.
        }
        //  Performs this statement as part of the surrounding method or GUI setup.
        account.setSelectedItem(old);target.setSelectedItem(old2);
    //  Opens or closes the current Java block or declaration.
    }
    //  Declares encapsulated instance state or a constant used by this class.
    private void report(OperationResult r){log((r.success()?"SUCCESS: ":"INFO: ")+r.message());}
    //  Declares encapsulated instance state or a constant used by this class.
    private void log(String s){output.append(s+"\n");output.setCaretPosition(output.getDocument().getLength());}
    //  Starts the GUI on Swing’s event-dispatch thread and prompts for a storage strategy.
    public static void main(String[] args){SwingUtilities.invokeLater(()->{
        //  Performs this statement as part of the surrounding method or GUI setup.
        Object[] choices={"CSV (.csv)","Java serialization (.ser)"};
        //  Performs this statement as part of the surrounding method or GUI setup.
        int selected=JOptionPane.showOptionDialog(null,"Choose ONE persistent storage format for this run.\nUse fictional customer data only.",
            //  Performs this statement as part of the surrounding method or GUI setup.
            "Storage strategy",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,choices,choices[0]);
        //  Evaluates the condition and executes its branch only when appropriate.
        if(selected<0)return;
        //  Begins protected execution so input or controller failures can be handled.
        try {
            //  Selects exactly one persistent storage implementation for this application run.
            Path data=Paths.get("data");Storage storage=selected==0?new CsvStorage(data):new SerializedStorage(data);
            //  Connects the chosen persistence implementation to the controller and GUI.
            new BankApp(new BankController(storage)).setVisible(true);
        //  Handles a failed operation and reports a readable message to the user.
        }catch(Exception ex){JOptionPane.showMessageDialog(null,"Startup error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    //  Performs this statement as part of the surrounding method or GUI setup.
    });}
//  Opens or closes the current Java block or declaration.
}
