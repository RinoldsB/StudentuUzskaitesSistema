package studentuuzskaitessistema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Datu piekļuve tabulai USERS (autentifikācija un lietotāju pārvaldība).
 */
public class UserDAO {

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"),
                rs.getString("password_hash"), rs.getString("full_name"), rs.getString("role"));
    }

    /**
     * Pārbauda lietotājvārdu un paroli.
     *
     * @param username lietotājvārds
     * @param password parole atklātā tekstā
     * @return lietotājs, ja dati pareizi, citādi null
     * @throws SQLException datu bāzes kļūda
     */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE username = ? AND password_hash = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Pārbauda, vai lietotājvārds jau ir aizņemts.
     *
     * @param username lietotājvārds
     * @return true, ja eksistē
     * @throws SQLException datu bāzes kļūda
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM USERS WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Reģistrē jaunu lietotāju.
     *
     * @param username lietotājvārds
     * @param password parole atklātā tekstā
     * @param fullName vārds un uzvārds
     * @param role loma (ADMIN vai TEACHER)
     * @return true, ja reģistrēts; false, ja lietotājvārds jau aizņemts
     * @throws SQLException datu bāzes kļūda
     */
    public boolean register(String username, String password, String fullName, String role) throws SQLException {
        if (usernameExists(username)) {
            return false;
        }
        String sql = "INSERT INTO USERS (username, password_hash, full_name, role) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName);
            ps.setString(4, role);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Atgriež visus lietotājus ar doto lomu.
     *
     * @param role loma (ADMIN vai TEACHER)
     * @return lietotāju saraksts
     * @throws SQLException datu bāzes kļūda
     */
    public List<User> findByRole(String role) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM USERS WHERE role = ? ORDER BY full_name";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Dzēš lietotāju.
     *
     * @param id lietotāja id
     * @return true, ja dzēsts
     * @throws SQLException datu bāzes kļūda
     */
    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM USERS WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
