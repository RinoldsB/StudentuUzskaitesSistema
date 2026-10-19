package studentuuzskaitesistema.model;

/**
 * Students.
 */
public class Student {

    private int id;
    private String firstName;
    private String lastName;
    private String personalCode;
    private String email;
    private String phone;
    private Integer groupId;
    private String groupName;

    /** Izveido tukšu studentu. */
    public Student() {
    }

    /**
     * Izveido studentu ar visiem laukiem.
     *
     * @param id studenta identifikators
     * @param firstName vārds
     * @param lastName uzvārds
     * @param personalCode personas kods
     * @param email e-pasts
     * @param phone tālrunis
     * @param groupId grupas id vai null
     */
    public Student(int id, String firstName, String lastName, String personalCode,
            String email, String phone, Integer groupId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalCode = personalCode;
        this.email = email;
        this.phone = phone;
        this.groupId = groupId;
    }

    /**
     * Atgriež pilnu vārdu.
     *
     * @return vārds un uzvārds
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPersonalCode() { return personalCode; }
    public void setPersonalCode(String personalCode) { this.personalCode = personalCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    @Override
    public String toString() {
        return getFullName();
    }
}
