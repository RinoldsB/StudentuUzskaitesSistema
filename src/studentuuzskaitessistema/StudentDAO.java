package studentuuzskaitessistema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Datu piekļuve tabulai STUDENTS (CRUD, meklēšana, filtrēšana).
 */
public class StudentDAO {

    private static final String SELECT_BASE =
            "SELECT s.id, s.first_name, s.last_name, s.personal_code, s.email, s.phone, "
            + "s.group_id, g.name AS group_name "
            + "FROM STUDENTS s LEFT JOIN STUDENT_GROUPS g ON s.group_id = g.id ";

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setPersonalCode(rs.getString("personal_code"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        int g = rs.getInt("group_id");
        s.setGroupId(rs.wasNull() ? null : g);
        s.setGroupName(rs.getString("group_name"));
        return s;
    }

    private void fill(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getFirstName());
        ps.setString(2, s.getLastName());
        ps.setString(3, s.getPersonalCode());
        ps.setString(4, s.getEmail());
        ps.setString(5, s.getPhone());
        if (s.getGroupId() == null) {
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(6, s.getGroupId());
        }
    }

    /**
     * Pievieno jaunu studentu.
     *
     * @param s students (id netiek izmantots)
     * @return true, ja pievienots
     * @throws SQLException datu bāzes kļūda (piem., dublēts personas kods)
     */
    public boolean add(Student s) throws SQLException {
        String sql = "INSERT INTO STUDENTS (first_name, last_name, personal_code, email, phone, group_id) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, s);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Atjaunina studenta datus.
     *
     * @param s students ar aizpildītu id
     * @return true, ja atjaunināts
     * @throws SQLException datu bāzes kļūda
     */
    public boolean update(Student s) throws SQLException {
        String sql = "UPDATE STUDENTS SET first_name=?, last_name=?, personal_code=?, email=?, phone=?, "
                + "group_id=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, s);
            ps.setInt(7, s.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Dzēš studentu (kopā ar viņa sekmēm).
     *
     * @param id studenta id
     * @return true, ja dzēsts
     * @throws SQLException datu bāzes kļūda
     */
    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM STUDENTS WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Atgriež visus studentus.
     *
     * @return studentu saraksts, sakārtots pēc uzvārda
     * @throws SQLException datu bāzes kļūda
     */
    public List<Student> findAll() throws SQLException {
        return search(null, null);
    }

    /**
     * Atrod studentu pēc id.
     *
     * @param id studenta id
     * @return students vai null
     * @throws SQLException datu bāzes kļūda
     */
    public Student findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE s.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Meklē studentus pēc vārda/uzvārda fragmenta un/vai grupas.
     *
     * @param text meklējamais teksts vārdā vai uzvārdā (null vai tukšs = bez filtra)
     * @param groupId grupas id (null = visas grupas)
     * @return atrasto studentu saraksts
     * @throws SQLException datu bāzes kļūda
     */
    public List<Student> search(String text, Integer groupId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        boolean hasText = text != null && !text.trim().isEmpty();
        if (hasText) {
            sql.append("AND (LOWER(s.first_name) LIKE ? OR LOWER(s.last_name) LIKE ?) ");
        }
        if (groupId != null) {
            sql.append("AND s.group_id = ? ");
        }
        sql.append("ORDER BY s.last_name, s.first_name");
        List<Student> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (hasText) {
                String like = "%" + text.trim().toLowerCase() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (groupId != null) {
                ps.setInt(i, groupId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Pārbauda, vai personas kods jau ir reģistrēts.
     *
     * @param personalCode personas kods
     * @return true, ja eksistē
     * @throws SQLException datu bāzes kļūda
     */
    public boolean personalCodeExists(String personalCode) throws SQLException {
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT 1 FROM STUDENTS WHERE personal_code = ?")) {
            ps.setString(1, personalCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
