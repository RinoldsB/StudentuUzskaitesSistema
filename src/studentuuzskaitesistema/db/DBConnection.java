package studentuuzskaitesistema.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import studentuuzskaitesistema.util.PasswordUtil;

/**
 * Nodrošina savienojumu ar Apache Derby (Embedded) datu bāzi,
 * izveido tabulas un aizpilda sākuma datus pirmajā palaišanas reizē.
 */
public class DBConnection {

    private static final String DB_URL =
            "jdbc:derby:StudentuUzskaitesDB;create=true";

    private DBConnection() {
    }

    /**
     * Atver un atgriež jaunu JDBC savienojumu ar Apache Derby datu bāzi.
     *
     * @return aktīvs {@link Connection} objekts
     * @throws SQLException ja savienojumu neizdodas izveidot
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Izveido datu bāzes tabulas (USERS, STUDENT_GROUPS, STUDENTS, GRADES),
     * ja tās vēl neeksistē, un ievieto sākuma datus.
     *
     * @throws SQLException ja tabulu vai sākuma datu izveide neizdodas
     */
    public static void initialize() throws SQLException {
        try (Connection con = getConnection()) {
            createTableIfMissing(con, "USERS",
                "CREATE TABLE USERS ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1), "
                + "username VARCHAR(50) NOT NULL UNIQUE, "
                + "password_hash VARCHAR(255) NOT NULL, "
                + "full_name VARCHAR(100) NOT NULL, "
                + "role VARCHAR(20) NOT NULL, "
                + "CONSTRAINT PK_USERS PRIMARY KEY (id))");
            ensurePasswordHashColumnLength(con);

            createTableIfMissing(con, "STUDENT_GROUPS",
                "CREATE TABLE STUDENT_GROUPS ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1), "
                + "name VARCHAR(30) NOT NULL UNIQUE, "
                + "course INT NOT NULL, "
                + "teacher_id INT, "
                + "CONSTRAINT PK_STUDENT_GROUPS PRIMARY KEY (id), "
                + "CONSTRAINT FK_GROUP_TEACHER FOREIGN KEY (teacher_id) "
                + "REFERENCES USERS(id))");

            createTableIfMissing(con, "STUDENTS",
                "CREATE TABLE STUDENTS ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1), "
                + "first_name VARCHAR(50) NOT NULL, "
                + "last_name VARCHAR(50) NOT NULL, "
                + "personal_code VARCHAR(20) NOT NULL UNIQUE, "
                + "email VARCHAR(100), "
                + "phone VARCHAR(30), "
                + "group_id INT, "
                + "CONSTRAINT PK_STUDENTS PRIMARY KEY (id), "
                + "CONSTRAINT FK_STUDENT_GROUP FOREIGN KEY (group_id) "
                + "REFERENCES STUDENT_GROUPS(id) ON DELETE SET NULL)");

            createTableIfMissing(con, "GRADES",
                "CREATE TABLE GRADES ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1), "
                + "student_id INT NOT NULL, "
                + "subject VARCHAR(100) NOT NULL, "
                + "grade INT NOT NULL CHECK (grade BETWEEN 1 AND 10), "
                + "grade_date DATE NOT NULL, "
                + "teacher_id INT, "
                + "CONSTRAINT PK_GRADES PRIMARY KEY (id), "
                + "CONSTRAINT FK_GRADE_STUDENT FOREIGN KEY (student_id) "
                + "REFERENCES STUDENTS(id) ON DELETE CASCADE, "
                + "CONSTRAINT FK_GRADE_TEACHER FOREIGN KEY (teacher_id) "
                + "REFERENCES USERS(id) ON DELETE SET NULL)");

            insertInitialData(con);

            System.out.println("Datubāze un tabulas ir gatavas.");
        }
    }

    private static void createTableIfMissing(
            Connection con, String tableName, String sql)
            throws SQLException {

        try (ResultSet rs = con.getMetaData().getTables(
                null, "APP", tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return;
            }
        }

        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static void ensurePasswordHashColumnLength(Connection con) throws SQLException {
        try (ResultSet rs = con.getMetaData().getColumns(null, "APP", "USERS", "PASSWORD_HASH")) {
            if (rs.next() && rs.getInt("COLUMN_SIZE") < 255) {
                try (Statement st = con.createStatement()) {
                    st.executeUpdate("ALTER TABLE USERS ALTER COLUMN password_hash SET DATA TYPE VARCHAR(255)");
                }
            }
        }
    }

    private static void insertInitialData(Connection con)
            throws SQLException {

        insertUserIfMissing(con, "admin", "admin123",
                "Sistēmas administrators", "ADMIN");

        insertUserIfMissing(con, "pasniedzejs", "teacher123",
                "Jānis Bērziņš", "TEACHER");

        removeLegacyExamples(con);
    }

    /**
     * Izdzēš iepriekšējos piemēra studentus (Anna Ozola, Mārtiņš Bērziņš, Laura Kalniņa)
     * un viņu piemēra atzīmes, lai datu bāzē glabātos tikai lietotāja pievienotie ieraksti.
     */
    private static void removeLegacyExamples(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM STUDENTS WHERE personal_code IN ('010101-12345', '020202-12345', '030303-12345')");
        }
    }

    private static void insertUserIfMissing(
            Connection con, String username, String password,
            String fullName, String role) throws SQLException {

        try (PreparedStatement check = con.prepareStatement(
                "SELECT id, password_hash FROM USERS WHERE username = ?")) {
            check.setString(1, username);

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    String existingHash = rs.getString("password_hash");
                    if (!PasswordUtil.verify(password, existingHash)) {
                        try (PreparedStatement upd = con.prepareStatement(
                                "UPDATE USERS SET password_hash = ? WHERE id = ?")) {
                            upd.setString(1, PasswordUtil.hash(password));
                            upd.setInt(2, rs.getInt("id"));
                            upd.executeUpdate();
                        }
                    }
                    return;
                }
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO USERS "
                + "(username, password_hash, full_name, role) "
                + "VALUES (?, ?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.executeUpdate();
        }
    }
}
