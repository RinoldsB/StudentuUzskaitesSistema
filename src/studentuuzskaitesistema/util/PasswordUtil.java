package studentuuzskaitesistema.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Drošai paroļu jaucējvērtību izveidei un pārbaudei.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 120000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Izveido paroli ar PBKDF2 un nejaušu sāli.
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Parole nevar būt tukša!");
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] hash = generateHash(password.toCharArray(), salt, ITERATIONS);

        return "PBKDF2$"
                + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Pārbauda ievadīto paroli pret saglabāto jaucējvērtību.
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split("\\$");

            if (parts.length != 4 || !"PBKDF2".equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            if (iterations < 1) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = generateHash(
                    password.toCharArray(),
                    salt,
                    iterations
            );

            return java.security.MessageDigest.isEqual(
                    expectedHash,
                    actualHash
            );

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] generateHash(
            char[] password,
            byte[] salt,
            int iterations) {

        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                iterations,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException(
                    "Neizdevās izveidot paroles jaucējvērtību!",
                    ex
            );

        } finally {
            spec.clearPassword();
        }
    }
}