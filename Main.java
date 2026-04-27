import database.DatabaseHelper;
import view.LoginFrame;

public class Main {
    public static void main(String[] args) {
        DatabaseHelper.initialize();

        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
