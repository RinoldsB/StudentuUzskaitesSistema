package studentuuzskaitesistema.util;

import studentuuzskaitesistema.model.User;

/**
 * Glabā informāciju par pašreizējo pieslēgto lietotāju.
 */
public class Session {

    private static User currentUser;

    private Session() {
        // Neļauj izveidot šīs klases objektus.
    }

    public static void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Lietotājs nevar būt null!");
        }

        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null
                && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isTeacher() {
        return currentUser != null
                && "TEACHER".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean hasRole(String role) {
        return currentUser != null
                && role != null
                && role.equalsIgnoreCase(currentUser.getRole());
    }
}