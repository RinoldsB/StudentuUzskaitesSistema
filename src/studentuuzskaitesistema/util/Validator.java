package studentuuzskaitesistema.util;

/**
 * Ievades datu validācija (tiek testēta ar JUnit4).
 */
public class Validator {

    private Validator() {
    }

    /**
     * Pārbauda, vai teksts nav null vai tukšs.
     *
     * @param text pārbaudāmais teksts
     * @return true, ja teksts satur vismaz vienu neatstarpes simbolu
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Pārbauda vārdu vai uzvārdu (burti, atstarpe, defise, 2-50 simboli).
     *
     * @param name vārds vai uzvārds
     * @return true, ja derīgs
     */
    public static boolean isValidName(String name) {
        return name != null && name.trim().matches("[\\p{L}][\\p{L} '\\-]{1,49}");
    }

    /**
     * Pārbauda personas kodu formātā 123456-12345.
     *
     * @param code personas kods
     * @return true, ja formāts pareizs
     */
    public static boolean isValidPersonalCode(String code) {
        return code != null && code.trim().matches("\\d{6}-\\d{5}");
    }

    /**
     * Pārbauda sekmes diapazonu (1-10).
     *
     * @param grade vērtējums
     * @return true, ja 1 līdz 10
     */
    public static boolean isValidGrade(int grade) {
        return grade >= 1 && grade <= 10;
    }

    /**
     * Pārbauda e-pastu. Tukšs e-pasts ir atļauts (lauks nav obligāts).
     *
     * @param email e-pasta adrese
     * @return true, ja tukšs vai derīgs
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        return email.trim().matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+");
    }

    /**
     * Pārbauda tālruni. Tukšs ir atļauts, citādi 8-15 cipari (ar iespējamu + sākumā).
     *
     * @param phone tālruņa numurs
     * @return true, ja tukšs vai derīgs
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        return phone.trim().matches("\\+?\\d{8,15}");
    }

    /**
     * Pārbauda lietotājvārdu (3-50 simboli: burti, cipari, pasvītra).
     *
     * @param username lietotājvārds
     * @return true, ja derīgs
     */
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{3,50}");
    }

    /**
     * Pārbauda paroli (vismaz 6 simboli).
     *
     * @param password parole
     * @return true, ja garums vismaz 6
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
