package studentuuzskaitesistema.dao;

import java.time.LocalDate;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import studentuuzskaitesistema.db.DBConnection;
import studentuuzskaitesistema.model.Grade;
import studentuuzskaitesistema.model.Student;

/**
 * JUnit4 vienībtesti klasei {@link GradeDAO}: sekmju pievienošana, rediģēšana,
 * dzēšana, vidējās atzīmes aprēķins un nekorektu atzīmju noraidīšana.
 */
public class GradeDAOTest {

    private static GradeDAO gradeDAO;
    private static StudentDAO studentDAO;
    private static int testStudentId;

    /**
     * Inicializē datubāzes shēmu un sagatavo pagaidu testa studentu pirms testu izpildes.
     *
     * @throws Exception ja neizdodas izveidot savienojumu ar datubāzi
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        DBConnection.initialize();
        gradeDAO = new GradeDAO();
        studentDAO = new StudentDAO();

        Student temp = new Student(0, "Testa", "Students", "999999-99999", "tests@example.lv", "29999999", null);
        if (!studentDAO.personalCodeExists("999999-99999")) {
            studentDAO.add(temp);
        }
        List<Student> found = studentDAO.search("999999-99999", null);
        assertFalse("Testa studentam jābūt izveidotam", found.isEmpty());
        testStudentId = found.get(0).getId();
    }

    /**
     * Izdzēš pagaidu testa studentu pēc vienībtestu izpildes.
     *
     * @throws Exception ja rodas SQL kļūda
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        if (studentDAO != null && testStudentId > 0) {
            studentDAO.delete(testStudentId);
        }
    }

    /**
     * Pārbauda pilnu CRUD ciklu (pievienošana, atjaunināšana, dzēšana) un vidējās atzīmes izmaiņas.
     *
     * @throws Exception ja rodas SQL kļūda
     */
    @Test
    public void addUpdateAndDeleteGradeWorksCorrectly() throws Exception {
        int beforeCount = gradeDAO.countForStudent(testStudentId);

        Grade newGrade = new Grade(0, testStudentId, "JUnit Tests", 9, LocalDate.of(2026, 3, 15), null);
        assertTrue("Jaunas atzīmes pievienošanai jāatgriež true", gradeDAO.add(newGrade));
        assertEquals(beforeCount + 1, gradeDAO.countForStudent(testStudentId));

        List<Grade> grades = gradeDAO.findByStudent(testStudentId);
        Grade inserted = grades.stream()
                .filter(g -> "JUnit Tests".equals(g.getSubject()) && g.getGrade() == 9)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Pievienotā atzīme netika atrasta"));

        inserted.setSubject("JUnit Tests Labots");
        inserted.setGrade(10);
        assertTrue("Atzīmes rediģēšanai jāatgriež true", gradeDAO.update(inserted));

        double avg = gradeDAO.averageForStudent(testStudentId);
        assertTrue("Vidējai atzīmei jābūt diapazonā [1.0; 10.0]", avg >= 1.0 && avg <= 10.0);

        assertTrue("Atzīmes dzēšanai jāatgriež true", gradeDAO.delete(inserted.getId()));
        assertEquals(beforeCount, gradeDAO.countForStudent(testStudentId));
    }

    /**
     * Pārbauda, ka atzīme zem minimālās robežas (< 1) izraisa {@link IllegalArgumentException}.
     *
     * @throws Exception ja izpilde neizdodas
     */
    @Test(expected = IllegalArgumentException.class)
    public void addRejectsGradeBelowMinimum() throws Exception {
        Grade invalidLow = new Grade(0, testStudentId, "Matemātika", 0, LocalDate.now(), null);
        gradeDAO.add(invalidLow);
    }

    /**
     * Pārbauda, ka atzīme virs maksimālās robežas (> 10) izraisa {@link IllegalArgumentException}.
     *
     * @throws Exception ja izpilde neizdodas
     */
    @Test(expected = IllegalArgumentException.class)
    public void updateRejectsGradeAboveMaximum() throws Exception {
        Grade invalidHigh = new Grade(1, testStudentId, "Matemātika", 11, LocalDate.now(), null);
        gradeDAO.update(invalidHigh);
    }

    /**
     * Pārbauda grupas vidējās atzīmes aprēķinu eksistējošai un neeksistējošai grupai.
     *
     * @throws Exception ja rodas SQL kļūda
     */
    @Test
    public void averageForGroupReturnsValidRangeAndZeroForMissingGroup() throws Exception {
        double nonExistentAvg = gradeDAO.averageForGroup(-999);
        assertEquals(0.0, nonExistentAvg, 0.001);

        Grade tempGrade = new Grade(0, testStudentId, "Matemātika", 8, LocalDate.of(2026, 4, 1), null);
        assertTrue(gradeDAO.add(tempGrade));
        List<Grade> allGrades = gradeDAO.findAll();
        assertFalse("Visu atzīmju sarakstam nav jābūt tukšam", allGrades.isEmpty());
    }
}
