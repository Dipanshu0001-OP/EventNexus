package view;

import database.DatabaseHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDash extends JFrame {
    private DefaultTableModel eventModel, clubModel, studentModel;
    private JTable eventTable, clubTable, studentTable;

    public AdminDash() {
        setTitle("System Admin Control");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Global Event Control", createEventPanel());
        tabs.addTab("Club Management", createClubPanel());

        tabs.addTab("Student Directory", createStudentPanel());

        add(tabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private JPanel createEventPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        eventModel = new DefaultTableModel(new String[]{"ID", "Title", "Owner"}, 0);
        eventTable = new JTable(eventModel);

        JButton btnRemove = new JButton("Remove Event (Global)");
        btnRemove.addActionListener(e -> deleteEvent());

        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        JPanel btnWrapper = new JPanel();
        btnWrapper.add(btnRemove);
        panel.add(btnWrapper, BorderLayout.SOUTH);

        loadEvents();
        return panel;
    }

    private JPanel createClubPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        clubModel = new DefaultTableModel(new String[]{"Club Name", "Username"}, 0);
        clubTable = new JTable(clubModel);

        JButton btnAdd = new JButton("Add New Club");
        JButton btnDel = new JButton("Delete Club");

        btnAdd.addActionListener(e -> addNewClub());
        btnDel.addActionListener(e -> deleteClub());

        panel.add(new JScrollPane(clubTable), BorderLayout.CENTER);
        JPanel btnWrapper = new JPanel();
        btnWrapper.add(btnAdd); btnWrapper.add(btnDel);
        panel.add(btnWrapper, BorderLayout.SOUTH);

        loadClubs();
        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        studentModel = new DefaultTableModel(new String[]{"Student Username"}, 0);
        studentTable = new JTable(studentModel);

        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        loadStudents();
        return panel;
    }

    // --- Database logic for loading,deletion...... ---

    private void loadEvents() {
        eventModel.setRowCount(0);
        try (Connection c = DatabaseHelper.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, title, club_owner FROM events")) {
            while(rs.next()) eventModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void deleteEvent() {
        int r = eventTable.getSelectedRow();
        if(r == -1) return;
        int id = (int)eventModel.getValueAt(r, 0);
        try (Connection c = DatabaseHelper.connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM events WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate(); loadEvents();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void loadClubs() {
        clubModel.setRowCount(0);
        try (Connection c = DatabaseHelper.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT club_name, username FROM users WHERE role='CLUB'")) {
            while(rs.next()) clubModel.addRow(new Object[]{rs.getString(1), rs.getString(2)});
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void addNewClub() {
        String name = JOptionPane.showInputDialog("Enter Club Name:");
        String user = JOptionPane.showInputDialog("Enter Club Username:");
        String pass = JOptionPane.showInputDialog("Enter Club Password:");

        if(name != null && user != null && pass != null) {
            try (Connection c = DatabaseHelper.connect(); PreparedStatement ps = c.prepareStatement("INSERT INTO users VALUES(?,?,?,?)")) {
                ps.setString(1, user); ps.setString(2, pass); ps.setString(3, "CLUB"); ps.setString(4, name);
                ps.executeUpdate(); loadClubs();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Username exists!"); }
        }
    }

    private void deleteClub() {
        int r = clubTable.getSelectedRow();
        if(r == -1) return;
        String user = clubModel.getValueAt(r, 1).toString();
        try (Connection c = DatabaseHelper.connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE username=?")) {
            ps.setString(1, user); ps.executeUpdate(); loadClubs();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void loadStudents() {
        studentModel.setRowCount(0);
        try (Connection c = DatabaseHelper.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT username FROM users WHERE role='STUDENT'")) {
            while(rs.next()) studentModel.addRow(new Object[]{rs.getString(1)});
        } catch(Exception e) { e.printStackTrace(); }
    }
}
