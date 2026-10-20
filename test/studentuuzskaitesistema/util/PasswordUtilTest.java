package studentuuzskaitesistema.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

/**
 * PasswordUtil klases JUnit 4 testi.
 */
public class PasswordUtilTest {

    @Test
    public void knownSha256Value() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                PasswordUtil.hash("abc"));
    }

    @Test
    public void hashHas64HexCharacters() {
        assertEquals(64, PasswordUtil.hash("admin123").length());
    }

    @Test
    public void sameInputSameHash() {
        assertEquals(PasswordUtil.hash("parole"), PasswordUtil.hash("parole"));
    }

    @Test
    public void differentInputDifferentHash() {
        assertNotEquals(PasswordUtil.hash("parole1"), PasswordUtil.hash("parole2"));
    }
}
