//package view;
//import database.DatabaseHelper;
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.sql.*;
//
//public class ClubDash extends JFrame {
//    private String clubName;
//    private DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Title", "Venue"}, 0);
//    private JTable table = new JTable(model);
//
//    public ClubDash(String clubName) {
//        this.clubName = clubName;
//        setTitle("Club Portal: " + clubName);
//        setSize(700, 500);
//
//        // Toolbar
//        JToolBar toolBar = new JToolBar();
//        JButton btnLogout = new JButton("Logout");
//        btnLogout.addActionListener(e -> { new LoginFrame().setVisible(true); this.dispose(); });
//        toolBar.add(btnLogout);
//
//        // Control Panel
//        JPanel controls = new JPanel();
//        JButton btnAdd = new JButton("Create Event");
//        JButton btnDel = new JButton("Delete Event");
//        controls.add(btnAdd); controls.add(btnDel);
//
//        btnAdd.addActionListener(e -> createEvent());
//        btnDel.addActionListener(e -> deleteEvent());
//
//        add(toolBar, BorderLayout.NORTH);
//        add(new JScrollPane(table), BorderLayout.CENTER);
//        add(controls, BorderLayout.SOUTH);
//
//        loadData();
//        setLocationRelativeTo(null);
//    }
//
//    private void loadData() {
//        model.setRowCount(0);
//        try (Connection c = DatabaseHelper.connect();
//             PreparedStatement ps = c.prepareStatement("SELECT id, title, venue FROM events WHERE club_owner=?")) {
//            ps.setString(1, clubName);
//            ResultSet rs = ps.executeQuery();
//            while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
//        } catch(SQLException e) { e.printStackTrace(); }
//    }
//
//    private void createEvent() {
//        String t = JOptionPane.showInputDialog("Event Title:");
//        String v = JOptionPane.showInputDialog("Venue:");
//        if(t != null && v != null) {
//            try (Connection c = DatabaseHelper.connect();
//                 PreparedStatement ps = c.prepareStatement("INSERT INTO events(title, club_owner, venue, capacity) VALUES(?,?,?,100)")) {
//                ps.setString(1, t); ps.setString(2, clubName); ps.setString(3, v);
//                ps.executeUpdate(); loadData();
//            } catch(SQLException e) { e.printStackTrace(); }
//        }
//    }
//
//    private void deleteEvent() {
//        int r = table.getSelectedRow();
//        if(r == -1) return;
//        int id = (int)model.getValueAt(r, 0);
//        try (Connection c = DatabaseHelper.connect();
//             PreparedStatement ps = c.prepareStatement("DELETE FROM events WHERE id=?")) {
//            ps.setInt(1, id); ps.executeUpdate(); loadData();
//        } catch(SQLException e) { e.printStackTrace(); }
//    }
//}
package view;
import database.DatabaseHelper;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ClubDash extends JFrame {
    private String clubName;

    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Title", "Venue", "Start Date", "End Date", "Last Date", "Registered", "Capacity"}, 0) {
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTable table = new JTable(model);

    public ClubDash(String clubName) {
        this.clubName = clubName;
        setTitle("Club Portal: " + clubName);
        setSize(900, 500);

        JToolBar toolBar = new JToolBar();
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        toolBar.add(btnLogout);

        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Create Event");
        JButton btnDel = new JButton("Delete Event");
        controls.add(btnAdd);
        controls.add(btnDel);

        btnAdd.addActionListener(e -> createEvent());
        btnDel.addActionListener(e -> deleteEvent());

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        loadData();
        setLocationRelativeTo(null);
    }

    private void loadData() {
        model.setRowCount(0);

        try (Connection c = DatabaseHelper.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, title, venue, start_date, end_date, last_date, registration_count, capacity FROM events WHERE club_owner=?")) {

            ps.setString(1, clubName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("venue"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDate("last_date"),
                        rs.getInt("registration_count"),
                        rs.getInt("capacity")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error!");
            e.printStackTrace();
        }
    }

    private Date parseDate(String input) throws Exception {
        input = input.trim();

        String[] formats = {
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "yyyy/MM/dd",
                "dd/MM/yyyy"
        };

        for (String f : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(f);
                sdf.setLenient(false);
                java.util.Date d = sdf.parse(input);
                return new Date(d.getTime());
            } catch (Exception ignored) {}
        }

        throw new Exception("Invalid date");
    }

    private void createEvent() {

        String t = JOptionPane.showInputDialog("Event Title:");
        if (t == null || t.trim().isEmpty()) return;

        String v = JOptionPane.showInputDialog("Venue:");
        if (v == null || v.trim().isEmpty()) return;

        String capStr = JOptionPane.showInputDialog("Maximum Capacity:");
        if (capStr == null || capStr.trim().isEmpty()) return;

        int capacity;
        try {
            capacity = Integer.parseInt(capStr.trim());
            if (capacity <= 0) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive number!");
            return;
        }

        String start = JOptionPane.showInputDialog("Start Date (YYYY-MM-DD):");
        String end = JOptionPane.showInputDialog("End Date (YYYY-MM-DD):");
        String last = JOptionPane.showInputDialog("Last Registration Date (YYYY-MM-DD):");

        if (start == null || end == null || last == null) return;

        Date startDate, endDate, lastDate;

        try {
            startDate = parseDate(start);
            endDate = parseDate(end);
            lastDate = parseDate(last);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date!\nUse formats:\nYYYY-MM-DD or DD-MM-YYYY");
            return;
        }

        if (endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "End date cannot be before start date!");
            return;
        }

        if (lastDate.after(startDate)) {
            JOptionPane.showMessageDialog(this, "Last registration must be before event start!");
            return;
        }

        try (Connection c = DatabaseHelper.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO events(title, club_owner, venue, capacity, start_date, end_date, last_date, registration_count) VALUES(?,?,?,?,?,?,?,0)")) {

            ps.setString(1, t.trim());
            ps.setString(2, clubName);
            ps.setString(3, v.trim());
            ps.setInt(4, capacity);
            ps.setDate(5, startDate);
            ps.setDate(6, endDate);
            ps.setDate(7, lastDate);

            ps.executeUpdate();
            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Insert Error!");
            e.printStackTrace();
        }
    }

    private void deleteEvent() {
        int r = table.getSelectedRow();

        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this event?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        int id = (int) model.getValueAt(r, 0);

        try (Connection c = DatabaseHelper.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM events WHERE id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Delete Error!");
            e.printStackTrace();
        }
    }
}
