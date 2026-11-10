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

/**
 * Datu piekļuve tabulai USERS (lietotāju autentifikācija, reģistrācija un atlase pēc lomas).
 */
public class UserDAO {

    /**
     * Pārbauda lietotāja pieslēgšanās datus (lietotājvārdu un paroli).
     *
     * @param username lietotājvārds
     * @param password nešifrēta parole
     * @return {@link User} objekts, ja dati ir pareizi, vai {@code null}, ja nepareizi
     * @throws SQLException datu bāzes kļūda
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
     * Pārbauda, vai lietotājvārds jau eksistē datu bāzē.
     *
     * @param username pārbaudāmais lietotājvārds
     * @return {@code true}, ja lietotājvārds jau ir aizņemts
     * @throws SQLException datu bāzes kļūda
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
     * Reģistrē jaunu lietotāju datu bāzē ar paroles jaucējvērtību.
     *
     * @param username lietotājvārds
     * @param password parole
     * @param fullName vārds un uzvārds
     * @param role lietotāja loma (ADMIN vai TEACHER)
     * @return {@code true}, ja reģistrācija ir veiksmīga
     * @throws SQLException datu bāzes kļūda
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
     * Reģistrē lietotāju, izmantojot {@link User} objektu.
     *
     * @param user lietotāja objekts
     * @return {@code true}, ja reģistrācija ir veiksmīga
     * @throws SQLException datu bāzes kļūda
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
     *
     * @param role lietotāja loma (ADMIN vai TEACHER)
     * @return lietotāju saraksts
     * @throws SQLException datu bāzes kļūda
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
     * Atrod pasniedzēja ID pēc vārda un uzvārda vai izveido jaunu pasniedzēja ierakstu datu bāzē.
     *
     * @param displayOrFullName pasniedzēja vārds un uzvārds
     * @return pasniedzēja ID datu bāzē vai {@code null}, ja vārds ir tukšs
     * @throws SQLException datu bāzes kļūda
     */
    public Integer findOrCreateTeacherByFullName(String displayOrFullName) throws SQLException {
        if (displayOrFullName == null || displayOrFullName.trim().isEmpty() || "-".equals(displayOrFullName.trim())) {
            return null;
        }
        String cleanName = displayOrFullName.trim().replaceAll("\\s*\\([^)]*\\)$", "").trim();
        if (cleanName.isEmpty()) {
            return null;
        }
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM USERS WHERE UPPER(full_name) = UPPER(?)")) {
                ps.setString(1, cleanName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }
            String baseUser = cleanName.toLowerCase()
                    .replace('ā', 'a').replace('č', 'c').replace('ē', 'e')
                    .replace('ģ', 'g').replace('ī', 'i').replace('ķ', 'k')
                    .replace('ļ', 'l').replace('ņ', 'n').replace('š', 's')
                    .replace('ū', 'u').replace('ž', 'z')
                    .replaceAll("[^a-z0-9]", "_");
            if (baseUser.length() < 3) {
                baseUser = "teacher_" + baseUser;
            }
            if (baseUser.length() > 40) {
                baseUser = baseUser.substring(0, 40);
            }
            String candidate = baseUser;
            int suffix = 1;
            while (usernameExists(candidate)) {
                candidate = baseUser + "_" + (suffix++);
            }
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO USERS (username, password_hash, full_name, role) VALUES (?, ?, ?, 'TEACHER')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ins.setString(1, candidate);
                ins.setString(2, PasswordUtil.hash("teacher123"));
                ins.setString(3, cleanName);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM USERS WHERE UPPER(full_name) = UPPER(?)")) {
                ps.setString(1, cleanName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }
        }
        return null;
    }

    /**
     * Dzēš lietotāju pēc ID.
     *
     * @param id lietotāja identifikators
     * @return {@code true}, ja lietotājs tika dzēsts
     * @throws SQLException datu bāzes kļūda
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
