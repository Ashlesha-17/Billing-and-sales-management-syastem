import javax.swing.*;
import java.sql.*;
import java.awt.*;
import javax.*;
import javax.swing.table.*;

class Bill extends JFrame {
    Double finalBill = 0.0;
    JTable table;
    String custName ,contact;

    Bill(JTable table,String custName, String contact){
        this.custName = custName;
        this.contact = contact;
        this.table = table;
        TableModel model = table.getModel();

        Font f1 = new Font("Arial", Font.BOLD, 40);
        Font f2 = new Font("Tahoma", Font.PLAIN, 22);
        Font f3 = new Font("Tahoma", Font.BOLD, 22);
        Font f4 = new Font("Tahoma", Font.PLAIN, 25);

        JLabel bill = new JLabel();
        bill.setFont(f2);
        bill.setPreferredSize(new Dimension(300, 50));
        bill.setBackground(new Color(0x9E6374));
        bill.setForeground(Color.white);
        bill.setOpaque(true);
        bill.setHorizontalAlignment(JLabel.CENTER);
        bill.setVerticalAlignment(JLabel.CENTER);


        //topPanel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Bill", JLabel.CENTER);
        title.setOpaque(true);
        title.setFont(f1);
        title.setBackground(new Color(0x9E6374));
        title.setForeground(Color.white);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topPanel.add(title,BorderLayout.CENTER);

        //customer info panel
        JPanel cust = new JPanel(new BorderLayout());

        JPanel info = new JPanel(new GridLayout(2,1));
        JLabel l1 = new JLabel("Name of Customer : "+custName);
        JLabel l2 = new JLabel("Contact No. : "+contact);
        l1.setFont(f2);
        l2.setFont(f2);
        info.add(l1);
        info.add(l2);
        cust.add(info,BorderLayout.WEST);

        String dateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        JLabel dateLabel = new JLabel("Date&Time : " + dateTime);
        dateLabel.setFont(f2);
        cust.add(dateLabel,BorderLayout.EAST);

        topPanel.add(cust,BorderLayout.SOUTH);



        //table consisting selected pdts and an opt to add more pdts
        String[] columnNames = {"No.", "Name", "Category", "Quantity", "Price","Remove"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
            @Override
            public Class<?> getColumnClass (int columnIndex){
                if (columnIndex == 5) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        //making table
        JTable table1 = new JTable(tableModel);
        table1.setFont(f2);
        table1.setRowHeight(35);
        table1.getTableHeader().setFont(f4);

        //Table setting values
        String url = "jdbc:mysql://localhost:3306/31may";
        try (Connection con = DriverManager.getConnection(url, "root", "Hope$02")) {
            String sql = "select Price from Stock WHERE Name = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {

                int count = 1;
                for (int i =0;i<model.getRowCount();i++){
                    Boolean selected = (Boolean) model.getValueAt(i,5);
                    if(selected != null && selected){
                        String name = (String) model.getValueAt(i, 1);
                        String category = (String) model.getValueAt(i, 2);
                        String qtyStr = (String) model.getValueAt(i, 3);
                        Object qtyObj = model.getValueAt(i, 3);
                        int quantity = 0;
                        try {
                            quantity = Integer.parseInt(qtyStr);
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(null, "Invalid quantity for product: " + name);
                            continue; // skip this product
                        }

                        // Optional: if you want price/amount later
                        String priceStr = model.getValueAt(i, 4) != null ? model.getValueAt(i, 4).toString().trim() : "0";

                        double price = 0;
                        try {
                            price = Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            // just set 0 or skip the row, your choice
                            JOptionPane.showMessageDialog(null, "Invalid price format for product: " + name);
                            continue;
                        }
                        if (qtyObj instanceof String) {
                            quantity = Integer.parseInt((String) qtyObj);
                        } else if (qtyObj instanceof Integer) {
                            quantity = (Integer) qtyObj;
                        }

                        pst.setString(1,name);
                        ResultSet rs = pst.executeQuery();


                        if (rs.next()) {
                            price = rs.getDouble("Price");
                            System.out.println("DEBUG: Price for product '" + name + "' fetched from DB = '" + price + "'");
                        }

                        int Q = Integer.parseInt(qtyStr);
                        double totalPrice = Q * price;
                        boolean found = false;
                        for(int j =0;j<tableModel.getRowCount();j++){
                            String existingName = (String) tableModel.getValueAt(j,1);
                            if(existingName.equals(name)){
                                int existingQty = Integer.parseInt((String) tableModel.getValueAt(j,3));
                                int newQty = existingQty + Q ;
                                Double newTotal = price*newQty;
                                tableModel.setValueAt(String.valueOf(newQty),j,3);
                                tableModel.setValueAt(String.valueOf(newTotal),j,4);
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            tableModel.addRow(new Object[]{count++, name, category, String.valueOf(quantity), String.valueOf(totalPrice), false});


                            int updatedQ = 0;
                            int StockQ = 0;
                            try (Connection conn = DriverManager.getConnection(url, "root", "Hope$02")) {
                                String sql1 = "UPDATE Stock SET Quantity = ? WHERE Name = ?";
                                String sql2 = "select Quantity from Stock where Name = ? ";
                                try(PreparedStatement pst1 = conn.prepareStatement(sql2)){
                                    pst1.setString(1,name);
                                    ResultSet rs1 = pst1.executeQuery();
                                    if(rs1.next()){
                                        StockQ = rs1.getInt("Quantity");
                                    }
                                }
                                updatedQ = StockQ - quantity;
                                try (PreparedStatement pst2 = conn.prepareStatement(sql1)) {
                                    pst2.setInt(1,updatedQ);
                                    pst2.setString(2,name);
                                    pst2.executeUpdate();
                                }
                            }catch (Exception e){
                                JOptionPane.showMessageDialog(null,e.getMessage());
                            }
                        }

                    }
                }


            }
        }
            catch (Exception e){
                JOptionPane.showMessageDialog(null,e.getMessage());
            }

        JScrollPane scroll = new JScrollPane(table1);

        tableModel.addTableModelListener(
                a->{
                    int row = a.getFirstRow();
                    int column = a.getColumn();

                    if (column == 5){
                        Boolean chcked = (Boolean) tableModel.getValueAt(row,column);
                        if (chcked != null && chcked){
                            tableModel.removeRow(row);

                            for(int i = 0; i<tableModel.getRowCount(); i++){
                                tableModel.setValueAt(i+1,i,0);

                            }calcBill(table1,bill);
                        }
                    }
                }
        );



        //centerPanel
        JPanel centerPanel = new JPanel(new BorderLayout());

        //AddMore button
        JPanel topTable = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Horizontally right-aligned
        JButton add = new JButton("Add");
        add.setFont(f2);
        add.setBackground(new Color(168, 225, 168));
        add.setPreferredSize(new Dimension(150, 40));  // Shorter height
        add.addActionListener(
                a->{
                    dispose();
                }
        );
        buttonPanel.add(add);
        topTable.add(buttonPanel,BorderLayout.NORTH);
        topTable.add(scroll,BorderLayout.CENTER);

        centerPanel.add(topTable,BorderLayout.CENTER);

        //bottomPanel
        JPanel bottomPanel =new JPanel(new BorderLayout());
        if (table1.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "No products selected. Bill cannot be generated.");
            dispose();
            return;
        }

        calcBill(table1,bill);

        JPanel centerButtonPanel = new JPanel(new FlowLayout());
        JButton payment = new JButton("Pay");
        payment.addActionListener(
                a->{
                    TableColumnModel columnModel = table1.getColumnModel();
                    if(columnModel.getColumnCount()>5){
                        TableColumn removeColumn = columnModel.getColumn(5);
                        removeColumn.setMinWidth(0);
                        removeColumn.setMaxWidth(0);
                        removeColumn.setPreferredWidth(0);
                        removeColumn.setResizable(false);
                    }
                    add.setVisible(false);
                    table1.setEnabled(false);
                    payment.setEnabled(false);
                    int billId = -1;
                    int FQ = 0;
                    for (int i = 0; i < table1.getRowCount(); i++) {
                        String qtyStr = (String) table1.getValueAt(i, 3); // quantity column
                        try {
                            int qty = Integer.parseInt(qtyStr);
                            FQ += qty;
                        } catch (NumberFormatException e) {
                            // handle error if needed
                        }
                    }

                    try (Connection con = DriverManager.getConnection(url,"root","Hope$02")){
                        String sql = "INSERT INTO bills(customer_name, contact_no,total_quantity, total_amount, bill_datetime) VALUES(?,?,?,?,?)";
                        try(PreparedStatement pst = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS))
                        {
                            pst.setString(1,custName);
                            pst.setString(2,contact);
                            pst.setInt(3,FQ);
                            pst.setDouble(4,finalBill);
                            pst.setString(5,dateTime);

                            pst.executeUpdate();
                            ResultSet rs = pst.getGeneratedKeys();

                            if (rs.next()) {
                                billId = rs.getInt(1);
                            }
                            dispose();
                        }
                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(null,e.getMessage());
                    }


                    try (Connection con = DriverManager.getConnection(url,"root","Hope$02")){
                        for(int k=0;k<table1.getRowCount();k++){
                            String sql = "INSERT INTO bill_items(bill_id,product_name,category, quantity, price) VALUES(?,?,?,?,?)";
                            try(PreparedStatement pst = con.prepareStatement(sql))
                            {
                                pst.setInt(1,billId);
                                pst.setString(2,(String) table1.getValueAt(k,1));
                                pst.setString(3,(String) table1.getValueAt(k,2));
                                pst.setInt(4,Integer.parseInt((String) table1.getValueAt(k,3)));
                                pst.setDouble(5,Double.parseDouble((String) table1.getValueAt(k,4)));

                                pst.executeUpdate();
                                dispose();
                            }
                        }


                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(null,e.getMessage());
                    }
                }
        );
        payment.setPreferredSize(new Dimension(200, 50));
        payment.setBackground(new Color(0x9E6374));
        payment.setForeground(Color.white);
        payment.setFont(f2);
        centerButtonPanel.add(bill);
        centerButtonPanel.add(payment);
        bottomPanel.add(centerButtonPanel,BorderLayout.CENTER);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        c.add(topPanel,BorderLayout.NORTH);
        c.add(centerPanel,BorderLayout.CENTER);
        c.add(bottomPanel,BorderLayout.SOUTH);


        setVisible(true);
        setSize(1200,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Bill");
    }


    private void calcBill(JTable table1, JLabel l1) {
        finalBill = 0.0; // reset before recalculation
        if (table1.getRowCount() == 0) {
            l1.setText("Total Bill : 0.0");
        } else {
            for (int i = 0; i < table1.getRowCount(); i++) {
                finalBill += Double.parseDouble((String) table1.getValueAt(i, 4));
            }
            l1.setText("Total Bill : " + finalBill);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Bill(new JTable(), "Ashlesha", "9876543210");
        });

    }
}