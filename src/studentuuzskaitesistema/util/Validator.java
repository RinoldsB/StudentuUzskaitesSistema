package studentuuzskaitesistema.util;

/**
 * Ievades datu validācijas palīgklase.
 */
public class Validator {

    private Validator() {
    }

    /**
     * Pārbauda, vai teksta virkne nav {@code null} un nesastāv tikai no atstarpēm.
     *
     * @param text pārbaudāmā virkne
     * @return {@code true}, ja virkne satur vismaz vienu rakstzīmi
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Pārbauda, vai vārds vai uzvārds ir derīgs (2–50 burti, atstarpes, defises vai apostrofi).
     *
     * @param name pārbaudāmais vārds
     * @return {@code true}, ja vārds atbilst formātam
     */
    public static boolean isValidName(String name) {
        return name != null
                && name.trim().matches("[\\p{L}][\\p{L} '\\-]{1,49}");
    }

    /**
     * Pārbauda, vai personas kods atbilst formātam {@code 123456-12345}.
     *
     * @param code personas kods
     * @return {@code true}, ja formāts ir derīgs
     */
    public static boolean isValidPersonalCode(String code) {
        return code != null
                && code.trim().matches("\\d{6}-\\d{5}");
    }

    /**
     * Pārbauda, vai sekme (atzīme) ir vesels skaitlis diapazonā no 1 līdz 10.
     *
     * @param grade atzīme
     * @return {@code true}, ja atzīme ir no 1 līdz 10
     */
    public static boolean isValidGrade(int grade) {
        return grade >= 1 && grade <= 10;
    }

    /**
     * Pārbauda, vai kurss ir diapazonā no 1 līdz 6.
     *
     * @param course kurss
     * @return {@code true}, ja kurss ir no 1 līdz 6
     */
    public static boolean isValidCourse(int course) {
        return course >= 1 && course <= 6;
    }

    /**
     * Pārbauda, vai priekšmeta nosaukums nav tukšs un nepārsniedz 100 rakstzīmes.
     *
     * @param subject priekšmeta nosaukums
     * @return {@code true}, ja nosaukums ir derīgs
     */
    public static boolean isValidSubject(String subject) {
        return subject != null
                && !subject.trim().isEmpty()
                && subject.trim().length() <= 100;
    }

    /**
     * Pārbauda, vai e-pasta adrese ir tukša (nav obligāta) vai atbilst standarta formātam.
     *
     * @param email e-pasta adrese
     * @return {@code true}, ja e-pasts ir tukšs vai derīgs
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        return email.trim().matches(
                "[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+");
    }

    /**
     * Pārbauda, vai tālruņa numurs ir tukšs (nav obligāts) vai satur 8–15 ciparus (var sākties ar +).
     *
     * @param phone tālruņa numurs
     * @return {@code true}, ja tālrunis ir tukšs vai derīgs
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }

        return phone.trim().matches("\\+?\\d{8,15}");
    }

    /**
     * Pārbauda, vai lietotājvārds ir 3–50 simbolus garš un satur tikai latīņu burtus, ciparus vai pasvītru.
     *
     * @param username lietotājvārds
     * @return {@code true}, ja lietotājvārds ir derīgs
     */
    public static boolean isValidUsername(String username) {
        return username != null
                && username.matches("[A-Za-z0-9_]{3,50}");
    }

    /**
     * Pārbauda, vai parole ir vismaz 6 simbolus gara.
     *
     * @param password parole
     * @return {@code true}, ja parole ir vismaz 6 simboli
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Pārbauda, vai lietotāja loma ir {@code ADMIN} vai {@code TEACHER}.
     *
     * @param role lietotāja loma
     * @return {@code true}, ja loma ir derīga
     */
    public static boolean isValidRole(String role) {
        return "ADMIN".equals(role) || "TEACHER".equals(role);
    }
}
