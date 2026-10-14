package studentuuzskaitessistema;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Datu piekļuve tabulai GRADES (sekmes un atskaites).
 */
public class GradeDAO {

    private static final String SELECT_BASE =
            "SELECT gr.id, gr.student_id, gr.subject, gr.grade, gr.grade_date, gr.teacher_id, "
            + "s.first_name, s.last_name "
            + "FROM GRADES gr JOIN STUDENTS s ON gr.student_id = s.id ";

    private Grade map(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setId(rs.getInt("id"));
        g.setStudentId(rs.getInt("student_id"));
        g.setSubject(rs.getString("subject"));
        g.setGrade(rs.getInt("grade"));
        g.setGradeDate(rs.getDate("grade_date").toLocalDate());
        int t = rs.getInt("teacher_id");
        g.setTeacherId(rs.wasNull() ? null : t);
        g.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        return g;
    }

    /**
     * Pievieno sekmi.
     *
     * @param g sekme (id netiek izmantots)
     * @return true, ja pievienota
     * @throws SQLException datu bāzes kļūda
     */
    public boolean add(Grade g) throws SQLException {
        if (!Validator.isValidGrade(g.getGrade())) {
            throw new IllegalArgumentException("Sekmei jābūt no 1 līdz 10");
        }
        String sql = "INSERT INTO GRADES (student_id, subject, grade, grade_date, teacher_id) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, g.getStudentId());
            ps.setString(2, g.getSubject());
            ps.setInt(3, g.getGrade());
            ps.setDate(4, Date.valueOf(g.getGradeDate()));
            if (g.getTeacherId() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, g.getTeacherId());
            }
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Atjaunina sekmi (priekšmetu, vērtējumu un datumu).
     *
     * @param g sekme ar aizpildītu id
     * @return true, ja atjaunināta
     * @throws SQLException datu bāzes kļūda
     */
    public boolean update(Grade g) throws SQLException {
        if (!Validator.isValidGrade(g.getGrade())) {
            throw new IllegalArgumentException("Sekmei jābūt no 1 līdz 10");
        }
        String sql = "UPDATE GRADES SET subject=?, grade=?, grade_date=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, g.getSubject());
            ps.setInt(2, g.getGrade());
            ps.setDate(3, Date.valueOf(g.getGradeDate()));
            ps.setInt(4, g.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Dzēš sekmi.
     *
     * @param id sekmes id
     * @return true, ja dzēsta
     * @throws SQLException datu bāzes kļūda
     */
    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM GRADES WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Atgriež viena studenta sekmes.
     *
     * @param studentId studenta id
     * @return sekmju saraksts, jaunākās pirmās
     * @throws SQLException datu bāzes kļūda
     */
    public List<Grade> findByStudent(int studentId) throws SQLException {
        return query(SELECT_BASE + "WHERE gr.student_id = ? ORDER BY gr.grade_date DESC", studentId);
    }

    /**
     * Atgriež visu grupas studentu sekmes (atskaitei).
     *
     * @param groupId grupas id
     * @return sekmju saraksts
     * @throws SQLException datu bāzes kļūda
     */
    public List<Grade> findByGroup(int groupId) throws SQLException {
        return query(SELECT_BASE + "WHERE s.group_id = ? ORDER BY s.last_name, s.first_name, gr.subject", groupId);
    }

    private List<Grade> query(String sql, int param) throws SQLException {
        List<Grade> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Aprēķina studenta vidējo vērtējumu.
     *
     * @param studentId studenta id
     * @return vidējais vērtējums vai 0, ja sekmju nav
     * @throws SQLException datu bāzes kļūda
     */
    public double averageForStudent(int studentId) throws SQLException {
        String sql = "SELECT AVG(CAST(grade AS DOUBLE)) FROM GRADES WHERE student_id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }
}
