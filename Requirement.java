import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.*;

class Requirement extends JFrame {
    Requirement(){
        Font f1 = new Font("Arial", Font.BOLD, 40);
        Font f2 = new Font("Tahoma", Font.PLAIN, 22);
        Font f3 = new Font("Tahoma", Font.BOLD, 22);

        //topPanel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Required Products",JLabel.CENTER);
        title.setPreferredSize(new Dimension(200,50));
        title.setOpaque(true);
        title.setFont(f2);
        title.setBackground(new Color(0xEF3169));
        title.setForeground(Color.white);
        topPanel.add(title,BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        //table making
        String[] columnNames = {"No.","Name","Category","Price"};
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

        String url = "jdbc:mysql://localhost:3306/31may";
        try (Connection con = DriverManager.getConnection(url, "root", "Hope$02")) {
            String sql = "select * from Stock";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                ResultSet rs = pst.executeQuery();

                int count = 1;
                while (rs.next())
                {
                    int Q = rs.getInt("Quantity");
                    if(Q==0){
                        String s1 = rs.getString("Name");
                        double d1 = rs.getDouble("Price");
                        String s2 = rs.getString("category");
                        tableModel.addRow(new Object[]{count++,s1,s2,String.valueOf(d1)});
                    }
                }
            }

        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        //center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane,BorderLayout.CENTER);

        //bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,20));
        JButton back = new JButton("Back");
        back.setFont(f2);
        back.setBackground(new Color(0xEF3169));
        back.setForeground(Color.white);
        Dimension size = new Dimension(200, 50);
        back.setPreferredSize(size);
        back.setMinimumSize(size);
        back.setMaximumSize(size);
        back.setSize(size);
        back.setVisible(true);
        back.setSize(200,50);
        back.addActionListener(
                a->{
                    new Home();
                }
        );
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bottomPanel.add(back,BorderLayout.CENTER);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        c.add(topPanel,BorderLayout.NORTH);
        c.add(centerPanel,BorderLayout.CENTER);
        c.add(bottomPanel,BorderLayout.SOUTH);

        setVisible(true);
        setSize(1200,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Stock1");
    }

    public static void main(String[] args) {
        new Requirement();
    }
}
