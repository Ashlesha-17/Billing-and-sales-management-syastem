import javax.swing.*;
import java.awt.*;

class Home extends JFrame{
    Home()
    {
        Font f1 = new Font("Arial", Font.BOLD, 40);
        Font f2 = new Font("Tahoma", Font.PLAIN, 22);
        Font f3 = new Font("Verdana", Font.PLAIN, 22);

        JLabel name = new JLabel("Super Market");
        JButton b1 = new JButton("NEW");
        JButton b2 = new JButton("Today's Sales");
        JButton b3 = new JButton("Month's sales");
        JButton b4 = new JButton("Stock");
        JButton b5 = new JButton("Required products");
        JButton b6 = new JButton("Analysis");

        name.setFont(f1);
        b1.setFont(f2);
        b2.setFont(f2);
        b3.setFont(f2);
        b4.setFont(f2);
        b5.setFont(f2);
        b6.setFont(f2);

        b1.setBackground(new Color(176, 224, 230));
        b2.setBackground(new Color(176, 243, 217));
        b3.setBackground(new Color(176, 243, 217));
        b4.setBackground(new Color(176, 243, 217));
        b5.setBackground(new Color(176, 243, 217));
        b6.setBackground(new Color(9, 206, 232));

        Container c = getContentPane();
        c.setLayout(null);

        name.setBounds(490,30,400,70);
        b1.setBounds(450,150,300,60);
        b2.setBounds(280,300,300,60);
        b3.setBounds(620,300,300,60);
        b4.setBounds(280,400,300,60);
        b5.setBounds(620,400,300,60);
        b6.setBounds(450,520,300,60);



        c.add(name);
        c.add(b1);
        c.add(b2);
        c.add(b3);
        c.add(b4);
        c.add(b5);
        c.add(b6);

        b1.addActionListener(
                a->{
                    new New();
                }
        );

        b2.addActionListener(
                a->{
                    new Today();
                }
        );

        b3.addActionListener(
                a->{
                    new Month();
                }
        );

        b4.addActionListener(
                a->{
                    new Stock();
                }
        );

        b5.addActionListener(
                a->{
                    new Requirement();
                }
        );

        b6.addActionListener(
                a->{
                    new Analysis();
                }
        );

        setVisible(true);
        setSize(1200,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Home");
    }
    public static void main(String[] args) {
    new Home();
    }
}