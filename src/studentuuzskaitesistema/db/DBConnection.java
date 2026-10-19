
package studentuuzskaitesistema.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import studentuuzskaitesistema.util.PasswordUtil;

public class DBConnection {

    private static final String DB_URL =
            "jdbc:derby:StudentuUzskaitesDB;create=true";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() throws SQLException {
        try (Connection con = getConnection()) {
            createTableIfMissing(con, "USERS",
                "CREATE TABLE USERS ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1), "
                + "username VARCHAR(50) NOT NULL UNIQUE, "
                + "password_hash VARCHAR(64) NOT NULL, "
                + "full_name VARCHAR(100) NOT NULL, "
                + "role VARCHAR(20) NOT NULL, "
                + "CONSTRAINT PK_USERS PRIMARY KEY (id))");

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

    private static void insertInitialData(Connection con)
            throws SQLException {

        insertUserIfMissing(con, "admin", "admin123",
                "Sistēmas administrators", "ADMIN");

        insertUserIfMissing(con, "pasniedzejs", "teacher123",
                "Jānis Bērziņš", "TEACHER");

        insertGroupIfMissing(con, "PR-31", 1);
        insertGroupIfMissing(con, "PR-32", 2);
        insertGroupIfMissing(con, "PR-33", 3);

        insertStudentIfMissing(con, "Anna", "Ozola",
                "010101-12345", "anna@example.com",
                "20000001", "PR-31");

        insertStudentIfMissing(con, "Mārtiņš", "Bērziņš",
                "020202-12345", "martins@example.com",
                "20000002", "PR-32");

        insertStudentIfMissing(con, "Laura", "Kalniņa",
                "030303-12345", "laura@example.com",
                "20000003", "PR-33");
    }

    private static void insertUserIfMissing(
            Connection con, String username, String password,
            String fullName, String role) throws SQLException {

        try (PreparedStatement check = con.prepareStatement(
                "SELECT 1 FROM USERS WHERE username = ?")) {
            check.setString(1, username);

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
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

    private static void insertGroupIfMissing(
            Connection con, String name, int course)
            throws SQLException {

        try (PreparedStatement check = con.prepareStatement(
                "SELECT 1 FROM STUDENT_GROUPS WHERE name = ?")) {
            check.setString(1, name);

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        Integer teacherId = findUserId(con, "pasniedzejs");

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO STUDENT_GROUPS (name, course, teacher_id) "
                + "VALUES (?, ?, ?)")) {
            ps.setString(1, name);
            ps.setInt(2, course);

            if (teacherId == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, teacherId);
            }

            ps.executeUpdate();
        }
    }

    private static Integer findUserId(
            Connection con, String username) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM USERS WHERE username = ?")) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    private static void insertStudentIfMissing(
            Connection con, String firstName, String lastName,
            String personalCode, String email, String phone,
            String groupName) throws SQLException {

        try (PreparedStatement check = con.prepareStatement(
                "SELECT 1 FROM STUDENTS WHERE personal_code = ?")) {
            check.setString(1, personalCode);

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        Integer groupId = null;

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM STUDENT_GROUPS WHERE name = ?")) {
            ps.setString(1, groupName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    groupId = rs.getInt("id");
                }
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO STUDENTS "
                + "(first_name, last_name, personal_code, email, phone, group_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, personalCode);
            ps.setString(4, email);
            ps.setString(5, phone);

            if (groupId == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, groupId);
            }

            ps.executeUpdate();
        }
    }
}
