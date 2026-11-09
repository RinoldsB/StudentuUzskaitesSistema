package studentuuzskaitesistema.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import studentuuzskaitesistema.db.DBConnection;
import studentuuzskaitesistema.model.User;
import studentuuzskaitesistema.util.PasswordUtil;
import studentuuzskaitesistema.util.Validator;

public class UserDAO {

    /**
     * Pārbauda lietotāja pieslēgšanās datus.
     */
    public User authenticate(String username, String password)
            throws SQLException {

        if (!Validator.isNotEmpty(username)
                || !Validator.isNotEmpty(password)) {
            return null;
        }

        String sql =
                "SELECT id, username, password_hash, full_name, role "
                + "FROM USERS WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String storedHash = rs.getString("password_hash");

                    if (PasswordUtil.verify(password, storedHash)) {

                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                storedHash,
                                rs.getString("full_name"),
                                rs.getString("role")
                        );
                    }
                }
            }
        }

        return null;
    }

    /**
     * Pārbauda, vai lietotājvārds jau eksistē.
     */
    public boolean usernameExists(String username)
            throws SQLException {

        if (!Validator.isNotEmpty(username)) {
            return false;
        }

        String sql =
                "SELECT id FROM USERS WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Reģistrē jaunu lietotāju.
     */
    public boolean register(
            String username,
            String password,
            String fullName,
            String role) throws SQLException {

        if (!Validator.isValidUsername(username)
                || !Validator.isValidPassword(password)
                || !Validator.isValidName(fullName)
                || !Validator.isValidRole(role)) {
            return false;
        }

        if (usernameExists(username)) {
            return false;
        }

        String sql =
                "INSERT INTO USERS "
                + "(username, password_hash, full_name, role) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName.trim());
            ps.setString(4, role.toUpperCase());

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Reģistrē lietotāju, izmantojot User objektu.
     */
    public boolean register(User user) throws SQLException {

        if (user == null) {
            return false;
        }

        return register(
                user.getUsername(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole()
        );
    }

    /**
     * Atgriež visus lietotājus ar norādīto lomu.
     */
    public List<User> findByRole(String role)
            throws SQLException {

        List<User> users = new ArrayList<>();

        if (!Validator.isValidRole(role)) {
            return users;
        }

        String sql =
                "SELECT id, username, password_hash, full_name, role "
                + "FROM USERS WHERE role = ? ORDER BY full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    users.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    ));
                }
            }
        }

        return users;
    }

    /**
     * Dzēš lietotāju pēc ID.
     */
    public boolean delete(int id) throws SQLException {

        if (id <= 0) {
            return false;
        }

        String sql = "DELETE FROM USERS WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() == 1;
        }
    }
}