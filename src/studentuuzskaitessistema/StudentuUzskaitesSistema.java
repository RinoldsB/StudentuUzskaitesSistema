/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package studentuuzskaitessistema;
import java.sql.SQLException;

/**
 *
 * @author Rinolds.Baikovs
 */
public class StudentuUzskaitesSistema {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            DBConnection.initialize();
            System.out.println("Datu bāze gatava.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}