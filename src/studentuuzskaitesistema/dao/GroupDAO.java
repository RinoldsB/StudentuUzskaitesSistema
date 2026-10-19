package studentuuzskaitesistema.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import studentuuzskaitesistema.db.DBConnection;
import studentuuzskaitesistema.model.StudentGroup;

/**
 * Datu piekļuve tabulai STUDENT_GROUPS.
 */
public class GroupDAO {

    private static final String SELECT_BASE =
            "SELECT g.id, g.name, g.course, g.teacher_id, u.full_name AS teacher_name "
            + "FROM STUDENT_GROUPS g LEFT JOIN USERS u ON g.teacher_id = u.id ";

    private StudentGroup map(ResultSet rs) throws SQLException {
        StudentGroup g = new StudentGroup();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setCourse(rs.getInt("course"));
        int t = rs.getInt("teacher_id");
        g.setTeacherId(rs.wasNull() ? null : t);
        g.setTeacherName(rs.getString("teacher_name"));
        return g;
    }

    private void setTeacher(PreparedStatement ps, int index, Integer teacherId) throws SQLException {
        if (teacherId == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, teacherId);
        }
    }

    /**
     * Pievieno jaunu grupu.
     *
     * @param g grupa (id netiek izmantots)
     * @return true, ja pievienota
     * @throws SQLException datu bāzes kļūda (piem., dublēts nosaukums)
     */
    public boolean add(StudentGroup g) throws SQLException {
        String sql = "INSERT INTO STUDENT_GROUPS (name, course, teacher_id) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setInt(2, g.getCourse());
            setTeacher(ps, 3, g.getTeacherId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Atjaunina grupas datus.
     *
     * @param g grupa ar aizpildītu id
     * @return true, ja atjaunināta
     * @throws SQLException datu bāzes kļūda
     */
    public boolean update(StudentGroup g) throws SQLException {
        String sql = "UPDATE STUDENT_GROUPS SET name = ?, course = ?, teacher_id = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setInt(2, g.getCourse());
            setTeacher(ps, 3, g.getTeacherId());
            ps.setInt(4, g.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Dzēš grupu (studentiem group_id kļūst NULL).
     *
     * @param id grupas id
     * @return true, ja dzēsta
     * @throws SQLException datu bāzes kļūda
     */
    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM STUDENT_GROUPS WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Atgriež visas grupas.
     *
     * @return grupu saraksts, sakārtots pēc nosaukuma
     * @throws SQLException datu bāzes kļūda
     */
    public List<StudentGroup> findAll() throws SQLException {
        List<StudentGroup> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_BASE + "ORDER BY g.name");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Atgriež grupas, kas piešķirtas konkrētam pasniedzējam.
     *
     * @param teacherId pasniedzēja id
     * @return grupu saraksts
     * @throws SQLException datu bāzes kļūda
     */
    public List<StudentGroup> findByTeacher(int teacherId) throws SQLException {
        List<StudentGroup> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE g.teacher_id = ? ORDER BY g.name")) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }
}
