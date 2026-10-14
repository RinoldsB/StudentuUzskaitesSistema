package studentuuzskaitessistema;

/**
 * Glabā pašlaik pieteikušos lietotāju (sesijas dati).
 */
public class Session {

    private static User currentUser;

    private Session() {
    }

    /**
     * Iestata pašreizējo lietotāju pēc pieteikšanās.
     *
     * @param user pieteikušais lietotājs
     */
    public static void login(User user) {
        currentUser = user;
    }

    /** Izbeidz sesiju (atteikšanās). */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Atgriež pašreizējo lietotāju.
     *
     * @return pieteikušais lietotājs vai null
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Pārbauda, vai pieteicies lietvedis (administrators).
     *
     * @return true, ja lietotāja loma ir ADMIN
     */
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
}
