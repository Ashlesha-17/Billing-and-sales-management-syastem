import javax.swing.*;
import java.sql.*;
import java.awt.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import javax.swing.border.EmptyBorder;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.text.SimpleDateFormat;
import java.util.Date;

class Today extends JFrame {

    Font f2 = new Font("Tahoma", Font.PLAIN, 22);
    Font f4 = new Font("Tahoma", Font.PLAIN, 28); // Bigger font for title

    private JDateChooser dateChooser;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel titleLabel;

    Today() {
        String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // top panel
        JPanel topPanel = new JPanel(new BorderLayout());

        titleLabel = new JLabel("Sales on " + todayStr);
        titleLabel.setOpaque(true);
        titleLabel.setFont(f4);
        titleLabel.setBackground(new Color(0xE0C058F3, true));
        titleLabel.setForeground(Color.white);
        titleLabel.setPreferredSize(new Dimension(600, 70));
        titleLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Date chooser
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setFont(f4);
        dateChooser.setDate(new Date()); // default date today

        // Hide the text field completely
        JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooser.getDateEditor();
        editor.setEditable(false);
        editor.setVisible(false);

        // Access the calendar button and make it bigger
        for (Component comp : dateChooser.getComponents()) {
            if (comp instanceof JButton) {
                JButton calendarButton = (JButton) comp;
                calendarButton.setPreferredSize(new Dimension(120, 50));
            }
        }

        topPanel.add(dateChooser, BorderLayout.EAST);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Bill Id", "Name", "Contact No.", "No of Pdts", "Amount", "Show Bill"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
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

        // bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton back = new JButton("Back");
        back.setFont(f2);
        back.setPreferredSize(new Dimension(250, 50));
        back.setBackground(new Color(0xE0C058F3, true));
        back.setForeground(Color.white);
        back.addActionListener(a -> new Home());
        bottomPanel.add(back);

        // container
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(topPanel, BorderLayout.NORTH);
        c.add(centerPanel, BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.SOUTH);

        // Load initial data
        loadData(todayStr);

        // Update title and reload data when date changes
        dateChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                Date selected = dateChooser.getDate();
                if (selected != null) {
                    String selectedDate = new SimpleDateFormat("yyyy-MM-dd").format(selected);
                    titleLabel.setText("Sales on " + selectedDate);
                    setTitle("Sales on " + selectedDate);
                    loadData(selectedDate);
                }
            }
        });

        setVisible(true);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadData(String date) {
        tableModel.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/31may", "root", "Hope$02")) {
            String sql = "select * from bills WHERE DATE(bill_datetime) = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, date);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("bill_id");
                    String name = rs.getString("customer_name");
                    String contact = rs.getString("contact_no");
                    int Q = rs.getInt("total_quantity");
                    Double amount = rs.getDouble("Total_amount");
                    tableModel.addRow(new Object[]{id, name, contact, String.valueOf(Q), String.valueOf(amount), false});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        // listen for checkbox "Show Bill"
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 5) {
                    Boolean checked = (Boolean) tableModel.getValueAt(row, col);
                    if (checked) {
                        int billId = (Integer) table.getValueAt(row, 0);
                        String customerName = (String) table.getValueAt(row, 1);
                        String totalQty = String.valueOf(table.getValueAt(row, 3));
                        String contactNo = (String) table.getValueAt(row, 2);
                        String amountStr = String.valueOf(table.getValueAt(row, 4));

                        showBillForCustomer(customerName, contactNo, totalQty, billId, amountStr);
                        tableModel.setValueAt(false, row, col);
                    }
                }
            }
        });
    }

    private void showBillForCustomer(String customerName, String contactNo, String totalQty, int billId, String totalAmount) {
        JFrame billFrame = new JFrame("Bill for " + customerName);
        billFrame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Bill for " + customerName);
        title.setOpaque(true);
        title.setFont(f4);
        title.setBackground(new Color(0xE073149F, true));
        title.setForeground(Color.white);
        title.setPreferredSize(new Dimension(400, 50));
        title.setBorder(new EmptyBorder(20, 20, 20, 20));
        topPanel.add(title);

        JLabel qtyLabel = new JLabel("Total Products: " + totalQty);
        qtyLabel.setOpaque(true);
        qtyLabel.setFont(f4);
        qtyLabel.setBackground(new Color(0xE073149F, true));
        qtyLabel.setForeground(Color.white);
        qtyLabel.setPreferredSize(new Dimension(400, 50));
        qtyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        topPanel.add(qtyLabel);

        String[] columnNames = {"No.", "Name", "Category", "Quantity", "Price"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        table.setFont(f2);
        table.setRowHeight(35);
        table.getTableHeader().setFont(f4);
        JScrollPane scrollPane = new JScrollPane(table);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/31may", "root", "Hope$02")) {
            String sql = "select * from bill_items WHERE bill_id = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, billId);
                ResultSet rs = pst.executeQuery();
                int c = 1;
                while (rs.next()) {
                    String name1 = rs.getString("product_name");
                    String cat = rs.getString("category");
                    int Q = rs.getInt("quantity");
                    Double price = rs.getDouble("price");
                    tableModel.addRow(new Object[]{c++, name1, cat, String.valueOf(Q), String.valueOf(price)});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel total = new JLabel("Total: " + totalAmount);
        total.setOpaque(true);
        total.setFont(f4);
        total.setBackground(new Color(0xE073149F, true));
        total.setForeground(Color.white);
        total.setPreferredSize(new Dimension(250, 50));

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("Back");
        back.setBackground(new Color(0x85803C));
        back.setForeground(Color.white);
        back.setFont(f2);
        back.setPreferredSize(new Dimension(200, 50));
        back.addActionListener(a -> billFrame.dispose());
        backPanel.add(back);

        bottomPanel.add(backPanel, BorderLayout.CENTER);
        bottomPanel.add(total, BorderLayout.WEST);

        billFrame.add(topPanel, BorderLayout.NORTH);
        billFrame.add(centerPanel, BorderLayout.CENTER);
        billFrame.add(bottomPanel, BorderLayout.SOUTH);

        billFrame.setSize(800, 800);
        billFrame.setLocationRelativeTo(null);
        billFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        billFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new Today();
    }
}
