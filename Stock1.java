import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.*;

class Stock1 extends JFrame {
    JFrame caller;
    Stock1(JFrame caller)
    {
        this.caller = caller;
        Font f1 = new Font("Arial", Font.BOLD, 40);
        Font f2 = new Font("Tahoma", Font.PLAIN, 22);
        Font f3 = new Font("Tahoma", Font.BOLD, 22);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.setSize(1200,800);

        JButton B1 = new JButton("BACK");
        B1.setBounds(200,700,300,50);
        B1.setFont(f3);
        JLabel title = new JLabel("Stock",JLabel.CENTER);
        title.setFont(f3);
        title.setBackground(new Color(0xf1A483));
        title.setOpaque(true);
        title.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        String[] columnNames = {"No.", "Name","Cost Price", "Price", "Quantity","Category"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames,0)
        {
            public boolean isCellEditable(int row, int columns){return false;}
        };
        JTable table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(f2);
        table.setRowHeight(30);
        table.getTableHeader().setFont(f2);
        table.setBackground(new Color(0xF1F1C8));

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0xE0EDC07C, true));
        topPanel.add(title,BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0xE0EDC07C, true));
        bottomPanel.add(B1);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(241, 241, 200));

        //search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,20,10));

        JTextField search = new JTextField(10);
        search.setFont(f2);
        search.setPreferredSize(new Dimension(150, 50));
        searchPanel.add(search);

        JComboBox<String> categoryBox = new JComboBox<>();
        categoryBox.setVisible(false);
        categoryBox.setPreferredSize(new Dimension(150, 50));
        categoryBox.setFont(f2);

        String url = "jdbc:mysql://localhost:3306/31may";
        String catSql = "SELECT DISTINCT Category FROM Stock";
        try (Connection con = DriverManager.getConnection(url, "root", "Hope$02")){
            try (PreparedStatement pstCat = con.prepareStatement(catSql)) {
                ResultSet rsCat = pstCat.executeQuery();
                categoryBox.addItem("Select");
                while (rsCat.next()) {
                    categoryBox.addItem(rsCat.getString("Category"));
                }
            }
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null,e.getMessage());
        }



        JButton b1 = new JButton("Search");
        b1.setFont(f2);
        b1.setBackground(new Color(0x8F8FEA));
        b1.setPreferredSize(new Dimension(150, 50));

        ImageIcon icon = new ImageIcon(getClass().getResource("SearchIcon.png"));
        Image image = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        JLabel logoLabel = new JLabel(scaledIcon);



        String[] options = {"Name", "Category"};
        JComboBox<String> searchType = new JComboBox<>(options);
        searchType.setFont(f2);
        searchType.setPreferredSize(new Dimension(150, 50));
        searchType.addActionListener(e -> {
            String selected = (String) searchType.getSelectedItem();
            if ("Category".equals(selected)) {
                search.setVisible(false);
                categoryBox.setVisible(true);
            } else {
                search.setVisible(true);
                categoryBox.setVisible(false);
            }
        });


        b1.addActionListener(
                a->{
                    String selectedType = (String) searchType.getSelectedItem();
                    if("Category".equals(selectedType)){
                        String categoryValue = (String) categoryBox.getSelectedItem();
                        if(categoryValue==null || "Select".equals(categoryValue)){
                            JOptionPane.showMessageDialog(null,"Select a category to search!");
                            return;
                        }
                        else {
                            String sql = "SELECT * FROM Stock WHERE Category = ?";
                            try (Connection con = DriverManager.getConnection(url, "root", "Hope$02");
                                 PreparedStatement pst = con.prepareStatement(sql)) {

                                pst.setString(1, categoryValue);

                                // Clear old table data
                                tableModel.setRowCount(0);

                                try (ResultSet rs = pst.executeQuery()) {
                                    int count = 1;
                                    while (rs.next()) {
                                        String s1 = rs.getString("Name");
                                        double d1 = rs.getDouble("Price");
                                        int Q = rs.getInt("Quantity");
                                        String s2 = rs.getString("Category");
                                        double d2 = rs.getDouble("CostP");

                                        tableModel.addRow(new Object[]{count++, s1, d2, d1, Q, s2});
                                    }
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, ex.getMessage());
                            }

                        }
                    }
                    else{
                        String nameValue = search.getText().trim();
                        if (nameValue.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Enter a name to search!");
                            return;
                        }

                        String sql = "SELECT * FROM Stock WHERE Name LIKE ?";
                        try (Connection con = DriverManager.getConnection(url, "root", "Hope$02");
                             PreparedStatement pst = con.prepareStatement(sql)) {

                            pst.setString(1, "%" + nameValue + "%");

                            // Clear old table data
                            tableModel.setRowCount(0);

                            try (ResultSet rs = pst.executeQuery()) {
                                int count = 1;
                                while (rs.next()) {
                                    String s1 = rs.getString("Name");
                                    double d1 = rs.getDouble("Price");
                                    int Q = rs.getInt("Quantity");
                                    String s2 = rs.getString("Category");
                                    double d2 = rs.getDouble("CostP");

                                    tableModel.addRow(new Object[]{count++, s1, d2, d1, Q, s2});
                                }
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage());
                        }

                    }

                }
        );

        searchPanel.add(searchType);
        searchPanel.add(categoryBox);
        searchPanel.add(b1);
        searchPanel.add(logoLabel);
        // end of search panel

        centerPanel.add(searchPanel,BorderLayout.NORTH);
        centerPanel.add(scrollPane,BorderLayout.CENTER);

        //Displaying stock
        try (Connection con = DriverManager.getConnection(url, "root", "Hope$02")) {
            String sql = "select * from Stock";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                ResultSet rs = pst.executeQuery();

                int count = 1;
                while (rs.next())
                {
                    String s1 = rs.getString("Name");
                    double d1 = rs.getDouble("Price");
                    int Q = rs.getInt("Quantity");
                    String s2 = rs.getString("category");
                    double d2 = rs.getDouble("CostP");

                    tableModel.addRow(new Object[]{count++,s1,d2,d1,Q,s2});
                }
            }

        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        B1.addActionListener(
                b->{
                    if (caller != null){
                        caller.setVisible(true);
                    }
                    dispose();
                }
        );
        c.add(topPanel,BorderLayout.NORTH);
        c.add(bottomPanel,BorderLayout.SOUTH);
        c.add(centerPanel,BorderLayout.CENTER);

        setVisible(true);
        setSize(1200,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Stock1");

    }
    public static void main(String[] args) {
        new Stock1(null);
    }
}