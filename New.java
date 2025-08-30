import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

class New extends JFrame {
    int StockQ = 0;
    JTable pdtTable;
    DefaultTableModel model;
    private Map<String, Integer> selectedQuantities = new HashMap<>();
    static boolean billStarted = false;
    private boolean fromBill = false;

    // keep all products in memory
    List<Object[]> allProducts = new ArrayList<>();

    public New(DefaultTableModel existingModel,boolean fromBill) {
        this.model = existingModel;
        this.fromBill = fromBill;
        setTitle("Adding pdts");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pdtTable = new JTable(existingModel);
        JScrollPane scrollPane = new JScrollPane(pdtTable);

        add(scrollPane);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    New(){
        this(false);
    }
    New(boolean fromBill) {
        this.fromBill = fromBill;
        Font f1 = new Font("Arial", Font.BOLD, 40);
        Font f2 = new Font("Tahoma", Font.PLAIN, 22);
        Font f3 = new Font("Tahoma", Font.BOLD, 22);
        Font f4 = new Font("Tahoma", Font.PLAIN, 25);

        JButton b1 = new JButton("Stock");
        b1.setFont(f2);
        b1.setBackground(new Color(0x7D7DE8));
        b1.addActionListener(a -> {
            new Stock1(this);
            this.setVisible(false);
        });
        b1.setPreferredSize(new Dimension(150, 50));

        JTextField search = new JTextField(10);
        search.setFont(f2);
        search.setPreferredSize(new Dimension(150, 50));
        search.setMaximumSize(new Dimension(150, 40));

        JButton b2 = new JButton("Search");
        b2.setFont(f2);
        b2.setBackground(new Color(0x8F8FEA));
        b2.setPreferredSize(new Dimension(150, 50));

        JButton backBtn = new JButton("Back");
        backBtn.setFont(f2);
        backBtn.setBackground(new Color(0xD3D3D3));
        backBtn.setPreferredSize(new Dimension(150, 50));

        ImageIcon icon = new ImageIcon(getClass().getResource("SearchIcon.png"));
        Image image = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);

        JLabel logoLabel = new JLabel(scaledIcon);

        // top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel l1 = new JLabel("Invoice Setup", JLabel.CENTER);
        l1.setForeground(new Color(0xFFF3FDF4, true));
        l1.setFont(f2);
        l1.setOpaque(true);
        l1.setBackground(new Color(0x55BA66));
        l1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // customer info
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        JLabel l2 = new JLabel("Customers details");
        l2.setFont(f4);
        l2.setOpaque(true);
        l2.setBackground(new Color(0xB0F3D9));

        JLabel l4 = new JLabel("Contact No.");
        JLabel l3 = new JLabel("Name ");
        l3.setFont(f4);
        l3.setOpaque(true);
        l3.setBackground(new Color(0x76CFAB));
        JTextField t1 = new JTextField(10);
        t1.setFont(f2);
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(l3);
        row1.add(t1);

        l4.setFont(f4);
        l4.setOpaque(true);
        l4.setBackground(new Color(0x76CFAB));
        JTextField t2 = new JTextField(10);
        t2.setFont(f2);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(l4);
        row2.add(t2);

        p1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p1.add(l2, BorderLayout.WEST);
        p1.add(Box.createVerticalStrut(10));
        p1.add(row1);
        p1.add(Box.createVerticalStrut(5));
        p1.add(row2);

        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.X_AXIS));
        wrapperPanel.add(p1);
        wrapperPanel.add(Box.createHorizontalStrut(20));
        wrapperPanel.add(search);
        wrapperPanel.add(Box.createHorizontalStrut(20));
        wrapperPanel.add(b2);
        wrapperPanel.add(Box.createHorizontalStrut(2));
        wrapperPanel.add(logoLabel);
        wrapperPanel.add(Box.createHorizontalStrut(20));
        wrapperPanel.add(backBtn);
        wrapperPanel.add(Box.createHorizontalStrut(70));
        wrapperPanel.add(b1);

        topPanel.add(l1, BorderLayout.NORTH);
        topPanel.add(p1);
        topPanel.add(wrapperPanel, BorderLayout.EAST);

        // table model
        String[] columnNames = {"No.", "Name", "Category", "Quantity", "Update Qty", "Select"};
        model = new DefaultTableModel(columnNames, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return Boolean.class;
                }
                if (columnIndex == 4) {
                    return Object.class;
                }
                return String.class;
            }
        };

        JTable table = new JTable(model);
        table.setFont(f2);
        table.setRowHeight(35);
        table.getTableHeader().setFont(f3);
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonPanelEditor(table));
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonPanelRenderer());

        model.addTableModelListener(a -> {
            int row = a.getFirstRow();
            int column = a.getColumn();

            if (column == 5) {
                Boolean selected = (Boolean) model.getValueAt(row, column);
                if (selected != null && selected) {
                    model.setValueAt("1", row, 3);
                } else {
                    model.setValueAt("", row, 3);
                }
            }
        });

        // load all products initially
        loadAllProducts();
        showAllProducts();

        // search button
        b2.addActionListener(a -> {
            String searchText = search.getText().trim().toLowerCase();
            if (searchText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter product name to search!");
                return;
            }
            searchProducts(searchText);
        });

        // back button
        backBtn.addActionListener(a -> showAllProducts());

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(1200, 70));
        bottomPanel.setLayout(new BorderLayout());
        JButton bill = new JButton("Get Bill");
        bill.setFont(f2);
        bill.setBackground(new Color(0x1E651E));
        bill.setForeground(Color.white);
        bill.setPreferredSize(new Dimension(200, 50));
        bill.addActionListener(a -> {
            String custName = t1.getText().trim();
            String contact = t2.getText().trim();

            if(!billStarted){
                // validate once here only
                if (custName.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Enter name of Customer!");
                    return;
                }
                if (!contact.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(null, "Enter valid Contact Number!");
                    return;
                }
            }
            billStarted = true;
            new Bill(table, custName, contact);
//            this.dispose(); // optional: close current window after opening bill
        });


        JButton home = new JButton("Home");
        home.setFont(f2);
        home.setBackground(new Color(0xACACEF));
        home.setForeground(Color.white);
        home.setPreferredSize(new Dimension(150, 50));
        home.addActionListener(a -> {
            this.dispose();
            new Home();
        });

        JPanel billPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        billPanel.add(bill);

        JPanel homePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        homePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        homePanel.add(home);

        bottomPanel.add(billPanel, BorderLayout.CENTER);
        bottomPanel.add(homePanel, BorderLayout.EAST);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        c.add(topPanel, BorderLayout.NORTH);
        c.add(centerPanel, BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("NEW");
    }

    // === utility methods ===

    private void loadAllProducts() {
        allProducts.clear();
        String url = "jdbc:mysql://localhost:3306/31may";
        try (Connection con = DriverManager.getConnection(url, "root", "Hope$02")) {
            String sql = "select * from Stock";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                ResultSet rs = pst.executeQuery();
                int count = 1;
                while (rs.next()) {
                    String Name = rs.getString("Name");
                    String Cat = rs.getString("Category");
                    int quan = rs.getInt("Quantity");
                    if (quan == 0) continue;
                    allProducts.add(new Object[]{count++, Name, Cat, "", null, false});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void showAllProducts() {
        model.setRowCount(0);
        for (Object[] row : allProducts) {
            String name = row[1].toString();
            Object[] newRow = row.clone();

            // restore qty if exists
            if (selectedQuantities.containsKey(name)) {
                int qty = selectedQuantities.get(name);
                newRow[3] = String.valueOf(qty);
                newRow[5] = qty > 0;
            }

            model.addRow(newRow);
        }
    }

    private void searchProducts(String keyword) {
        model.setRowCount(0);
        for (Object[] row : allProducts) {
            if (row[1].toString().toLowerCase().contains(keyword) ||
                    row[2].toString().toLowerCase().contains(keyword)) {

                String name = row[1].toString();
                Object[] newRow = row.clone();

                // restore qty if exists
                if (selectedQuantities.containsKey(name)) {
                    int qty = selectedQuantities.get(name);
                    newRow[3] = String.valueOf(qty);
                    newRow[5] = qty > 0;
                }

                model.addRow(newRow);
            }
        }
    }

    // === editors/renderers ===

    class ButtonPanelEditor extends AbstractCellEditor implements TableCellEditor{
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        private final JButton plus = new JButton("+");
        private final JButton minus = new JButton("-");
        private JTable table;
        private int row;

        public ButtonPanelEditor(JTable table) {
            this.table = table;
            plus.addActionListener(a -> updateQ(1));
            minus.addActionListener(a -> updateQ(-1));
            panel.add(plus);
            panel.add(minus);
        }

        private void updateQ(int delta) {
            int currentQty = 0;
            Object qtyObj = table.getValueAt(row, 3);
            if (qtyObj != null && !qtyObj.toString().isEmpty()) {
                try {
                    currentQty = Integer.parseInt(qtyObj.toString());
                } catch (NumberFormatException ignored) {}
            }

            int newQty = currentQty + delta;
            if (newQty < 0) newQty = 0;

            String name = (String) table.getValueAt(row, 1);
            String url = "jdbc:mysql://localhost:3306/31may";
            try (Connection con = DriverManager.getConnection(url, "root", "Hope$02")) {
                String sql = "select Quantity from Stock WHERE Name = ?";
                try (PreparedStatement pstt = con.prepareStatement(sql)) {
                    pstt.setString(1, name);
                    ResultSet rs = pstt.executeQuery();

                    if (rs.next()) {
                        StockQ = rs.getInt("Quantity");
                    }
                    if (newQty > StockQ) {
                        JOptionPane.showMessageDialog(null, "Pdt " + name + " is not available. Available units are " + StockQ);
                        return;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                return;
            }
            table.setValueAt(String.valueOf(newQty), row, 3);
            table.setValueAt(newQty > 0, row, 5);
            selectedQuantities.put(name, newQty);
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return panel;
        }
    }


    class ButtonPanelRenderer extends JPanel implements TableCellRenderer {
        private final JButton plus = new JButton("+");
        private final JButton minus = new JButton("-");

        public ButtonPanelRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            add(plus);
            add(minus);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Boolean selected = (Boolean) table.getValueAt(row, 5);
            setVisible(selected != null && selected);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new New();
        });
    }
}
