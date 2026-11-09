package studentuuzskaitesistema.util;

/**
 * Ievades datu validācijas palīgklase.
 */
public class Validator {

    private Validator() {
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        return name != null
                && name.trim().matches("[\\p{L}][\\p{L} '\\-]{1,49}");
    }

    public static boolean isValidPersonalCode(String code) {
        return code != null
                && code.trim().matches("\\d{6}-\\d{5}");
    }

    public static boolean isValidGrade(int grade) {
        return grade >= 1 && grade <= 10;
    }

    public static boolean isValidCourse(int course) {
        return course >= 1 && course <= 6;
    }

    public static boolean isValidSubject(String subject) {
        return subject != null
                && !subject.trim().isEmpty()
                && subject.trim().length() <= 100;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        return email.trim().matches(
                "[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }

        return phone.trim().matches("\\+?\\d{8,15}");
    }

    public static boolean isValidUsername(String username) {
        return username != null
                && username.matches("[A-Za-z0-9_]{3,50}");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isValidRole(String role) {
        return "ADMIN".equals(role) || "TEACHER".equals(role);
    }
}