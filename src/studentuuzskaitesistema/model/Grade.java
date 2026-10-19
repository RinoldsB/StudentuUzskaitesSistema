package studentuuzskaitesistema.model;

import java.time.LocalDate;

/**
 * Studenta sekme (vērtējums) kādā priekšmetā.
 */
public class Grade {

    private int id;
    private int studentId;
    private String subject;
    private int grade;
    private LocalDate gradeDate;
    private Integer teacherId;
    private String studentName;

    /** Izveido tukšu sekmi. */
    public Grade() {
    }

    /**
     * Izveido sekmi ar pamata laukiem.
     *
     * @param id sekmes identifikators
     * @param studentId studenta id
     * @param subject priekšmets
     * @param grade vērtējums (1-10)
     * @param gradeDate vērtējuma datums
     * @param teacherId pasniedzēja id vai null
     */
    public Grade(int id, int studentId, String subject, int grade, LocalDate gradeDate, Integer teacherId) {
        this.id = id;
        this.studentId = studentId;
        this.subject = subject;
        this.grade = grade;
        this.gradeDate = gradeDate;
        this.teacherId = teacherId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }
    public LocalDate getGradeDate() { return gradeDate; }
    public void setGradeDate(LocalDate gradeDate) { this.gradeDate = gradeDate; }
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
