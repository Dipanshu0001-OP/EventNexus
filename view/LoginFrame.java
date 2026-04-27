package view;

import database.DatabaseHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField userField = new JTextField();
    private JPasswordField passField = new JPasswordField();

    public LoginFrame() {
        setTitle("College Event Portal");
        setSize(850, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(new Color(44, 62, 80));
        left.setPreferredSize(new Dimension(320, 520));
        JLabel brand = new JLabel("<html><center>CULTURAL<br>EVENTS HUB</center></html>");
        brand.setFont(new Font("SansSerif", Font.BOLD, 28));
        brand.setForeground(Color.WHITE);
        left.add(brand);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(0, 50, 0, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0); gbc.gridx = 0;

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 26));
        gbc.gridy = 0; right.add(welcome, gbc);

        gbc.gridy = 1; right.add(new JLabel("Username"), gbc);
        userField.setPreferredSize(new Dimension(280, 35));
        gbc.gridy = 2; right.add(userField, gbc);

        gbc.gridy = 3; right.add(new JLabel("Password"), gbc);
        passField.setPreferredSize(new Dimension(280, 35));
        gbc.gridy = 4; right.add(passField, gbc);

        JButton btnLogin = createStyledButton("LOGIN", new Color(41, 128, 185), Color.WHITE);
        gbc.gridy = 5; right.add(btnLogin, gbc);

        JButton btnReg = new JButton("Create Student Account");
        btnReg.setForeground(new Color(41, 128, 185));
        btnReg.setContentAreaFilled(false);
        gbc.gridy = 6; right.add(btnReg, gbc);

        JButton btnGuest = createStyledButton("Continue as Guest", new Color(189, 195, 199), Color.BLACK);
        gbc.gridy = 7; right.add(btnGuest, gbc);

        btnLogin.addActionListener(e -> handleLogin());
        btnGuest.addActionListener(e -> { DatabaseHelper.currentUser = "Guest"; new StudentDash().setVisible(true); this.dispose(); });
        btnReg.addActionListener(e -> handleRegistration());

        add(left, BorderLayout.WEST); add(right, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    private JButton createStyledButton(String t, Color bg, Color fg) {
        JButton b = new JButton(t); b.setPreferredSize(new Dimension(280, 40));
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false); b.setBorderPainted(false);
        return b;
    }

    private void handleLogin() {
        try (Connection c = DatabaseHelper.connect(); PreparedStatement ps = c.prepareStatement("SELECT role, club_name FROM users WHERE username=? AND password=?")) {
            ps.setString(1, userField.getText());
            ps.setString(2, new String(passField.getPassword()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DatabaseHelper.currentUser = userField.getText();
                String role = rs.getString("role");
                if (role.equals("ADMIN")) new AdminDash().setVisible(true);
                else if (role.equals("CLUB")) new ClubDash(rs.getString("club_name")).setVisible(true);
                else new StudentDash().setVisible(true);
                this.dispose();
            } else { JOptionPane.showMessageDialog(this, "Invalid Login"); }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void handleRegistration() {
        String u = JOptionPane.showInputDialog("New Username:");
        String p = JOptionPane.showInputDialog("New Password:");
        if (u != null && !u.isEmpty()) {
            try (Connection c = DatabaseHelper.connect(); PreparedStatement ps = c.prepareStatement("INSERT INTO users VALUES(?,?,?,?)")) {
                ps.setString(1, u); ps.setString(2, p); ps.setString(3, "STUDENT"); ps.setString(4, "NONE");
                ps.executeUpdate(); JOptionPane.showMessageDialog(this, "Account Created!");
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: User exists."); }
        }
    }
    //Hello My Na

}
