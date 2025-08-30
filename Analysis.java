import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

// Frame to show total sales for a selected month
class TotalSalesFrame extends JFrame {
    Font f2 = new Font("Tahoma", Font.PLAIN, 22);

    TotalSalesFrame(int month) {
        setTitle("Total Sales Details");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] columns = { "Product Name", "Quantity Sold" };
        Object[][] data = fetchTotalSales(month);

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.getTableHeader().setBackground(new Color(211, 94, 146));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 18));
        table.setFont(new Font("Tahoma", Font.PLAIN, 18));
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private Object[][] fetchTotalSales(int month) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/31may", "root", "Hope$02"
            );

            String query = "SELECT bi.product_name, SUM(bi.quantity) AS SoldQty " +
                    "FROM bill_items bi " +
                    "JOIN bills b ON bi.bill_id = b.bill_id " +
                    "WHERE MONTH(b.bill_datetime) = ? " +
                    "GROUP BY bi.product_name";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, month);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String product = rs.getString("product_name");
                int qty = rs.getInt("SoldQty");
                list.add(new Object[]{ product, qty });
            }

            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage());
        }

        Object[][] data = new Object[list.size()][2];
        for (int i = 0; i < list.size(); i++) data[i] = list.get(i);
        return data;
    }
}

// Frame to show top 5 selling products for a selected month
class TopProductsFrame extends JFrame {
    Font f2 = new Font("Tahoma", Font.PLAIN, 22);

    TopProductsFrame(int month) {
        setTitle("Top Selling Products");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] columns = { "Product Name", "Quantity Sold" };
        Object[][] data = fetchTopProducts(month);

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.getTableHeader().setBackground(new Color(211, 94, 146));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 18));
        table.setFont(new Font("Tahoma", Font.PLAIN, 18));
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private Object[][] fetchTopProducts(int month) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/31may", "root", "Hope$02"
            );

            String query = "SELECT bi.product_name, SUM(bi.quantity) AS SoldQty " +
                    "FROM bill_items bi " +
                    "JOIN bills b ON bi.bill_id = b.bill_id " +
                    "WHERE MONTH(b.bill_datetime) = ? " +
                    "GROUP BY bi.product_name " +
                    "ORDER BY SoldQty DESC " +
                    "LIMIT 5";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, month);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String product = rs.getString("product_name");
                int qty = rs.getInt("SoldQty");
                list.add(new Object[]{ product, qty });
            }

            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage());
        }

        Object[][] data = new Object[list.size()][2];
        for (int i = 0; i < list.size(); i++) data[i] = list.get(i);
        return data;
    }
}

// Main Analysis frame
public class Analysis extends JFrame {
    JPanel chartPanel;

    Analysis() {
        Font f2 = new Font("Tahoma", Font.PLAIN, 22);

        // --- Top Panel ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(163, 14, 81));
        topPanel.setPreferredSize(new Dimension(1200, 120));

        JLabel titleLabel = new JLabel("Sales Analysis :");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 28));
        titleLabel.setForeground(Color.white);
        titleLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] months = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };
        JComboBox<String> monthSelector = new JComboBox<>(months);
        monthSelector.setFont(new Font("Tahoma", Font.PLAIN, 20));
        monthSelector.setBackground(Color.white);
        monthSelector.setForeground(Color.black);
        monthSelector.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel monthPanel = new JPanel();
        monthPanel.setOpaque(false);
        monthPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        monthPanel.add(monthSelector);
        topPanel.add(monthPanel, BorderLayout.EAST);

        monthSelector.addActionListener(e -> {
            String selectedMonth = (String) monthSelector.getSelectedItem();
            titleLabel.setText("Sales Analysis : " + selectedMonth);
            updateChartAndPercent(selectedMonth);
        });

        // --- Center Panel ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        JButton totalSalesButton = new JButton("Total Sales");
        totalSalesButton.setFont(f2);
        totalSalesButton.setBackground(new Color(0xAA075E));
        totalSalesButton.setForeground(Color.white);
        JButton topProductsButton = new JButton("Top Selling Products");
        topProductsButton.setFont(f2);
        topProductsButton.setBackground(new Color(0xAA075E));
        topProductsButton.setForeground(Color.white);

        buttonPanel.add(totalSalesButton);
        buttonPanel.add(topProductsButton);
        centerPanel.add(buttonPanel, BorderLayout.NORTH);

        chartPanel = new JPanel();
        chartPanel.setPreferredSize(new Dimension(1000, 400));
        chartPanel.setBackground(new Color(220, 220, 220));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Chart/Graph"));
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(topPanel, BorderLayout.NORTH);
        c.add(centerPanel, BorderLayout.CENTER);

        setTitle("Analysis");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);

        // --- Button actions ---
        totalSalesButton.addActionListener(e -> {
            int selectedMonth = monthSelector.getSelectedIndex() + 1;
            new TotalSalesFrame(selectedMonth);
        });

        topProductsButton.addActionListener(e -> {
            int selectedMonth = monthSelector.getSelectedIndex() + 1;
            new TopProductsFrame(selectedMonth);
        });
    }

    void updateChartAndPercent(String month) {
        chartPanel.removeAll();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/31may", "root", "Hope$02"
            );

            String query = "SELECT bi.product_name, SUM(bi.quantity) AS SoldQty " +
                    "FROM bill_items bi " +
                    "JOIN bills b ON bi.bill_id = b.bill_id " +
                    "WHERE MONTH(b.bill_datetime) = ? " +
                    "GROUP BY bi.product_name " +
                    "ORDER BY SoldQty DESC LIMIT 5";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, java.time.Month.valueOf(month.toUpperCase()).getValue());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String product = rs.getString("product_name");
                int qty = rs.getInt("SoldQty");
                dataset.addValue(qty, "Products", product);
            }

            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Top Selling Products - " + month,
                "Product",
                "Quantity Sold",
                dataset
        );

        ChartPanel jfreeChartPanel = new ChartPanel(barChart);
        jfreeChartPanel.setPreferredSize(new Dimension(900, 400));
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    public static void main(String[] args) {
        new Analysis();
    }
}
