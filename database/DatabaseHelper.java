
package database;
import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:college_events.db";
    public static String currentUser = null;
    public static String userRole = null;

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY, " +
                        "password TEXT, " +
                        "role TEXT, " +
                        "club_name TEXT)");

                stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT, " +
                        "club_owner TEXT, " +
                        "venue TEXT, " +
                        "capacity INTEGER)");

                addColumnIfNotExists(conn, "events", "start_date", "DATE");
                addColumnIfNotExists(conn, "events", "end_date", "DATE");
                addColumnIfNotExists(conn, "events", "last_date", "DATE");
                addColumnIfNotExists(conn, "events", "registration_count", "INTEGER DEFAULT 0");

                stmt.execute("CREATE TABLE IF NOT EXISTS registrations (" +
                        "student_user TEXT, " +
                        "event_id INTEGER, " +
                        "UNIQUE(student_user, event_id))");

                stmt.execute("INSERT OR IGNORE INTO users VALUES ('admin', 'admin123', 'ADMIN', 'SYSTEM')");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addColumnIfNotExists(Connection conn, String table, String column, String type) {
        try (Statement stmt = conn.createStatement()) {

            // Check existing columns
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")");
            boolean exists = false;

            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(column)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
