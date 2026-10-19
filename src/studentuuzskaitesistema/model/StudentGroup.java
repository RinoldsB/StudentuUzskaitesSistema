package studentuuzskaitesistema.model;

/**
 * Studentu grupa.
 */
public class StudentGroup {

    private int id;
    private String name;
    private int course;
    private Integer teacherId;
    private String teacherName;

    /** Izveido tukšu grupu. */
    public StudentGroup() {
    }

    /**
     * Izveido grupu ar pamata laukiem.
     *
     * @param id grupas identifikators
     * @param name grupas nosaukums
     * @param course kurss (1-6)
     * @param teacherId pasniedzēja (klases audzinātāja) id vai null
     */
    public StudentGroup(int id, String name, int course, Integer teacherId) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.teacherId = teacherId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCourse() { return course; }
    public void setCourse(int course) { this.course = course; }
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    @Override
    public String toString() {
        return name;
    }
}
