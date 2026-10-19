package studentuuzskaitesistema.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Palīgklase paroļu jaucējkodu (SHA-256) izveidei.
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Aprēķina paroles SHA-256 jaucējkodu.
     *
     * @param password parole atklātā tekstā
     * @return 64 simbolu heksadecimāls jaucējkods
     */
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nav pieejams", e);
        }
    }
}
