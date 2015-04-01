/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LawnMowingPackage;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 *  This is a database application made for a Lawn Mowing Service Organization that keeps track of all technicians 
 * and customers.
 * This was designed with the Administrator in mind; the admin can create, update, and retreive data from the database.
 * @author Alex Ma
 */
public class StartGUI extends javax.swing.JFrame {

    /**
     * Creates new form StartGUI
     * initialize GUI components
     */
    public StartGUI() {
        initComponents();
    }

    //JDBC connection information
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DATABASE_URL = "jdbc:mysql://localhost/lawn_mowing";
    static final String USERNAME = "root";
    static final String PASSWORD = "";

    //queries 
    public static final String DEFAULT_QUERY = "SELECT FullName, Company_Name FROM Technician";
    public static final String NEWCUST_QUERY = "SELECT FullName, Address FROM Customer";
    public static final String RETURNTECH_QUERY = "SELECT DISTINCT Technician.*, (SELECT Count(*) FROM Customer "
            + "WHERE Customer.Technician_ID = Technician.Technician_ID) FROM Customer, Technician "
            + "WHERE Technician.Technician_ID = Customer.Technician_ID;";
    public static final String RETURNCUST_QUERY = "SELECT * FROM Customer";
    
    private ResultSetTableModel tableModel;
    private ResultSetTableModel newCust_tableModel;
    private ResultSetTableModel returnTech_tableModel;
    private ResultSetTableModel returnCust_tableModel;

    /**
     * Helper method used to add items of query to array; used for JComboBox
     *
     * @param query String of query used to add items to array
     * @param firstItem index 0 item of JComboBox
     * @return the items in an Array
     */
    public String[] addToArray(String query, String firstItem) {
        ArrayList<String> optionsList = new ArrayList<String>();

        try {
            Class.forName(JDBC_DRIVER).newInstance();
            Connection conn = DriverManager.getConnection(DATABASE_URL, USERNAME,
                    PASSWORD);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                optionsList.add(rs.getString(1));
            }
            rs.close();
            st.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(optionsList);
        if (!firstItem.equals("")) {
            optionsList.add(0, firstItem);
        }
        String[] list = optionsList.toArray(new String[optionsList.size()]);

        return list;
    }

    /**
     * Getter method to retrieve the ID of the provided Technician name.
     *
     * @param fullName Name of Technician
     * @return The ID of the Technician
     */
    public int getTechID(String fullName) {
        int techId = 0;
        try {
            Class.forName(JDBC_DRIVER);
            Connection getTechId_conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);

            PreparedStatement getTechIDquery = getTechId_conn.prepareStatement("SELECT Technician_ID FROM Technician WHERE FullName = ?");
            getTechIDquery.setString(1, fullName);

            ResultSet rs = getTechIDquery.executeQuery();

            while (rs.next()) {
                techId = rs.getInt("Technician_ID");
            }
            rs.close();
            getTechIDquery.close();
            getTechId_conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return techId;
    }

    /**
     * Getter method to retrieve the ID of the provided Customer name.
     *
     * @param fullName Name of Customer
     * @return The ID of the Customer
     */
    public int getCustID(String fullName) {
        int custId = 0;
        try {
            Class.forName(JDBC_DRIVER);
            Connection getCustId_conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);

            PreparedStatement getCustIDquery = getCustId_conn.prepareStatement("SELECT Customer_ID FROM Customer WHERE FullName = ?");
            getCustIDquery.setString(1, fullName);

            ResultSet rs = getCustIDquery.executeQuery();

            while (rs.next()) {
                custId = rs.getInt("Customer_ID");
            }

            rs.close();
            getCustIDquery.close();
            getCustId_conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return custId;
    }

    /**
     * method used to find technician with least amount of customers
     * useful because i want to have all technicians have balanced of customers at any time.
     * e.g. technician A having 4 customers and technician  B having 1 customer gives an
     * unfair advantage to technician A because he earns more money but a disadvantage because he's being overworked. 
     * @return a String, the name of Technician with least amount of customers in database.
     */
    public String getLowestCustQuery() {
        ArrayList<Integer> getIdList = new ArrayList<Integer>();
        ArrayList<Integer> countZero = new ArrayList<Integer>();
        ArrayList<Integer> countOne = new ArrayList<Integer>();
        ArrayList<Integer> countTwo = new ArrayList<Integer>();
        ArrayList<Integer> countThree = new ArrayList<Integer>();
        ArrayList<Integer> countFour = new ArrayList<Integer>();
        ArrayList<Integer> countFive = new ArrayList<Integer>();
        String getTechQuery = "SELECT FullName FROM Technician WHERE Technician_ID = ";
        int count = 0;
        try {
            Class.forName(JDBC_DRIVER);
            Connection newCust_conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
            Statement st = newCust_conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT Technician_ID FROM Technician;");

            //get all technician id's in database
            while (rs.next()) {
                getIdList.add(rs.getInt("Technician_ID"));
            }
            //count the number of customers each technician has.
            PreparedStatement countCustQuery = newCust_conn.prepareStatement("SELECT count(*) FROM Customer WHERE Technician_ID = ?");
            ResultSet countRs = null;
            for (int i = 0; i < getIdList.size(); i++) {

                int id = getIdList.get(i);
                countCustQuery.setInt(1, id);
                countRs = countCustQuery.executeQuery();

                while (countRs.next()) {
                    count = countRs.getInt("count(*)");
                }

                //add technicians with lowest in list
                if (count == 0) {
                    countZero.add(id);
                } else if (count == 1) {
                    countOne.add(id);
                } else if (count == 2) {
                    countTwo.add(id);
                } else if (count == 3) {
                    countThree.add(id);
                } else if (count == 4) {
                    countFour.add(id);
                } else if (count == 5) {
                    countFive.add(id);
                }
            }

            rs.close();
            countRs.close();
            st.close();
            countCustQuery.close();
            newCust_conn.close();

            if (!countZero.isEmpty()) {
                for (int i = 0; i < countZero.size(); i++) {
                    int id = countZero.get(i);
                    //if last element in arrayList
                    if (i + 1 == countZero.size()) {
                        getTechQuery += id + ";";
                    } else {
                        getTechQuery += id + " OR Technician_ID = ";
                    }
                }
            } else if (!countOne.isEmpty()) {
                for (int i = 0; i < countOne.size(); i++) {
                    int id = countOne.get(i);
                    //if last element in arrayList
                    if (i + 1 == countOne.size()) {
                        getTechQuery += id + ";";
                    } else {
                        getTechQuery += id + " OR Technician_ID = ";
                    }
                }
            } else if (!countTwo.isEmpty()) {
                for (int i = 0; i < countTwo.size(); i++) {
                    int id = countTwo.get(i);
                    //if last element in arrayList
                    if (i + 1 == countTwo.size()) {
                        getTechQuery += id + ";";
                    } else {
                        getTechQuery += id + " OR Technician_ID = ";
                    }
                }

            } else if (!countThree.isEmpty()) {
                for (int i = 0; i < countThree.size(); i++) {
                    int id = countThree.get(i);
                    //if last element in arrayList
                    if (i + 1 == countThree.size()) {
                        getTechQuery += id + ";";
                    } else {
                        getTechQuery += id + " OR Technician_ID = ";
                    }
                }

            } else if (!countFour.isEmpty()) {
                for (int i = 0; i < countFour.size(); i++) {
                    int id = countFour.get(i);
                    //if last element in arrayList
                    if (i + 1 == countFour.size()) {
                        getTechQuery += id + ";";
                    } else {
                        getTechQuery += id + " OR Technician_ID = ";
                    }
                }
            } else if (!countFive.isEmpty()) {
                DefaultComboBoxModel model = new DefaultComboBoxModel();
                model.removeAllElements();
                jComboBox8.setModel(model);
                jComboBox8.addItem("Select Technician");
                return "Select Technician";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getTechQuery;
    }

/**
 * Used for retrieving information of a JTable which is useful later when you want to transfer onto PDF file.
 * @param table JTable you want to retrieve date of
 * @return a 2-D array of the information in a JTable 
 */
    public String[][] getTableData(JTable table) {

        ResultSetTableModel model = (ResultSetTableModel) table.getModel();
        int totalRows = model.getRowCount();
        int totalColumns = model.getColumnCount();
        String[][] tableData = new String[totalRows][totalColumns];
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalColumns; j++) {
                tableData[i][j] = model.getValueAt(i, j).toString();
            }
        }

        return tableData;
    }
    
/**
 * 
 * @param textField the textField you want to limit the length of
 * @param pwLength the maximum length you want to limit
 */
    public void limitPwLength(JTextField textField, int pwLength) {
        PlainDocument document = (PlainDocument) textField.getDocument();
        document.setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String string = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;

                if (string.length() <= pwLength) {
                    super.replace(fb, offset, length, text, attrs); //To change body of generated methods, choose Tools | Templates.
                }
            }

        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cardLayoutPanel = new javax.swing.JPanel();
        mainPanel = new javax.swing.JPanel();
        techButton = new javax.swing.JButton();
        custButton = new javax.swing.JButton();
        homeLabel = new javax.swing.JLabel();
        techPanel = new javax.swing.JPanel();
        techLabel = new javax.swing.JLabel();
        newTechButton = new javax.swing.JButton();
        returnTechButton = new javax.swing.JButton();
        techHomeBtn = new javax.swing.JButton();
        newTechPanel = new javax.swing.JPanel();
        phoneNumTextField = new javax.swing.JTextField();
        t_nameTextField = new javax.swing.JTextField();
        t_submitButton = new javax.swing.JButton();
        t_cancelButton1 = new javax.swing.JButton();
        compNameTextField = new javax.swing.JTextField();
        compAddrTextField = new javax.swing.JTextField();
        newTech_homeBtn = new javax.swing.JButton();
        costTextField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        existTechTable = new javax.swing.JTable();
        existTechLabel = new javax.swing.JLabel();
        newTechLabel = new javax.swing.JLabel();
        returnTechPanel = new javax.swing.JPanel();
        selectTechCombo = new javax.swing.JComboBox();
        workWeekBtn = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        returnTech_updateBtn = new javax.swing.JButton();
        returnTech_costTextField = new javax.swing.JTextField();
        returnTech_phoneNumTextField = new javax.swing.JTextField();
        returnTech_compAddrTextField = new javax.swing.JTextField();
        selectYearCombo = new javax.swing.JComboBox();
        selectMonthCombo = new javax.swing.JComboBox();
        selectDateCombo = new javax.swing.JComboBox();
        returnTech_cancelBtn = new javax.swing.JButton();
        weekOfLabel = new javax.swing.JLabel();
        viewTechBtn = new javax.swing.JButton();
        returnTech_homeBtn = new javax.swing.JButton();
        returnTechLabel = new javax.swing.JLabel();
        custPanel = new javax.swing.JPanel();
        custLabel = new javax.swing.JLabel();
        returnCustButton = new javax.swing.JButton();
        newCustButton = new javax.swing.JButton();
        custHomeBtn = new javax.swing.JButton();
        newCustPanel = new javax.swing.JPanel();
        addrTextField = new javax.swing.JTextField();
        nameTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        ccPassField = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox3 = new javax.swing.JComboBox();
        jComboBox4 = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        jComboBox6 = new javax.swing.JComboBox();
        jComboBox7 = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        cust_phoneNumTextField = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        ccConfirmPassField = new javax.swing.JPasswordField();
        jComboBox8 = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        returnCustPanel = new javax.swing.JPanel();
        selectCustCombo = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        selectYearCombo1 = new javax.swing.JComboBox();
        selectMonthCombo1 = new javax.swing.JComboBox();
        invoiceBtn = new javax.swing.JButton();
        returnCust_updateBtn = new javax.swing.JButton();
        streetAddrTxtField = new javax.swing.JTextField();
        phoneNumTxtField = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        ccPassField1 = new javax.swing.JPasswordField();
        ccConfirmPassField1 = new javax.swing.JPasswordField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        viewCustBtn = new javax.swing.JButton();
        returnCust_cancelBtn = new javax.swing.JButton();
        returnCust_homeBtn = new javax.swing.JButton();
        billBtn = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Lawn Mowing Service Admin");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cardLayoutPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        cardLayoutPanel.setLayout(new java.awt.CardLayout());

        mainPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        techButton.setText("Technician");
        techButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                techButtonActionPerformed(evt);
            }
        });
        mainPanel.add(techButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 30, -1, -1));

        custButton.setText("Customer");
        custButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                custButtonActionPerformed(evt);
            }
        });
        mainPanel.add(custButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, -1, -1));

        homeLabel.setText("Home");
        mainPanel.add(homeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 10, -1, -1));

        cardLayoutPanel.add(mainPanel, "card4");

        techPanel.setPreferredSize(new java.awt.Dimension(850, 850));
        techPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        techLabel.setText("Technician");
        techPanel.add(techLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 10, -1, -1));

        newTechButton.setText("New Technician");
        newTechButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTechButtonActionPerformed(evt);
            }
        });
        techPanel.add(newTechButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 30, -1, -1));

        returnTechButton.setText("Returning Technician");
        returnTechButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnTechButtonActionPerformed(evt);
            }
        });
        techPanel.add(returnTechButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, -1, -1));

        techHomeBtn.setText("Home");
        techHomeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                techHomeBtnActionPerformed(evt);
            }
        });
        techPanel.add(techHomeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 60, -1, -1));

        cardLayoutPanel.add(techPanel, "card3");

        newTechPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        phoneNumTextField.setForeground(new java.awt.Color(204, 204, 204));
        phoneNumTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        phoneNumTextField.setText("Phone Number");
        phoneNumTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                phoneNumTextFieldKeyTyped(evt);
            }
        });
        //limitPwLength(phoneNumTextField,12);
        newTechPanel.add(phoneNumTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 190, 140, -1));

        t_nameTextField.setForeground(new java.awt.Color(204, 204, 204));
        t_nameTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        t_nameTextField.setText("Name");
        t_nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                t_nameTextFieldKeyTyped(evt);
            }
        });
        newTechPanel.add(t_nameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 100, 140, -1));

        t_submitButton.setText("Submit");
        t_submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_submitButtonActionPerformed(evt);
            }
        });
        newTechPanel.add(t_submitButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 280, -1, -1));

        t_cancelButton1.setText("Cancel");
        t_cancelButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_cancelButton1ActionPerformed(evt);
            }
        });
        newTechPanel.add(t_cancelButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 280, -1, -1));

        compNameTextField.setForeground(new java.awt.Color(204, 204, 204));
        compNameTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        compNameTextField.setText("Company Name");
        compNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                compNameTextFieldKeyTyped(evt);
            }
        });
        newTechPanel.add(compNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 130, 140, -1));

        compAddrTextField.setForeground(new java.awt.Color(204, 204, 204));
        compAddrTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        compAddrTextField.setText("Company Address");
        compAddrTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                compAddrTextFieldKeyTyped(evt);
            }
        });
        newTechPanel.add(compAddrTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 160, 140, -1));

        newTech_homeBtn.setText("Home");
        newTech_homeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTech_homeBtnActionPerformed(evt);
            }
        });
        newTechPanel.add(newTech_homeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, -1, -1));

        costTextField.setForeground(new java.awt.Color(204, 204, 204));
        costTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        costTextField.setText("Cost");
        costTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                costTextFieldKeyTyped(evt);
            }
        });
        newTechPanel.add(costTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 220, 140, -1));

        try
        {
            // create TableModel for results of Default query.
            tableModel = new ResultSetTableModel( JDBC_DRIVER, DATABASE_URL,
                USERNAME, PASSWORD, DEFAULT_QUERY );

        } // end try
        catch ( ClassNotFoundException classNotFound )
        {
            JOptionPane.showMessageDialog( null,
                "MySQL driver not found", "Driver not found",
                JOptionPane.ERROR_MESSAGE );

            System.exit( 1 ); // terminate application
        } // end catch
        catch ( SQLException sqlException )
        {
            JOptionPane.showMessageDialog( null, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );

            // ensure database connection is closed
            tableModel.disconnectFromDatabase();

            System.exit( 1 );   // terminate application
        } // end
        existTechTable.setModel(tableModel);
        existTechTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(existTechTable);

        newTechPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 80, 260, 280));

        existTechLabel.setText("Existing Technicians");
        newTechPanel.add(existTechLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, -1, -1));

        newTechLabel.setText("New Technician");
        newTechPanel.add(newTechLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, -1, -1));

        cardLayoutPanel.add(newTechPanel, "card9");

        returnTechPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        selectTechCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectTechComboActionPerformed(evt);
            }
        });
        returnTechPanel.add(selectTechCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        workWeekBtn.setText("Weekly Printout");
        workWeekBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workWeekBtnActionPerformed(evt);
            }
        });
        returnTechPanel.add(workWeekBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, -1, -1));

        try
        {
            // create TableModel for results of Default query.
            returnTech_tableModel = new ResultSetTableModel( JDBC_DRIVER, DATABASE_URL,
                USERNAME, PASSWORD, RETURNTECH_QUERY );

        } // end try
        catch ( ClassNotFoundException classNotFound )
        {
            JOptionPane.showMessageDialog( null,
                "MySQL driver not found", "Driver not found",
                JOptionPane.ERROR_MESSAGE );

            System.exit( 1 ); // terminate application
        } // end catch
        catch ( SQLException sqlException )
        {
            JOptionPane.showMessageDialog( null, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );

            // ensure database connection is closed
            returnTech_tableModel.disconnectFromDatabase();

            System.exit( 1 );   // terminate application
        } // end
        jTable3.setModel(returnTech_tableModel);
        jTable3.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(jTable3);

        returnTechPanel.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 110, 760, 320));

        returnTech_updateBtn.setText("Update Profile");
        returnTech_updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnTech_updateBtnActionPerformed(evt);
            }
        });
        returnTechPanel.add(returnTech_updateBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, -1, -1));

        returnTech_costTextField.setForeground(new java.awt.Color(204, 204, 204));
        returnTech_costTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        returnTech_costTextField.setText("Cost");
        returnTech_costTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                returnTech_costTextFieldKeyTyped(evt);
            }
        });
        returnTechPanel.add(returnTech_costTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 120, -1));

        returnTech_phoneNumTextField.setForeground(new java.awt.Color(204, 204, 204));
        returnTech_phoneNumTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        returnTech_phoneNumTextField.setText("Phone Number");
        returnTech_phoneNumTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                returnTech_phoneNumTextFieldKeyTyped(evt);
            }
        });
        //limitPwLength(returnTech_phoneNumTextField, 12);
        returnTechPanel.add(returnTech_phoneNumTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 120, 30));

        returnTech_compAddrTextField.setForeground(new java.awt.Color(204, 204, 204));
        returnTech_compAddrTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        returnTech_compAddrTextField.setText("Company Address");
        returnTech_compAddrTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                returnTech_compAddrTextFieldKeyTyped(evt);
            }
        });
        returnTechPanel.add(returnTech_compAddrTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 120, -1));

        selectYearCombo.setVisible(false);
        selectYearCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectYearComboActionPerformed(evt);
            }
        });
        returnTechPanel.add(selectYearCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, -1, -1));

        selectMonthCombo.setVisible(false);
        selectMonthCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectMonthCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectMonthComboActionPerformed(evt);
            }
        });
        returnTechPanel.add(selectMonthCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 150, -1, -1));

        selectDateCombo.setVisible(false);
        selectDateCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        returnTechPanel.add(selectDateCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 150, -1, -1));

        returnTech_cancelBtn.setText("Cancel");
        returnTech_cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnTech_cancelBtnActionPerformed(evt);
            }
        });
        returnTechPanel.add(returnTech_cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, -1, -1));

        weekOfLabel.setText("Week Of");
        returnTechPanel.add(weekOfLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        viewTechBtn.setText("View Technicians");
        viewTechBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewTechBtnActionPerformed(evt);
            }
        });
        returnTechPanel.add(viewTechBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 80, -1, -1));

        returnTech_homeBtn.setText("Home");
        returnTech_homeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnTech_homeBtnActionPerformed(evt);
            }
        });
        returnTechPanel.add(returnTech_homeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, -1, -1));

        returnTechLabel.setText("Returning Technician");
        returnTechPanel.add(returnTechLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, -1, -1));

        cardLayoutPanel.add(returnTechPanel, "card10");

        custPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        custPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        custLabel.setText("Customer");
        custLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        custLabel.setPreferredSize(new java.awt.Dimension(20, 16));
        custPanel.add(custLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 0, 70, 40));

        returnCustButton.setText("Returning Customer");
        returnCustButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnCustButtonActionPerformed(evt);
            }
        });
        custPanel.add(returnCustButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, -1, -1));

        newCustButton.setText("New Customer");
        newCustButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCustButtonActionPerformed(evt);
            }
        });
        custPanel.add(newCustButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 30, -1, -1));

        custHomeBtn.setText("Home");
        custHomeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                custHomeBtnActionPerformed(evt);
            }
        });
        custPanel.add(custHomeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 60, -1, -1));

        cardLayoutPanel.add(custPanel, "card2");

        newCustPanel.setMinimumSize(new java.awt.Dimension(600, 600));
        newCustPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        newCustPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addrTextField.setForeground(new java.awt.Color(204, 204, 204));
        addrTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        addrTextField.setText("Street Address");
        addrTextField.setToolTipText("");
        addrTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                addrTextFieldKeyTyped(evt);
            }
        });
        newCustPanel.add(addrTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 210, 140, -1));

        nameTextField.setForeground(new java.awt.Color(204, 204, 204));
        nameTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nameTextField.setText("Name");
        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyTyped(evt);
            }
        });
        newCustPanel.add(nameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 90, 140, -1));

        jLabel2.setText("Sign-up Date");
        newCustPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 270, -1, -1));

        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        newCustPanel.add(submitButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 340, -1, -1));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        newCustPanel.add(cancelButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 340, -1, -1));

        limitPwLength(ccPassField,16);
        newCustPanel.add(ccPassField, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 150, 140, -1));

        jButton1.setText("Home");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        newCustPanel.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, -1, -1));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Month", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        newCustPanel.add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 270, -1, -1));

        jComboBox3.addItem("Day");
        for (int i = 0; i < 31; i++) {
            jComboBox3.addItem(i+1);
        }
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox3ActionPerformed(evt);
            }
        });
        newCustPanel.add(jComboBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 270, -1, -1));

        jComboBox4.addItem("Year");
        int todaysYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = (todaysYear-5); i <= (todaysYear+1); i++) {
            jComboBox4.addItem(i);
        }
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });
        newCustPanel.add(jComboBox4, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 270, -1, -1));

        jLabel4.setText("Birthday");
        newCustPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 120, -1, -1));

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Month", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jComboBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox5ActionPerformed(evt);
            }
        });
        newCustPanel.add(jComboBox5, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 120, -1, -1));

        jComboBox6.addItem("Day");
        for (int i = 0; i < 31; i++) {
            jComboBox6.addItem(i+1);
        }
        jComboBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox6ActionPerformed(evt);
            }
        });
        newCustPanel.add(jComboBox6, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 120, -1, -1));

        jComboBox7.addItem("Year");
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        int minYear = thisYear - 18;
        for (int i = 1884; i < (minYear + 1); i++) {
            jComboBox7.addItem(i);
        }
        newCustPanel.add(jComboBox7, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 120, -1, -1));

        jLabel5.setText("Credit Card");
        newCustPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 150, 70, 20));

        cust_phoneNumTextField.setForeground(new java.awt.Color(204, 204, 204));
        cust_phoneNumTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cust_phoneNumTextField.setText("Phone Number");
        cust_phoneNumTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cust_phoneNumTextFieldKeyTyped(evt);
            }
        });
        //limitPwLength(cust_phoneNumTextField,12);
        newCustPanel.add(cust_phoneNumTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 240, 140, -1));

        try
        {
            // create TableModel for results of Default query.
            newCust_tableModel = new ResultSetTableModel( JDBC_DRIVER, DATABASE_URL,
                USERNAME, PASSWORD, NEWCUST_QUERY );

        } // end try
        catch ( ClassNotFoundException classNotFound )
        {
            JOptionPane.showMessageDialog( null,
                "MySQL driver not found", "Driver not found",
                JOptionPane.ERROR_MESSAGE );

            System.exit( 1 ); // terminate application
        } // end catch
        catch ( SQLException sqlException )
        {
            JOptionPane.showMessageDialog( null, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );

            // ensure database connection is closed
            newCust_tableModel.disconnectFromDatabase();

            System.exit( 1 );   // terminate application
        } // end
        jTable2.setModel(newCust_tableModel);
        jTable2.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(jTable2);

        newCustPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 70, 270, 290));

        jLabel6.setText("Re-enter CC");
        newCustPanel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 180, -1, -1));

        limitPwLength(ccConfirmPassField,16);
        newCustPanel.add(ccConfirmPassField, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 180, 140, -1));

        jComboBox8.addItem("Select Technician");
        newCustPanel.add(jComboBox8, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 300, -1, -1));

        jLabel7.setText("Select Technician");
        newCustPanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 300, 120, 20));

        jLabel13.setText("Existing Customers");
        newCustPanel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 30, 110, 30));

        jLabel16.setText("New Customer");
        newCustPanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, -1, -1));

        cardLayoutPanel.add(newCustPanel, "card5");

        returnCustPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        selectCustCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectCustComboActionPerformed(evt);
            }
        });
        returnCustPanel.add(selectCustCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 90, -1, -1));

        jLabel10.setText("Month Of");
        returnCustPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 120, -1, -1));

        selectYearCombo1.setVisible(false);
        selectYearCombo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectYearCombo1ActionPerformed(evt);
            }
        });
        returnCustPanel.add(selectYearCombo1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 140, -1, -1));

        selectMonthCombo1.setVisible(false);
        selectMonthCombo1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectMonthCombo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectMonthCombo1ActionPerformed(evt);
            }
        });
        returnCustPanel.add(selectMonthCombo1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 140, -1, -1));

        invoiceBtn.setText("Invoice Mail");
        invoiceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invoiceBtnActionPerformed(evt);
            }
        });
        returnCustPanel.add(invoiceBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 200, -1, -1));

        returnCust_updateBtn.setText("Update Profile");
        returnCust_updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnCust_updateBtnActionPerformed(evt);
            }
        });
        returnCustPanel.add(returnCust_updateBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 360, -1, -1));

        streetAddrTxtField.setForeground(new java.awt.Color(204, 204, 204));
        streetAddrTxtField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        streetAddrTxtField.setText("Street Address");
        streetAddrTxtField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                streetAddrTxtFieldKeyTyped(evt);
            }
        });
        returnCustPanel.add(streetAddrTxtField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 290, 140, -1));

        phoneNumTxtField.setForeground(new java.awt.Color(204, 204, 204));
        phoneNumTxtField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        phoneNumTxtField.setText("Phone Number");
        phoneNumTxtField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                phoneNumTxtFieldKeyTyped(evt);
            }
        });
        //limitPwLength(phoneNumTxtField, 12);
        returnCustPanel.add(phoneNumTxtField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 320, 140, 30));

        try
        {
            // create TableModel for results of Default query.
            returnCust_tableModel = new ResultSetTableModel( JDBC_DRIVER, DATABASE_URL,
                USERNAME, PASSWORD, RETURNCUST_QUERY );

        } // end try
        catch ( ClassNotFoundException classNotFound )
        {
            JOptionPane.showMessageDialog( null,
                "MySQL driver not found", "Driver not found",
                JOptionPane.ERROR_MESSAGE );

            System.exit( 1 ); // terminate application
        } // end catch
        catch ( SQLException sqlException )
        {
            JOptionPane.showMessageDialog( null, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );

            // ensure database connection is closed
            returnCust_tableModel.disconnectFromDatabase();

            System.exit( 1 );   // terminate application
        } // end
        jTable4.setModel(returnCust_tableModel);
        jTable4.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(jTable4);

        returnCustPanel.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 110, 770, 320));

        jLabel3.setVisible(false);
        jLabel3.setText("You Owe");
        returnCustPanel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 90, -1, -1));

        limitPwLength(ccPassField1,16);
        returnCustPanel.add(ccPassField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 230, 140, -1));

        limitPwLength(ccConfirmPassField1,16);
        returnCustPanel.add(ccConfirmPassField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 260, 140, -1));

        jLabel11.setText("Re-enter CC");
        returnCustPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, 70, 20));

        jLabel12.setText("Credit Card");
        returnCustPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 70, 20));

        viewCustBtn.setText("View Customers");
        viewCustBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewCustBtnActionPerformed(evt);
            }
        });
        returnCustPanel.add(viewCustBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 60, -1, -1));

        returnCust_cancelBtn.setText("Cancel");
        returnCust_cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnCust_cancelBtnActionPerformed(evt);
            }
        });
        returnCustPanel.add(returnCust_cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 410, -1, -1));

        returnCust_homeBtn.setText("Home");
        returnCust_homeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnCust_homeBtnActionPerformed(evt);
            }
        });
        returnCustPanel.add(returnCust_homeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, -1, -1));

        billBtn.setText("Monthly Bill");
        billBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                billBtnActionPerformed(evt);
            }
        });
        returnCustPanel.add(billBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 170, -1, -1));

        jLabel17.setText("Returning Customer");
        returnCustPanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, -1, -1));

        cardLayoutPanel.add(returnCustPanel, "card6");

        getContentPane().add(cardLayoutPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 1050, 570));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed

        String name = nameTextField.getText();
        boolean pwMatch = false;
        boolean fieldsFilled = true;

        //get current year,month,day
        int todaysYear = Calendar.getInstance().get(Calendar.YEAR);
        int todaysMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int todaysDay = Calendar.getInstance().get(Calendar.DATE);

        //get date information of selected items in combo boxes
        int birth_monthNum = getMonthNum(jComboBox5.getSelectedItem().toString());
        String birth_month = jComboBox5.getSelectedItem().toString();
        String birth_dateStr = jComboBox6.getSelectedItem().toString();
        int birth_date = 0;
        String birth_yearStr = jComboBox7.getSelectedItem().toString();
        int ageDiff = 0;

        if (!jComboBox6.getSelectedItem().toString().equals("Day")) {
            birth_date = Integer.parseInt(jComboBox6.getSelectedItem().toString());
        }
        if (!jComboBox7.getSelectedItem().toString().equals("Year")) {
            int birth_year = Integer.parseInt(jComboBox7.getSelectedItem().toString());
            ageDiff = todaysYear - birth_year;
        }

        //find the age from the difference of entered birthday and today's date.
        if (todaysMonth < birth_monthNum) {
            ageDiff--;
        }
        if (todaysMonth == birth_monthNum) {
            if (todaysDay < birth_date) {
                ageDiff--;
            }
        }

        char[] creditCard = ccPassField.getPassword();
        char[] creditCardConfirm = ccConfirmPassField.getPassword();

        String streetAddr = addrTextField.getText();
        String phoneNum = cust_phoneNumTextField.getText();

        int signUp_monthNum = getMonthNum(jComboBox2.getSelectedItem().toString());
        String signUp_month = jComboBox2.getSelectedItem().toString();
        String signUp_dateStr = jComboBox3.getSelectedItem().toString();
        String signUp_yearStr = jComboBox4.getSelectedItem().toString();

        if (!jComboBox3.getSelectedItem().toString().equals("Day")) {
            int signUp_date = Integer.parseInt(jComboBox3.getSelectedItem().toString());
        }
        if (!jComboBox4.getSelectedItem().toString().equals("Year")) {
            int signUp_year = Integer.parseInt(jComboBox4.getSelectedItem().toString());
        }

        String selectTech = jComboBox8.getSelectedItem().toString();

        if (Arrays.equals(creditCard, creditCardConfirm)) {
            pwMatch = true;
        }

        //check for any empty textfields. displays message if not all boxes are filled.
        if (name.equals("") || birth_month.equals("Month")
                || birth_dateStr.equals("Day") || birth_yearStr.equals("Year")
                || creditCard.length == 0 || creditCardConfirm.length == 0
                || streetAddr.equals("") || phoneNum.equals("")
                || signUp_month.equals("Month") || signUp_dateStr.equals("Day")
                || signUp_yearStr.equals("Year") || selectTech.equals("Select Technician") || name.equals("Name")
                || streetAddr.equals("Street Address") || phoneNum.equals("Phone Number")) {
            fieldsFilled = false;
            JOptionPane.showMessageDialog(null, "All fields must be filled.");
        }
        
        //display error if pw fields do not match.
        if (pwMatch == false) {
            JOptionPane.showMessageDialog(null, "Credit Card Info Do Not Match.");
        }
        
        ArrayList<String> getIdList = new ArrayList<String>();
        if (fieldsFilled == true && pwMatch == true) {
            try {
                Class.forName(JDBC_DRIVER);
                Connection newCust_conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
                PreparedStatement query = newCust_conn.prepareStatement("SELECT Technician_ID FROM Technician WHERE FullName = ?");
                query.setString(1, selectTech);
                ResultSet rs = query.executeQuery();

                // get all tech id's 
                while (rs.next()) {
                    getIdList.add(rs.getString(1));
                }
                rs.close();
                query.close();

                String id = getIdList.get(0);
                //get number of customers of particular technician.
                PreparedStatement countCustQuery = newCust_conn.prepareStatement("SELECT count(*) FROM Customer WHERE Technician_ID = ?");
                countCustQuery.setInt(1, Integer.parseInt(id));
                ResultSet countRs = countCustQuery.executeQuery();
                String count = "";
                while (countRs.next()) {
                    count = countRs.getString("count(*)");
                }
                countRs.close();
                countCustQuery.close();

                int count_int = Integer.parseInt(count);
                
                String insertQuery = "INSERT INTO Customer (Technician_ID, FullName, Age, Birthday, CreditCard, Address, Phone_Number, SignUpDate) "
                        + "VALUES ('" + id + "', '" + name + "', '" + ageDiff + "', '" + birth_yearStr + "-" + birth_monthNum + "-" + birth_dateStr + "', '"
                        + creditCard + "', '" + streetAddr + "', '" + phoneNum + "', '" + signUp_yearStr + "-" + signUp_monthNum + "-" + signUp_dateStr + "');";

                Statement st = newCust_conn.createStatement();

                //insert new customer into customer table.
                if (count_int < 5) {
                    st.executeUpdate(insertQuery);
                    newCust_tableModel.setQuery(NEWCUST_QUERY);

                    PreparedStatement getCustIdQuery = newCust_conn.prepareStatement("SELECT Customer_ID FROM Customer WHERE FullName = ?");
                    getCustIdQuery.setString(1, name);
                    ResultSet getCustId_rs = getCustIdQuery.executeQuery();
                    String custId = "";
                    while (getCustId_rs.next()) {
                        custId = getCustId_rs.getString("Customer_ID");
                    }
                    getCustId_rs.close();

                    ArrayList<Calendar> serviceList = new ArrayList<Calendar>();
                    String date = signUp_yearStr + "-" + signUp_monthNum + "-" + signUp_dateStr;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar c = Calendar.getInstance();

                    try {
                        c.setTime(sdf.parse(date));
                    } catch (ParseException ex) {
                        Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //add sign up date if date is not equal to nov, dec, jan, and feb.
                    if (c.get(Calendar.MONTH) != 10 && c.get(Calendar.MONTH) != 11 && c.get(Calendar.MONTH) != 0 && c.get(Calendar.MONTH) != 1) {

                        Calendar c1 = (Calendar) c.clone();
                        serviceList.add(c1);
                    } else if (c.get(Calendar.MONTH) == 10 || c.get(Calendar.MONTH) == 11 || c.get(Calendar.MONTH) == 0 || c.get(Calendar.MONTH) == 1) {

                        c.set(Calendar.MONTH, 2);
                        c.set(Calendar.DATE, 1);
                        Calendar c2 = (Calendar) c.clone();
                        serviceList.add(c2);

                    }

                    int numOfMonths = 1;
                    
                    while (numOfMonths < 9) {
                        int oldMonth = c.get(Calendar.MONTH);
                        c.add(Calendar.DATE, 14);
                        //real month of 10 is november (11).
                        if (c.get(Calendar.MONTH) == 10) {
                            c.add(Calendar.MONTH, 4);
                            c.set(Calendar.DATE, 1);
                            oldMonth = 1;
                        }
                        int newMonth = c.get(Calendar.MONTH);

                        if (newMonth > oldMonth) {
                            numOfMonths++;
                        } else if (numOfMonths == 8) {
                            Calendar lastDate = (Calendar) c.clone();
                            lastDate.add(Calendar.DATE, 14);
                            if (lastDate.get(Calendar.MONTH) > oldMonth) {
                                numOfMonths++;
                            }
                        }

                        Calendar c3 = (Calendar) c.clone();
                        serviceList.add(c3);

                    }

                    for (int i = 0; i < serviceList.size(); i++) {
                        int month = serviceList.get(i).get(Calendar.MONTH) + 1;
                        String insertServiceQuery = "INSERT INTO Service (Customer_ID, Technician_ID, DateOfService) "
                                + "VALUES ('" + custId + "', '" + id + "', '" + serviceList.get(i).get(Calendar.YEAR) + "-"
                                + month + "-" + serviceList.get(i).get(Calendar.DATE) + "');";
                        st.executeUpdate(insertServiceQuery);

                    }
                    st.close();
                    newCust_conn.close();

                    //set all boxes/fields back to default values and colors
                    Color color = new Color(204, 204, 204);
                    nameTextField.setText("Name");
                    nameTextField.setForeground(color);
                    jComboBox5.setSelectedIndex(0);
                    jComboBox6.setSelectedIndex(0);
                    jComboBox7.setSelectedIndex(0);
                    ccPassField.setText("");
                    ccConfirmPassField.setText("");
                    addrTextField.setText("Street Address");
                    addrTextField.setForeground(color);
                    cust_phoneNumTextField.setText("Phone Number");
                    cust_phoneNumTextField.setForeground(color);
                    jComboBox2.setSelectedIndex(0);
                    jComboBox3.setSelectedIndex(0);
                    jComboBox4.setSelectedIndex(0);
                    String lowestCustQuery = getLowestCustQuery();
                    if (!lowestCustQuery.equals("Select Technician")) {
                        String[] selectTech_arr = addToArray(lowestCustQuery, "Select Technician");
                        DefaultComboBoxModel model = new DefaultComboBoxModel(selectTech_arr);
                        jComboBox8.setModel(model);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please choose another technician. Note: This technician already has 5 customers.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }


    }//GEN-LAST:event_submitButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        newCustPanel.setVisible(false);
        custPanel.setVisible(true);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void techButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_techButtonActionPerformed
        mainPanel.setVisible(false);
        techPanel.setVisible(true);

    }//GEN-LAST:event_techButtonActionPerformed

    private void custButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_custButtonActionPerformed
        mainPanel.setVisible(false);
        custPanel.setVisible(true);
    }//GEN-LAST:event_custButtonActionPerformed

    private void returnCustButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnCustButtonActionPerformed
        custPanel.setVisible(false);
        returnCustPanel.setVisible(true);

        String[] returnCust_arr = addToArray("select fullname from customer", "Select Customer");
        DefaultComboBoxModel model = new DefaultComboBoxModel(returnCust_arr);
        selectCustCombo.setModel(model);

        try {
            returnCust_tableModel.setQuery(RETURNCUST_QUERY);

        } // end try
        catch (SQLException sqlException) {

            JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);

            // ensure database connection is closed
            returnCust_tableModel.disconnectFromDatabase();

            System.exit(1);   // terminate application
        } // end
    }//GEN-LAST:event_returnCustButtonActionPerformed

    private void newCustButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCustButtonActionPerformed

        custPanel.setVisible(false);
        newCustPanel.setVisible(true);

        if (!getLowestCustQuery().equals("Select Technician")) {
            String[] selectTech_arr = addToArray(getLowestCustQuery(), "Select Technician");
            DefaultComboBoxModel model = new DefaultComboBoxModel(selectTech_arr);
            jComboBox8.setModel(model);
        }


    }//GEN-LAST:event_newCustButtonActionPerformed

    private void t_submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_submitButtonActionPerformed

        String name = t_nameTextField.getText();
        String comp_name = compNameTextField.getText();
        String comp_addr = compAddrTextField.getText();
        String phone_num = phoneNumTextField.getText();
        String cost = costTextField.getText();

        //check if all fields are filled
        if (name.equals("") || comp_name.equals("")
                || comp_addr.equals("") || phone_num.equals("")
                || cost.equals("") || name.equals("Name")
                || comp_name.equals("Company Name") || comp_addr.equals("Company Address")
                || phone_num.equals("Phone Number") || cost.equals("Cost")) {
            JOptionPane.showMessageDialog(null, "All fields must be filled.");
        } else {

            //insert new technician into technician table
            try {

                tableModel.setQuery("INSERT INTO Technician (FullName, Company_Name, Company_Address, Phone_Number, Cost) "
                        + "VALUES ('" + name + "', '" + comp_name
                        + "', '" + comp_addr + "', '" + phone_num
                        + "', '" + cost + "');");
                tableModel.setQuery(DEFAULT_QUERY);
                Color c = new Color(204, 204, 204);
                t_nameTextField.setText("Name");
                t_nameTextField.setForeground(c);
                compNameTextField.setText("Company Name");
                compNameTextField.setForeground(c);
                compAddrTextField.setText("Company Address");
                compAddrTextField.setForeground(c);
                phoneNumTextField.setText("Phone Number");
                phoneNumTextField.setForeground(c);
                costTextField.setText("Cost");
                costTextField.setForeground(c);

            } // end try
            catch (SQLException sqlException) {

                JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                        "Database error", JOptionPane.ERROR_MESSAGE);

                // ensure database connection is closed
                tableModel.disconnectFromDatabase();

                System.exit(1);   // terminate application
            } // end
        }
    }//GEN-LAST:event_t_submitButtonActionPerformed

    private void t_cancelButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_cancelButton1ActionPerformed

        Color c = new Color(204, 204, 204);
        t_nameTextField.setText("Name");
        t_nameTextField.setForeground(c);
        compNameTextField.setText("Company Name");
        compNameTextField.setForeground(c);
        compAddrTextField.setText("Company Address");
        compAddrTextField.setForeground(c);
        phoneNumTextField.setText("Phone Number");
        phoneNumTextField.setForeground(c);
        costTextField.setText("Cost");
        costTextField.setForeground(c);

        newTechPanel.setVisible(false);
        techPanel.setVisible(true);
    }//GEN-LAST:event_t_cancelButton1ActionPerformed

    private void newTech_homeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTech_homeBtnActionPerformed
        Color c = new Color(204, 204, 204);
        t_nameTextField.setText("Name");
        t_nameTextField.setForeground(c);
        compNameTextField.setText("Company Name");
        compNameTextField.setForeground(c);
        compAddrTextField.setText("Company Address");
        compAddrTextField.setForeground(c);
        phoneNumTextField.setText("Phone Number");
        phoneNumTextField.setForeground(c);
        costTextField.setText("Cost");
        costTextField.setForeground(c);

        newTechPanel.setVisible(false);
        mainPanel.setVisible(true);
    }//GEN-LAST:event_newTech_homeBtnActionPerformed

    private void newTechButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTechButtonActionPerformed

        techPanel.setVisible(false);
        newTechPanel.setVisible(true);
    }//GEN-LAST:event_newTechButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        newCustPanel.setVisible(false);
        mainPanel.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jComboBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox6ActionPerformed

    private void nameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (nameTextField.getForeground().equals(c)) {
            nameTextField.setText("");
            nameTextField.setForeground(Color.black);
        } else if (nameTextField.getForeground().equals(Color.black) && nameTextField.getText().equals("")) {
            nameTextField.setText("Name");
            nameTextField.setForeground(c);
        }
    }//GEN-LAST:event_nameTextFieldKeyTyped

    private void addrTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addrTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (addrTextField.getForeground().equals(c)) {
            addrTextField.setText("");
            addrTextField.setForeground(Color.black);
        } else if (addrTextField.getForeground().equals(Color.black) && addrTextField.getText().equals("")) {
            addrTextField.setText("Street Address");
            addrTextField.setForeground(c);
        }
    }//GEN-LAST:event_addrTextFieldKeyTyped

    private void costTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_costTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (costTextField.getForeground().equals(c)) {
            costTextField.setText("");
            costTextField.setForeground(Color.black);
        } else if (costTextField.getForeground().equals(Color.black) && costTextField.getText().equals("")) {
            costTextField.setText("Cost");
            costTextField.setForeground(c);
        }


    }//GEN-LAST:event_costTextFieldKeyTyped

    private void phoneNumTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneNumTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (phoneNumTextField.getForeground().equals(c)) {
            phoneNumTextField.setText("");
            phoneNumTextField.setForeground(Color.black);
        } else if (phoneNumTextField.getForeground().equals(Color.black) && phoneNumTextField.getText().equals("")) {
            phoneNumTextField.setText("Phone Number");
            phoneNumTextField.setForeground(c);
        }
    }//GEN-LAST:event_phoneNumTextFieldKeyTyped

    private void compAddrTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_compAddrTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (compAddrTextField.getForeground().equals(c)) {
            compAddrTextField.setText("");
            compAddrTextField.setForeground(Color.black);
        } else if (compAddrTextField.getForeground().equals(Color.black) && compAddrTextField.getText().equals("")) {
            compAddrTextField.setText("Company Address");
            compAddrTextField.setForeground(c);
        }
    }//GEN-LAST:event_compAddrTextFieldKeyTyped

    private void compNameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_compNameTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (compNameTextField.getForeground().equals(c)) {
            compNameTextField.setText("");
            compNameTextField.setForeground(Color.black);
        } else if (compNameTextField.getForeground().equals(Color.black) && compNameTextField.getText().equals("")) {
            compNameTextField.setText("Company Name");
            compNameTextField.setForeground(c);
        }
    }//GEN-LAST:event_compNameTextFieldKeyTyped

    private void t_nameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_t_nameTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (t_nameTextField.getForeground().equals(c)) {
            t_nameTextField.setText("");
            t_nameTextField.setForeground(Color.black);
        } else if (t_nameTextField.getForeground().equals(Color.black) && t_nameTextField.getText().equals("")) {
            t_nameTextField.setText("Name");
            t_nameTextField.setForeground(c);
        }
    }//GEN-LAST:event_t_nameTextFieldKeyTyped

    public int getMonthNum(String month) {
        DateFormatSymbols symbols = new DateFormatSymbols();
        String months[] = symbols.getMonths();
        for (int i = 0; i < months.length; i++) {
            if (month.equalsIgnoreCase(months[i])) {
                return i + 1;
            }
        }
        return 0;
    }
    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed

    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void cust_phoneNumTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cust_phoneNumTextFieldKeyTyped

        Color c = new Color(204, 204, 204);
        if (cust_phoneNumTextField.getForeground().equals(c)) {
            cust_phoneNumTextField.setText("");
            cust_phoneNumTextField.setForeground(Color.black);
        } else if (cust_phoneNumTextField.getForeground().equals(Color.black) && cust_phoneNumTextField.getText().equals("")) {
            cust_phoneNumTextField.setText("Phone Number");
            cust_phoneNumTextField.setForeground(c);
        }
    }//GEN-LAST:event_cust_phoneNumTextFieldKeyTyped

    private void returnTechButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnTechButtonActionPerformed
        techPanel.setVisible(false);
        returnTechPanel.setVisible(true);

        String[] returnTech_arr = addToArray("Select FullName FROM Technician", "Select Technician");
        DefaultComboBoxModel model = new DefaultComboBoxModel(returnTech_arr);
        selectTechCombo.setModel(model);

        try {
            returnTech_tableModel.setQuery(RETURNTECH_QUERY);
            jTable3.getTableHeader().getColumnModel().getColumn(6).setHeaderValue("# of Customers");

        } // end try
        catch (SQLException sqlException) {

            JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);

            // ensure database connection is closed
            returnTech_tableModel.disconnectFromDatabase();

            System.exit(1);   // terminate application
        } // end


    }//GEN-LAST:event_returnTechButtonActionPerformed

    private void returnTech_updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnTech_updateBtnActionPerformed
        String techSelected = selectTechCombo.getSelectedItem().toString();

        String comp_addr = returnTech_compAddrTextField.getText();
        String phone_num = returnTech_phoneNumTextField.getText();
        String cost = returnTech_costTextField.getText();

        try {

            Color c = new Color(204, 204, 204);
            if (!returnTech_compAddrTextField.getText().equals("Company Address") && !returnTech_compAddrTextField.getForeground().equals(c)) {
                returnTech_tableModel.setQuery("UPDATE Technician SET Company_Address = '" + comp_addr + "' WHERE FullName = '"
                        + techSelected + "';");
                returnTech_tableModel.setQuery(RETURNTECH_QUERY);
                returnTech_compAddrTextField.setText("Company Address");
                returnTech_compAddrTextField.setForeground(c);
            }
            if (!returnTech_phoneNumTextField.getText().equals("Phone Number") && !returnTech_phoneNumTextField.getForeground().equals(c)) {
                returnTech_tableModel.setQuery("UPDATE Technician SET Phone_Number = '" + phone_num + "' WHERE FullName = '"
                        + techSelected + "';");
                returnTech_tableModel.setQuery(RETURNTECH_QUERY);

                returnTech_phoneNumTextField.setText("Phone Number");
                returnTech_phoneNumTextField.setForeground(c);
            }
            if (!returnTech_costTextField.getText().equals("Cost") && !returnTech_costTextField.getForeground().equals(c)) {
                returnTech_tableModel.setQuery("UPDATE Technician SET Cost = '" + cost + "' WHERE FullName = '"
                        + techSelected + "';");
                returnTech_tableModel.setQuery(RETURNTECH_QUERY);
                returnTech_costTextField.setText("Cost");
                returnTech_costTextField.setForeground(c);
            }
            jTable3.getTableHeader().getColumnModel().getColumn(6).setHeaderValue("# of Customers");

        } // end try
        catch (SQLException sqlException) {

            JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);

            // ensure database connection is closed
            returnTech_tableModel.disconnectFromDatabase();

            System.exit(1);   // terminate application
        } // end

    }//GEN-LAST:event_returnTech_updateBtnActionPerformed

    private void returnTech_phoneNumTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_returnTech_phoneNumTextFieldKeyTyped
        Color c = new Color(204, 204, 204);
        if (returnTech_phoneNumTextField.getForeground().equals(c)) {
            returnTech_phoneNumTextField.setText("");
            returnTech_phoneNumTextField.setForeground(Color.black);
        } else if (returnTech_phoneNumTextField.getForeground().equals(Color.black) && returnTech_phoneNumTextField.getText().equals("")) {
            returnTech_phoneNumTextField.setText("Phone Number");
            returnTech_phoneNumTextField.setForeground(c);
        }
    }//GEN-LAST:event_returnTech_phoneNumTextFieldKeyTyped

    private void returnTech_compAddrTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_returnTech_compAddrTextFieldKeyTyped
        Color c = new Color(204, 204, 204);
        if (returnTech_compAddrTextField.getForeground().equals(c)) {
            returnTech_compAddrTextField.setText("");
            returnTech_compAddrTextField.setForeground(Color.black);
        } else if (returnTech_compAddrTextField.getForeground().equals(Color.black) && returnTech_compAddrTextField.getText().equals("")) {
            returnTech_compAddrTextField.setText("Company Address");
            returnTech_compAddrTextField.setForeground(c);
        }
    }//GEN-LAST:event_returnTech_compAddrTextFieldKeyTyped

    private void returnTech_costTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_returnTech_costTextFieldKeyTyped
        Color c = new Color(204, 204, 204);
        if (returnTech_costTextField.getForeground().equals(c)) {
            returnTech_costTextField.setText("");
            returnTech_costTextField.setForeground(Color.black);
        } else if (returnTech_costTextField.getForeground().equals(Color.black) && returnTech_costTextField.getText().equals("")) {
            returnTech_costTextField.setText("Cost");
            returnTech_costTextField.setForeground(c);
        }
    }//GEN-LAST:event_returnTech_costTextFieldKeyTyped

    private void workWeekBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_workWeekBtnActionPerformed
        String year = selectYearCombo.getSelectedItem().toString();
        String month = selectMonthCombo.getSelectedItem().toString();
        String dateCombo = selectDateCombo.getSelectedItem().toString();

        if (selectYearCombo.isShowing() && selectMonthCombo.isShowing() && selectDateCombo.isShowing() && !year.equals("Year") && !month.equals("Month") && !dateCombo.equals("Day")) {
            try {

                String date = year + "-" + month + "-" + dateCombo;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();

                try {
                    c.setTime(sdf.parse(date));

                } catch (ParseException ex) {
                    Logger.getLogger(StartGUI.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                Calendar c_clone = (Calendar) c.clone();
                int min_dateRange = 0;
                int max_dateRange = 0;
                int min_monthRange = 0;
                int max_monthRange = 0;
                int min_yearRange = 0;
                int max_yearRange = 0;
                //the following calculates which dates fall under which week.
                //Sunday
                if (dayOfWeek == 1) {
                    c.add(Calendar.DATE, -6);
                    min_dateRange = c.get(Calendar.DATE);
                    min_monthRange = c.get(Calendar.MONTH);
                    min_yearRange = c.get(Calendar.YEAR);
                    max_dateRange = c_clone.get(Calendar.DATE);
                    max_monthRange = c_clone.get(Calendar.MONTH);
                    max_yearRange = c_clone.get(Calendar.YEAR);
                } //Monday
                else if (dayOfWeek == 2) {
                    min_dateRange = c.get(Calendar.DATE);
                    min_monthRange = c.get(Calendar.MONTH);
                    min_yearRange = c.get(Calendar.YEAR);
                    c_clone.add(Calendar.DATE, 6);
                    max_dateRange = c_clone.get(Calendar.DATE);
                    max_monthRange = c_clone.get(Calendar.MONTH);
                    max_yearRange = c_clone.get(Calendar.YEAR);
                } //Tuesday
                else if (dayOfWeek == 3) {
                    c.add(Calendar.DATE, -1);
                    min_dateRange = c.get(Calendar.DATE);
                    min_monthRange = c.get(Calendar.MONTH);
                    min_yearRange = c.get(Calendar.YEAR);
                    c_clone.add(Calendar.DATE, 5);
                    max_dateRange = c_clone.get(Calendar.DATE);
                    max_monthRange = c_clone.get(Calendar.MONTH);
                    max_yearRange = c_clone.get(Calendar.YEAR);
                } //Wednesday
                else if (dayOfWeek == 4) {
                    c.add(Calendar.DATE, -2);
                    min_dateRange = c.get(Calendar.DATE);
                    min_monthRange = c.get(Calendar.MONTH);
                    min_yearRange = c.get(Calendar.YEAR);
                    c_clone.add(Calendar.DATE, 4);
                    max_dateRange = c_clone.get(Calendar.DATE);
                    max_monthRange = c_clone.get(Calendar.MONTH);
                    max_yearRange = c_clone.get(Calendar.YEAR);
                } //Thursday
                else if (dayOfWeek == 5) {
                    c.add(Calendar.DATE, -3);
                    min_dateRange = c.get(Calendar.DATE);
                    min_monthRange = c.get(Calendar.MONTH);
                    min_yearRange = c.get(Calendar.YEAR);
                    c_clone.add(Calendar.DATE, 3);
                    max_dateRange = c_clone.get(Calendar.DATE);
                    max_monthRange = c_clone.get(Calendar.MONTH);
                    max_yearRange = c_clone.get(Calendar.YEAR);
                } //Friday
                else if (dayOfWeek == 6) {
                    c.add(Calendar.DATE, -4);
                    min_dateRange = c.get(Calendar.DATE);
                    min_monthRange = c.get(Calendar.MONTH);
                    min_yearRange = c.get(Calendar.YEAR);
                    c_clone.add(Calendar.DATE, 2);
                    max_dateRange = c_clone.get(Calendar.DATE);
                    max_monthRange = c_clone.get(Calendar.MONTH);
                    max_yearRange = c_clone.get(Calendar.YEAR);
                } //Saturday
                else if (dayOfWeek == 7) {
                    c.add(Calendar.DATE, -5);
                    min_dateRange = c.get(Calendar.DATE);
                    min_monthRange = c.get(Calendar.MONTH);
                    min_yearRange = c.get(Calendar.YEAR);
                    c_clone.add(Calendar.DATE, 1);
                    max_dateRange = c_clone.get(Calendar.DATE);
                    max_monthRange = c_clone.get(Calendar.MONTH);
                    max_yearRange = c_clone.get(Calendar.YEAR);
                }
                min_monthRange++;
                max_monthRange++;
                
                String min_date = min_yearRange + "-" + min_monthRange + "-" + min_dateRange;
                String max_date = max_yearRange + "-" + max_monthRange + "-" + max_dateRange;
                String fullName = selectTechCombo.getSelectedItem().toString();
                int techId = getTechID(fullName);
                
                //set query between the range of first day of week (monday) to the end of week (sunday).
                returnTech_tableModel.setQuery("SELECT Technician.FullName, Customer.FullName, DateOfService FROM Service JOIN Customer JOIN Technician "
                        + "WHERE Service.Customer_ID = Customer.Customer_ID AND Service.Technician_ID = Technician.Technician_ID AND DateOfService >= '" + min_date
                        + "' AND DateOfService <= '" + max_date + "' AND Technician.Technician_ID = " + techId + ";");

                jTable3.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Technician Name");
                jTable3.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Customer Name");

            } // end try
            catch (SQLException sqlException) {

                JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                        "Database error", JOptionPane.ERROR_MESSAGE);

                // ensure database connection is closed
                returnTech_tableModel.disconnectFromDatabase();

                System.exit(1);   // terminate application
            } // end
        }


    }//GEN-LAST:event_workWeekBtnActionPerformed

    private void selectTechComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTechComboActionPerformed
        if (!selectTechCombo.getSelectedItem().toString().equals("Select Technician")) {
            selectYearCombo.setVisible(true);
        } else {
            selectYearCombo.setVisible(false);
            selectMonthCombo.setVisible(false);
            selectDateCombo.setVisible(false);
        }

        int id = getTechID(selectTechCombo.getSelectedItem().toString());
        String[] returnTechYear_arr = addToArray("SELECT Distinct Year(DateOfService) FROM Service WHERE Technician_ID = " + id + ";", "Year");
        DefaultComboBoxModel model = new DefaultComboBoxModel(returnTechYear_arr);
        selectYearCombo.setModel(model);


    }//GEN-LAST:event_selectTechComboActionPerformed

    private void selectYearComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectYearComboActionPerformed
        if (!selectYearCombo.getSelectedItem().toString().equals("Year")) {

            int id = getTechID(selectTechCombo.getSelectedItem().toString());
            String year = selectYearCombo.getSelectedItem().toString();
//            System.out.println("id: " + id);
//            System.out.println("year: " + year);
            String[] returnTechMonth_arr = addToArray("SELECT Distinct Month(DateOfService) FROM Service WHERE Technician_ID = " + id
                    + " AND Year(DateOfService) = " + year + ";", "Month");
            DefaultComboBoxModel model = new DefaultComboBoxModel(returnTechMonth_arr);

            selectMonthCombo.setModel(model);
            selectMonthCombo.setVisible(true);

        } else {
            selectMonthCombo.setVisible(false);
            selectDateCombo.setVisible(false);
        }


    }//GEN-LAST:event_selectYearComboActionPerformed

    private void selectMonthComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectMonthComboActionPerformed
        if (!selectMonthCombo.getSelectedItem().toString().equals("Month")) {

            int id = getTechID(selectTechCombo.getSelectedItem().toString());
            String year = selectYearCombo.getSelectedItem().toString();
            String month = selectMonthCombo.getSelectedItem().toString();

            String[] returnTechDate_arr = addToArray("SELECT Distinct Day(DateOfService) FROM Service WHERE Technician_ID = " + id
                    + " AND Year(DateOfService) = " + year + " AND Month(DateOfService) = " + month + ";", "Day");
            DefaultComboBoxModel model = new DefaultComboBoxModel(returnTechDate_arr);
            selectDateCombo.setModel(model);
            selectDateCombo.setVisible(true);

        } else {
            selectDateCombo.setVisible(false);
        }
    }//GEN-LAST:event_selectMonthComboActionPerformed

    private void returnTech_cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnTech_cancelBtnActionPerformed
        selectTechCombo.setSelectedIndex(0);
        returnTechPanel.setVisible(false);
        techPanel.setVisible(true);
    }//GEN-LAST:event_returnTech_cancelBtnActionPerformed

    private void viewTechBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewTechBtnActionPerformed

        selectTechCombo.setSelectedIndex(0);

        try {
            returnTech_tableModel.setQuery(RETURNTECH_QUERY);
            jTable3.getTableHeader().getColumnModel().getColumn(6).setHeaderValue("# of Customers");
        } // end try
        catch (SQLException sqlException) {

            JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);

            // ensure database connection is closed
            returnTech_tableModel.disconnectFromDatabase();

            System.exit(1);   // terminate application
        } // end
    }//GEN-LAST:event_viewTechBtnActionPerformed

    private void selectCustComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectCustComboActionPerformed
        if (!selectCustCombo.getSelectedItem().toString().equals("Select Customer")) {
            selectYearCombo1.setVisible(true);
        } else {
            selectYearCombo1.setVisible(false);
            selectMonthCombo1.setVisible(false);
        }

        int id = getCustID(selectCustCombo.getSelectedItem().toString());
        String[] returnCustYear_arr = addToArray("SELECT Distinct Year(DateOfService) FROM Service WHERE Customer_ID = " + id + ";", "Year");
        DefaultComboBoxModel model = new DefaultComboBoxModel(returnCustYear_arr);
        selectYearCombo1.setModel(model);
    }//GEN-LAST:event_selectCustComboActionPerformed

    private void selectYearCombo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectYearCombo1ActionPerformed
        if (!selectYearCombo1.getSelectedItem().toString().equals("Year")) {
            selectMonthCombo1.setVisible(true);
        } else {
            selectMonthCombo1.setVisible(false);
        }
        String year = selectYearCombo1.getSelectedItem().toString();
        int id = getCustID(selectCustCombo.getSelectedItem().toString());
        String[] returnCustMonth_arr = addToArray("SELECT Distinct Month(DateOfService) FROM Service WHERE Customer_ID = " + id + " AND Year(DateOfService) = "
                + year + ";", "Month");
        DefaultComboBoxModel model = new DefaultComboBoxModel(returnCustMonth_arr);
        selectMonthCombo1.setModel(model);
    }//GEN-LAST:event_selectYearCombo1ActionPerformed

    private void selectMonthCombo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectMonthCombo1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectMonthCombo1ActionPerformed

    private void invoiceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceBtnActionPerformed
        String year = selectYearCombo1.getSelectedItem().toString();
        String month = selectMonthCombo1.getSelectedItem().toString();
        String fullName = selectCustCombo.getSelectedItem().toString();
        int custId = getCustID(fullName);
        float totalCost = 0;
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        Font termsFont = new Font(Font.FontFamily.TIMES_ROMAN, 7);
        String date = year + "-" + month + "-" + 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        int lastDay = 0;
        int dueDay = 0;
        int dueYear = 0;
        int dueMonth = 0;
        String dueDate = "";
        String custAddr = "";
        String custPhoneNum = "";
        
        //only perform if combo boxes are selected or visible
        if (selectYearCombo1.isShowing() && selectMonthCombo1.isShowing() && !year.equals("Year") && !month.equals("Month")) {
            try {

                returnCust_tableModel.setQuery("SELECT DateOfService, Customer.FullName, Technician.FullName, Cost FROM Service JOIN Customer JOIN Technician WHERE"
                        + " Customer.Customer_ID = Service.Customer_ID AND Technician.Technician_ID = Service.Technician_ID AND Year(DateOfService) = " + year
                        + " AND Month(DateOfService) = " + month + " AND Customer.Customer_ID = " + custId);

                jTable4.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Customer Name");
                jTable4.getTableHeader().getColumnModel().getColumn(2).setHeaderValue("Technician Name");
                jTable4.getTableHeader().getColumnModel().getColumn(3).setHeaderValue("Cost Per Service; (USD)");

                jLabel3.setVisible(true);

                Connection invoiceBtn_conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
                Statement st = invoiceBtn_conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT Customer.FullName, Technician.FullName, SUM(Cost) FROM Service JOIN Customer JOIN Technician WHERE"
                        + " Customer.Customer_ID = Service.Customer_ID AND Technician.Technician_ID = Service.Technician_ID AND Year(DateOfService) = " + year
                        + " AND Month(DateOfService) = " + month + " AND Customer.Customer_ID = " + custId);

                while (rs.next()) {
                    totalCost = rs.getFloat("SUM(Cost)");
                    System.out.println("totalCost: " + totalCost);
                }
                rs.close();

                try {
                    c.setTime(sdf.parse(date));
                } catch (ParseException ex) {
                    Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                c.set(Calendar.DATE, lastDay);
                c.add(Calendar.DATE, 30);
                dueDay = c.get(Calendar.DATE);
                dueYear = c.get(Calendar.YEAR);
                dueMonth = c.get(Calendar.MONTH) + 1;
                dueDate = dueYear + "-" + dueMonth + "-" + dueDay;

                ResultSet rs1 = st.executeQuery("SELECT * FROM Customer WHERE Customer_ID = " + custId + ";");

                while (rs1.next()) {
                    custAddr = rs1.getString("Address");
                    custPhoneNum = rs1.getString("Phone_Number");
                }
                rs1.close();
                st.close();
                invoiceBtn_conn.close();
            } // end try
            catch (SQLException sqlException) {

                JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                        "Database error", JOptionPane.ERROR_MESSAGE);

                // ensure database connection is closed
                returnCust_tableModel.disconnectFromDatabase();

                System.exit(1);   // terminate application
            } // end
            
            //format of PDF file
            try {
                WriteToPDF writeToPdf = new WriteToPDF();

                writeToPdf.addToPage(writeToPdf.newParagraph(year + "-" + month + "-" + lastDay, false, false, true));
                writeToPdf.addToPage(new Paragraph(" "));
                writeToPdf.addTable(jTable4.getColumnCount(), getTableData(jTable4), jTable4);
                writeToPdf.addToPage(new Paragraph("                                                                                                              "
                        + "Total Cost: $" + totalCost, boldFont));
                writeToPdf.addToPage(writeToPdf.newParagraph("Payment due by: " + dueDate, false, true, false));

                Paragraph p = new Paragraph("Terms of Condition: You are subject to pay late charges if the full payment is not paid promptly.", termsFont);
                p.setAlignment(Element.ALIGN_CENTER);
                for (int i = 0; i < 8; i++) {
                    writeToPdf.addToPage(new Paragraph(" "));
                }
                writeToPdf.addToPage(new Paragraph("INVOICED CUSTOMER:                                                              SEND PAYMENT TO:", boldFont));
                writeToPdf.addToPage(new Paragraph(" "));
                writeToPdf.addToPage(writeToPdf.newParagraph(fullName + "                                                                                                                       John Mower", false, true, false));
                writeToPdf.addToPage(writeToPdf.newParagraph(custAddr + "                                                                                                             2768 Lawn Rd", false, true, false));
                writeToPdf.addToPage(writeToPdf.newParagraph(custPhoneNum + "                                                                                                    San Jose, CA 95148", false, true, false));
                writeToPdf.addToPage(new Paragraph(" "));
                writeToPdf.addToPage(writeToPdf.newParagraph("408-459-4747", false, false, true));
                writeToPdf.addToPage(writeToPdf.newParagraph("JohnMower@gmail.com", false, false, true));
                writeToPdf.addToPage(writeToPdf.newParagraph("http://www.lawnmowingassoc.com", false, false, true));
                for (int i = 0; i < 12; i++) {
                    writeToPdf.addToPage(new Paragraph(" "));
                }
                writeToPdf.addToPage(p);
                writeToPdf.getDoc().close();

                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler Invoice.pdf");

            } catch (FileNotFoundException ex) {
                Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
                Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }//GEN-LAST:event_invoiceBtnActionPerformed

    private void returnCust_updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnCust_updateBtnActionPerformed
        boolean pwMatch = false;
        String custSelected = selectCustCombo.getSelectedItem().toString();
        char[] creditCard = ccPassField1.getPassword();
        char[] creditCardConfirm = ccConfirmPassField1.getPassword();
        String streetAddr = streetAddrTxtField.getText();
        String phoneNum = phoneNumTxtField.getText();

        if (Arrays.equals(creditCard, creditCardConfirm) && creditCard.length != 0 && creditCardConfirm.length != 0) {
            pwMatch = true;
        }

        if (pwMatch == false && creditCard.length != 0 && creditCardConfirm.length != 0) {
            JOptionPane.showMessageDialog(null, "Credit Card Info Do Not Match.");
        }
        Color c = new Color(204, 204, 204);
        try {
            if (pwMatch == true) {
                returnCust_tableModel.setQuery("UPDATE Customer SET CreditCard = '" + creditCard + "' WHERE FullName = '"
                        + custSelected + "';");
            }
            if (!streetAddr.equals("Street Address") && !streetAddrTxtField.getForeground().equals(c)) {
                returnCust_tableModel.setQuery("UPDATE Customer SET Address = '" + streetAddr + "' WHERE FullName = '"
                        + custSelected + "';");
                streetAddrTxtField.setText("Street Address");
                streetAddrTxtField.setForeground(c);
            }
            if (!phoneNum.equals("Phone Number") && !phoneNumTxtField.getForeground().equals(c)) {
                returnCust_tableModel.setQuery("UPDATE Customer SET Phone_Number = '" + phoneNum + "' WHERE FullName = '"
                        + custSelected + "';");
                phoneNumTxtField.setText("Phone Number");
                phoneNumTxtField.setForeground(c);
            }

            returnCust_tableModel.setQuery(RETURNCUST_QUERY);

        } // end try
        catch (SQLException sqlException) {

            JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);

            // ensure database connection is closed
            returnCust_tableModel.disconnectFromDatabase();

            System.exit(1);   // terminate application
        } // end

    }//GEN-LAST:event_returnCust_updateBtnActionPerformed

    private void streetAddrTxtFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_streetAddrTxtFieldKeyTyped
        Color c = new Color(204, 204, 204);
        if (streetAddrTxtField.getForeground().equals(c)) {
            streetAddrTxtField.setText("");
            streetAddrTxtField.setForeground(Color.black);
        } else if (streetAddrTxtField.getForeground().equals(Color.black) && streetAddrTxtField.getText().equals("")) {
            streetAddrTxtField.setText("Street Address");
            streetAddrTxtField.setForeground(c);
        }
    }//GEN-LAST:event_streetAddrTxtFieldKeyTyped

    private void phoneNumTxtFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneNumTxtFieldKeyTyped
        Color c = new Color(204, 204, 204);
        if (phoneNumTxtField.getForeground().equals(c)) {
            phoneNumTxtField.setText("");
            phoneNumTxtField.setForeground(Color.black);
        } else if (phoneNumTxtField.getForeground().equals(Color.black) && phoneNumTxtField.getText().equals("")) {
            phoneNumTxtField.setText("Phone Number");
            phoneNumTxtField.setForeground(c);
        }
    }//GEN-LAST:event_phoneNumTxtFieldKeyTyped

    private void viewCustBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewCustBtnActionPerformed

        selectCustCombo.setSelectedIndex(0);
        jLabel3.setVisible(false);
        try {
            returnCust_tableModel.setQuery(RETURNCUST_QUERY);
        } // end try
        catch (SQLException sqlException) {

            JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);

            // ensure database connection is closed
            returnCust_tableModel.disconnectFromDatabase();

            System.exit(1);   // terminate application
        } // end
    }//GEN-LAST:event_viewCustBtnActionPerformed

    private void techHomeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_techHomeBtnActionPerformed
        techPanel.setVisible(false);
        mainPanel.setVisible(true);
    }//GEN-LAST:event_techHomeBtnActionPerformed

    private void custHomeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_custHomeBtnActionPerformed
        custPanel.setVisible(false);
        mainPanel.setVisible(true);
    }//GEN-LAST:event_custHomeBtnActionPerformed

    private void returnCust_cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnCust_cancelBtnActionPerformed
        selectCustCombo.setSelectedIndex(0);
        returnCustPanel.setVisible(false);
        custPanel.setVisible(true);
    }//GEN-LAST:event_returnCust_cancelBtnActionPerformed

    private void returnTech_homeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnTech_homeBtnActionPerformed
        selectTechCombo.setSelectedIndex(0);
        returnTechPanel.setVisible(false);
        mainPanel.setVisible(true);
    }//GEN-LAST:event_returnTech_homeBtnActionPerformed

    private void returnCust_homeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnCust_homeBtnActionPerformed
        selectCustCombo.setSelectedIndex(0);
        returnCustPanel.setVisible(false);
        mainPanel.setVisible(true);
    }//GEN-LAST:event_returnCust_homeBtnActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void billBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billBtnActionPerformed
        String year = selectYearCombo1.getSelectedItem().toString();
        String month = selectMonthCombo1.getSelectedItem().toString();
        String fullName = selectCustCombo.getSelectedItem().toString();
        int custId = getCustID(fullName);
        if (selectYearCombo1.isShowing() && selectMonthCombo1.isShowing() && !year.equals("Year") && !month.equals("Month")) {
            try {

                returnCust_tableModel.setQuery("SELECT Customer.FullName, Technician.FullName, SUM(Cost) FROM Service JOIN Customer JOIN Technician WHERE"
                        + " Customer.Customer_ID = Service.Customer_ID AND Technician.Technician_ID = Service.Technician_ID AND Year(DateOfService) = " + year
                        + " AND Month(DateOfService) = " + month + " AND Customer.Customer_ID = " + custId);

                jTable4.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Customer Name");
                jTable4.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Technician Name");
                jTable4.getTableHeader().getColumnModel().getColumn(2).setHeaderValue("Monthly Bill (net30); (USD)");
                jLabel3.setVisible(true);

            } // end try
            catch (SQLException sqlException) {

                JOptionPane.showMessageDialog(null, sqlException.getMessage(),
                        "Database error", JOptionPane.ERROR_MESSAGE);

                // ensure database connection is closed
                returnCust_tableModel.disconnectFromDatabase();

                System.exit(1);   // terminate application
            } // end
        }
    }//GEN-LAST:event_billBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws FileNotFoundException, DocumentException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StartGUI().setVisible(true);
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addrTextField;
    private javax.swing.JButton billBtn;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cardLayoutPanel;
    private javax.swing.JPasswordField ccConfirmPassField;
    private javax.swing.JPasswordField ccConfirmPassField1;
    private javax.swing.JPasswordField ccPassField;
    private javax.swing.JPasswordField ccPassField1;
    private javax.swing.JTextField compAddrTextField;
    private javax.swing.JTextField compNameTextField;
    private javax.swing.JTextField costTextField;
    private javax.swing.JButton custButton;
    private javax.swing.JButton custHomeBtn;
    private javax.swing.JLabel custLabel;
    private javax.swing.JPanel custPanel;
    private javax.swing.JTextField cust_phoneNumTextField;
    private javax.swing.JLabel existTechLabel;
    private javax.swing.JTable existTechTable;
    private javax.swing.JLabel homeLabel;
    private javax.swing.JButton invoiceBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JComboBox jComboBox8;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton newCustButton;
    private javax.swing.JPanel newCustPanel;
    private javax.swing.JButton newTechButton;
    private javax.swing.JLabel newTechLabel;
    private javax.swing.JPanel newTechPanel;
    private javax.swing.JButton newTech_homeBtn;
    private javax.swing.JTextField phoneNumTextField;
    private javax.swing.JTextField phoneNumTxtField;
    private javax.swing.JButton returnCustButton;
    private javax.swing.JPanel returnCustPanel;
    private javax.swing.JButton returnCust_cancelBtn;
    private javax.swing.JButton returnCust_homeBtn;
    private javax.swing.JButton returnCust_updateBtn;
    private javax.swing.JButton returnTechButton;
    private javax.swing.JLabel returnTechLabel;
    private javax.swing.JPanel returnTechPanel;
    private javax.swing.JButton returnTech_cancelBtn;
    private javax.swing.JTextField returnTech_compAddrTextField;
    private javax.swing.JTextField returnTech_costTextField;
    private javax.swing.JButton returnTech_homeBtn;
    private javax.swing.JTextField returnTech_phoneNumTextField;
    private javax.swing.JButton returnTech_updateBtn;
    private javax.swing.JComboBox selectCustCombo;
    private javax.swing.JComboBox selectDateCombo;
    private javax.swing.JComboBox selectMonthCombo;
    private javax.swing.JComboBox selectMonthCombo1;
    private javax.swing.JComboBox selectTechCombo;
    private javax.swing.JComboBox selectYearCombo;
    private javax.swing.JComboBox selectYearCombo1;
    private javax.swing.JTextField streetAddrTxtField;
    private javax.swing.JButton submitButton;
    private javax.swing.JButton t_cancelButton1;
    private javax.swing.JTextField t_nameTextField;
    private javax.swing.JButton t_submitButton;
    private javax.swing.JButton techButton;
    private javax.swing.JButton techHomeBtn;
    private javax.swing.JLabel techLabel;
    private javax.swing.JPanel techPanel;
    private javax.swing.JButton viewCustBtn;
    private javax.swing.JButton viewTechBtn;
    private javax.swing.JLabel weekOfLabel;
    private javax.swing.JButton workWeekBtn;
    // End of variables declaration//GEN-END:variables
}
