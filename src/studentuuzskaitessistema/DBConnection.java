package studentuuzskaitessistema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Nodrošina savienojumu ar iegulto Apache Derby datu bāzi un
 * izveido tabulas, ja tās vēl neeksistē.
 */
public class DBConnection {

    private static final String URL = "jdbc:derby:StudentuDB;create=true";

    private DBConnection() {
    }

    /**
     * Izveido jaunu savienojumu ar datu bāzi.
     *
     * @return atvērts JDBC savienojums
     * @throws SQLException ja savienojumu neizdodas izveidot
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Izveido tabulas (ja to nav) un pievieno noklusēto administratoru.
     *
     * @throws SQLException ja notiek datu bāzes kļūda
     */
    public static void initialize() throws SQLException {
        try (Connection c = getConnection()) {
            if (!tableExists(c, "USERS")) {
                createTables(c);
                createDefaultAdmin(c);
            }
        }
    }

    private static boolean tableExists(Connection c, String table) throws SQLException {
        DatabaseMetaData meta = c.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, table, null)) {
            return rs.next();
        }
    }

    private static void createTables(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE USERS ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "password_hash VARCHAR(64) NOT NULL, "
                    + "full_name VARCHAR(100) NOT NULL, "
                    + "role VARCHAR(10) NOT NULL CHECK (role IN ('ADMIN','TEACHER')))");
            st.executeUpdate("CREATE TABLE STUDENT_GROUPS ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                    + "name VARCHAR(30) NOT NULL UNIQUE, "
                    + "course INT NOT NULL CHECK (course BETWEEN 1 AND 6), "
                    + "teacher_id INT REFERENCES USERS(id) ON DELETE SET NULL)");
            st.executeUpdate("CREATE TABLE STUDENTS ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                    + "first_name VARCHAR(50) NOT NULL, "
                    + "last_name VARCHAR(50) NOT NULL, "
                    + "personal_code VARCHAR(12) NOT NULL UNIQUE, "
                    + "email VARCHAR(100), phone VARCHAR(20), "
                    + "group_id INT REFERENCES STUDENT_GROUPS(id) ON DELETE SET NULL)");
            st.executeUpdate("CREATE TABLE GRADES ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                    + "student_id INT NOT NULL REFERENCES STUDENTS(id) ON DELETE CASCADE, "
                    + "subject VARCHAR(80) NOT NULL, "
                    + "grade INT NOT NULL CHECK (grade BETWEEN 1 AND 10), "
                    + "grade_date DATE NOT NULL, "
                    + "teacher_id INT REFERENCES USERS(id) ON DELETE SET NULL)");
        }
    }

    private static void createDefaultAdmin(Connection c) throws SQLException {
        String sql = "INSERT INTO USERS (username, password_hash, full_name, role) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, PasswordUtil.hash("admin123"));
            ps.setString(3, "Lietvedis");
            ps.setString(4, "ADMIN");
            ps.executeUpdate();
        }
    }
}
