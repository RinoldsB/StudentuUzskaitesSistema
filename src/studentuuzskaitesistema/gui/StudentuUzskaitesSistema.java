
package studentuuzskaitesistema.gui;

import studentuuzskaitesistema.db.DBConnection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class StudentuUzskaitesSistema {

    public static void main(String[] args) {
        try {
            DBConnection.initialize();
            System.out.println("Datu bāze gatava.");
        } catch (SQLException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                null,
                "Neizdevās inicializēt datubāzi: " + e.getMessage(),
                "Datubāzes kļūda",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
