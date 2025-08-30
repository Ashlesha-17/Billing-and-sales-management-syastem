import javax.swing.*;
import java.sql.*;
import java.awt.*;

class Stock extends JFrame {
    Stock()
    {
        Font f1 = new Font("Arial", Font.BOLD, 40);
        Font f2 = new Font("Tahoma", Font.PLAIN, 22);
        Font f3 = new Font("Tahoma", Font.BOLD, 22);

        JLabel l1 = new JLabel("Name");
        JLabel l2 = new JLabel("Cost Price");
        JLabel l3 = new JLabel("Price");
        JLabel l4 = new JLabel("Quantity");
        JLabel l5 = new JLabel("Category");

        JTextField t1 = new JTextField(10);
        JTextField t2 = new JTextField(10);
        JTextField t3 = new JTextField(10);
        JTextField t4 = new JTextField(10);
        JComboBox<String> catBox = new JComboBox<>();
        catBox.setEditable(false);

        JButton b1 = new JButton("Add to Stock");
        JButton b2 = new JButton("View Stock");
        JButton b3 = new JButton("Home");

        l1.setFont(f2);
        l2.setFont(f2);
        l3.setFont(f2);
        l4.setFont(f2);
        l5.setFont(f2);

        t1.setFont(f2);
        t2.setFont(f2);
        t3.setFont(f2);
        t4.setFont(f2);
        catBox.setFont(f2);

        b1.setFont(f3);
        b2.setFont(f3);
        b3.setFont(f2);

        l1.setBounds(400,100,270,40);
        l2.setBounds(400,170,270,40);
        l3.setBounds(400,240,270,40);
        l4.setBounds(400,310,270,40);
        l5.setBounds(400,390,270,40);

        t1.setBounds(600,100,270,40);
        t2.setBounds(600,170,270,40);
        t3.setBounds(600,240,270,40);
        t4.setBounds(600,310,270,40);
        catBox.setBounds(600,390,270,40);

        b1.setBounds(500,500,300,50);
        b2.setBounds(500,600,300,50);
        b3.setBounds(1050,650,100,50);

        b1.setBackground(new Color(0x51F397));
        b2.setBackground(new Color(0xE0F43131, true));
        b3.setBackground(new Color(0xACACEF));

        Container c = getContentPane();
        c.setLayout(null);
        catBox.addItem("--Select Category--"); // Placeholder

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/31may", "root", "Hope$02")) {
            String catSQL = "SELECT DISTINCT Category FROM Stock";
            try (PreparedStatement pst = con.prepareStatement(catSQL)) {
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    String category = rs.getString("Category");
                    catBox.addItem(category);  // <-- adds to combo box
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        catBox.setSelectedIndex(0);
        catBox.addFocusListener(new java.awt.event.FocusAdapter(){
            @Override
            public void focusGained(java.awt.event.FocusEvent e ){
                if("--Select Category--".equals(catBox.getSelectedItem())){
                    catBox.insertItemAt("",0);
                    catBox.removeItem("--Select Category--");
                    catBox.setSelectedIndex(0);
                }
            }
        });
        catBox.setEditable(true);
        catBox.getEditor().setItem("--Select Category--");
        catBox.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("--Select Category--".equals(catBox.getEditor().getItem().toString())) {
                    catBox.getEditor().setItem("");
                }
            }
        });

        c.add(l1);
        c.add(l2);
        c.add(l3);
        c.add(l4);
        c.add(l5);
        c.add(t1);
        c.add(t2);
        c.add(t3);
        c.add(t4);
        c.add(catBox);
        c.add(b1);
        c.add(b2);
        c.add(b3);

        //Inserting into stock
        b1.addActionListener(
                a->{
                    String url = "jdbc:mysql://localhost:3306/31may";

                    try (Connection con = DriverManager.getConnection(url,"root","Hope$02")){
                        String sql = "INSERT INTO Stock(Name, CostP, Price, Quantity, Category) VALUES(?,?,?,?,?)";
                        String sql1 = "select Quantity from Stock where Name = ?";
                        try(PreparedStatement pst1 = con.prepareStatement(sql1)){
                            pst1.setString(1,t1.getText());
                            ResultSet rs1 = pst1.executeQuery();
                            if(rs1.next()){
                                int existingQ = rs1.getInt("Quantity");
                                int updatedQ = existingQ + Integer.parseInt(t4.getText());

                                String sql2 = "update Stock set Quantity = ? where Name = ?";
                                try(PreparedStatement pst2 = con.prepareStatement(sql2)){
                                    pst2.setInt(1,updatedQ);
                                    pst2.setString(2,t1.getText());
                                    pst2.executeUpdate();
                                }
                            }
                            else{
                                try(PreparedStatement pst = con.prepareStatement(sql))
                                {
                                    pst.setString(1,t1.getText());
                                    pst.setString(2,t2.getText());
                                    pst.setString(3,t3.getText());
                                    pst.setString(4,t4.getText());
                                    pst.setString(5, catBox.getSelectedItem().toString());

                                    pst.executeUpdate();
                                    new Stock();
                                    dispose();
                                }
                            }
                            }
                        }

                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(null,e.getMessage());
                    }
                    JOptionPane.showMessageDialog(null,"Added to Stock!");
                }
        );

        b3.addActionListener(
                a->{
                    new Home();
                }
        );

        b2.addActionListener(
                a-> {
                    new Stock1(this);
                    this.setVisible(false);
                }
        );

        setVisible(true);
        setSize(1200,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Stock");
    }
    public static void main(String[] args) {
        new Stock();
    }
}