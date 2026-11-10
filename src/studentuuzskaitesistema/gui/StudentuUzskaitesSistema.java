package studentuuzskaitesistema.gui;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import studentuuzskaitesistema.db.DBConnection;

/**
 * Lietotnes «Studentu uzskaites sistēma» galvenā palaišanas klase.
 */
public class StudentuUzskaitesSistema {

    /**
     * Inicializē datu bāzi, iestata grafiskās saskarnes izskatu un atver pieslēgšanās logu.
     *
     * @param args komandrindas argumenti (netiek izmantoti)
     */
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Ja Nimbus nav pieejams, izmanto noklusējuma izskatu
        }

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
