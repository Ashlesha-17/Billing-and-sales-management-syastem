import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class Month extends JFrame {

    Font f2 = new Font("Tahoma", Font.PLAIN, 22);
    Font f4 = new Font("Tahoma", Font.PLAIN, 28);

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel titleLabel;
    private JComboBox<String> monthSelector;

    Month() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());

        titleLabel = new JLabel("Sales on Month");
        titleLabel.setOpaque(true);
        titleLabel.setFont(f4);
        titleLabel.setBackground(new Color(180, 88, 234));
        titleLabel.setForeground(Color.white);
        titleLabel.setPreferredSize(new Dimension(600, 100));
        titleLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Month Selector
        String[] months = {"January","February","March","April","May","June",
                "July","August","September","October","November","December"};
        monthSelector = new JComboBox<>(months);
        monthSelector.setFont(f4);
        monthSelector.setPreferredSize(new Dimension(200, 50));
        topPanel.add(monthSelector, BorderLayout.EAST);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Center Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        String[] columns = {"Bill Id", "Name", "Contact No.", "No of Pdts", "Amount", "Show Bill"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // only Show Bill checkbox editable
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 5) ? Boolean.class : String.class;
            }
        };
        table = new JTable(tableModel);
        table.setFont(f2);
        table.setRowHeight(35);
        table.getTableHeader().setFont(f4);
        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton back = new JButton("Back");
        back.setFont(f2);
        back.setPreferredSize(new Dimension(250, 50));
        back.setBackground(new Color(180, 88, 234));
        back.setForeground(Color.white);
        back.addActionListener(a -> dispose()); // Replace with new Home() if needed
        bottomPanel.add(back);

        // Container
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(topPanel, BorderLayout.NORTH);
        c.add(centerPanel, BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.SOUTH);

        // Load initial month
        loadData(monthSelector.getSelectedIndex() + 1);
        titleLabel.setText("Sales in " + months[monthSelector.getSelectedIndex()]);

        // Month change listener
        monthSelector.addActionListener(a -> {
            int month = monthSelector.getSelectedIndex() + 1;
            titleLabel.setText("Sales in " + months[month - 1]);
            loadData(month);
        });

        // Checkbox listener
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 5) {
                    Boolean checked = (Boolean) tableModel.getValueAt(row, col);
                    if (checked != null && checked) {
                        int billId = Integer.parseInt(table.getValueAt(row, 0).toString());
                        String customerName = table.getValueAt(row, 1).toString();
                        String contactNo = table.getValueAt(row, 2).toString();
                        String totalQty = table.getValueAt(row, 3).toString();
                        String amountStr = table.getValueAt(row, 4).toString();

                        showBillForCustomer(customerName, contactNo, totalQty, billId, amountStr);
                        tableModel.setValueAt(false, row, col);
                    }
                }
            }
        });

        setVisible(true);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadData(int month) {
        tableModel.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/31may", "root", "Hope$02")) {
            String sql = "SELECT * FROM bills WHERE MONTH(bill_datetime)=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, month);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("bill_id");
                String name = rs.getString("customer_name");
                String contact = rs.getString("contact_no");
                int qty = rs.getInt("total_quantity");
                double amount = rs.getDouble("Total_amount");
                tableModel.addRow(new Object[]{id, name, contact, String.valueOf(qty), String.valueOf(amount), false});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void showBillForCustomer(String customerName, String contactNo, String totalQty, int billId, String totalAmount) {
        JFrame billFrame = new JFrame("Bill for " + customerName);
        billFrame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Bill for " + customerName);
        title.setOpaque(true);
        title.setFont(f4);
        title.setBackground(new Color(115, 49, 159));
        title.setForeground(Color.white);
        title.setBorder(new EmptyBorder(20, 20, 20, 20));
        topPanel.add(title);

        JLabel qtyLabel = new JLabel("Total Products: " + totalQty);
        qtyLabel.setOpaque(true);
        qtyLabel.setFont(f4);
        qtyLabel.setBackground(new Color(115, 49, 159));
        qtyLabel.setForeground(Color.white);
        qtyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        topPanel.add(qtyLabel);

        String[] cols = {"No.", "Name", "Category", "Quantity", "Price"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable billTable = new JTable(model);
        billTable.setFont(f2);
        billTable.setRowHeight(35);
        billTable.getTableHeader().setFont(f4);
        JScrollPane scrollPane = new JScrollPane(billTable);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/31may", "root", "Hope$02")) {
            String sql = "SELECT * FROM bill_items WHERE bill_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, billId);
            ResultSet rs = pst.executeQuery();
            int c = 1;
            while (rs.next()) {
                String pname = rs.getString("product_name");
                String cat = rs.getString("category");
                int pqty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                model.addRow(new Object[]{c++, pname, cat, pqty, price});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel total = new JLabel("Total: " + totalAmount);
        total.setOpaque(true);
        total.setFont(f4);
        total.setBackground(new Color(115, 49, 159));
        total.setForeground(Color.white);
        total.setPreferredSize(new Dimension(250, 50));

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("Back");
        back.setFont(f2);
        back.setBackground(new Color(133, 128, 60));
        back.setForeground(Color.white);
        back.setPreferredSize(new Dimension(200, 50));
        back.addActionListener(a -> billFrame.dispose());
        backPanel.add(back);

        bottomPanel.add(backPanel, BorderLayout.CENTER);
        bottomPanel.add(total, BorderLayout.WEST);

        billFrame.add(topPanel, BorderLayout.NORTH);
        billFrame.add(scrollPane, BorderLayout.CENTER);
        billFrame.add(bottomPanel, BorderLayout.SOUTH);

        billFrame.setSize(800, 800);
        billFrame.setLocationRelativeTo(null);
        billFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new Month();
    }
}
