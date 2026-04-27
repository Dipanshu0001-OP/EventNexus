package view;

import database.DatabaseHelper;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentDash extends JFrame {

    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Event Title", "Organizing Club", "Venue", "Seats Left"}, 0);

    private JTable table = new JTable(model);

    public StudentDash() {
        setTitle("Student Event Hub");
        setSize(950, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80));
        header.setPreferredSize(new Dimension(950, 70));

        JLabel welcome = new JLabel("  Welcome, " + DatabaseHelper.currentUser);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton btnOut = new JButton("Sign Out");
        btnOut.setBackground(new Color(231, 76, 60));
        btnOut.setForeground(Color.WHITE);
        btnOut.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        header.add(welcome, BorderLayout.WEST);
        header.add(btnOut, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search: "));
        searchBar.add(new JTextField(20));

        table.setRowHeight(25);

        JTabbedPane tabs = new JTabbedPane();

        JPanel discover = new JPanel(new BorderLayout());
        discover.add(searchBar, BorderLayout.NORTH);
        discover.add(new JScrollPane(table), BorderLayout.CENTER);

        tabs.addTab("Discover Events", discover);
        tabs.addTab("My Registered Events", new JPanel());

        JButton btnReg = new JButton("View Details & Register");
        btnReg.setBackground(new Color(41, 128, 185));
        btnReg.setForeground(Color.WHITE);
        btnReg.setPreferredSize(new Dimension(950, 50));
        btnReg.addActionListener(e -> register());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(btnReg, BorderLayout.SOUTH);

        loadEvents();
        setLocationRelativeTo(null);
    }

    private void loadEvents() {
        model.setRowCount(0);

        try (Connection c = DatabaseHelper.connect();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM events")) {

            while (rs.next()) {

                int capacity = rs.getInt("capacity");
                int registered = rs.getInt("registration_count");

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("club_owner"),
                        rs.getString("venue"),
                        (capacity - registered) // 🔥 seats left
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void register() {

        if (DatabaseHelper.currentUser.equals("Guest")) {
            JOptionPane.showMessageDialog(this, "Please Login to register for events.");
            return;
        }

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event first!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        try (Connection c = DatabaseHelper.connect()) {

            PreparedStatement check = c.prepareStatement(
                    "SELECT capacity, registration_count FROM events WHERE id=?");
            check.setInt(1, id);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                int capacity = rs.getInt("capacity");
                int registered = rs.getInt("registration_count");

                if (registered >= capacity) {
                    JOptionPane.showMessageDialog(this, "Event is FULL!");
                    return;
                }
            }

            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO registrations(student_user, event_id) VALUES(?,?)");

            ps.setString(1, DatabaseHelper.currentUser);
            ps.setInt(2, id);
            ps.executeUpdate();

            PreparedStatement update = c.prepareStatement(
                    "UPDATE events SET registration_count = registration_count + 1 WHERE id=?");

            update.setInt(1, id);
            update.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registered Successfully!");

            loadEvents();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Already Registered!");
        }
    }
}
