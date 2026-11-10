package studentuuzskaitesistema.util;

import studentuuzskaitesistema.model.User;

/**
 * Glabā informāciju par pašreizējo sistēmā pieslēgto lietotāju un viņa lomu.
 */
public class Session {

    private static User currentUser;

    private Session() {
        // Neļauj izveidot šīs klases objektus.
    }

    /**
     * Saglabā pieslēgušos lietotāju sesijā.
     *
     * @param user pieslēgtais lietotājs
     */
    public static void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Lietotājs nevar būt null!");
        }

        currentUser = user;
    }

    /**
     * Notīra pašreizējo sesiju, lietotājam izejot no sistēmas.
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Atgriež pašreiz pieslēgto lietotāju.
     *
     * @return {@link User} objekts vai {@code null}, ja neviens nav pieslēdzies
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Pārbauda, vai sistēmā ir pieslēdzies lietotājs.
     *
     * @return {@code true}, ja lietotājs ir pieslēdzies
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Pārbauda, vai pieslēgtais lietotājs ir administrators (lietvedis).
     *
     * @return {@code true}, ja loma ir ADMIN
     */
    public static boolean isAdmin() {
        return currentUser != null
                && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Pārbauda, vai pieslēgtais lietotājs ir pasniedzējs.
     *
     * @return {@code true}, ja loma ir TEACHER
     */
    public static boolean isTeacher() {
        return currentUser != null
                && "TEACHER".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Pārbauda, vai pieslēgtajam lietotājam ir norādītā loma.
     *
     * @param role pārbaudāmā loma
     * @return {@code true}, ja loma sakrīt
     */
    public static boolean hasRole(String role) {
        return currentUser != null
                && role != null
                && role.equalsIgnoreCase(currentUser.getRole());
    }
}
