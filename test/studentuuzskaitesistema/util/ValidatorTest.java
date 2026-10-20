package studentuuzskaitesistema.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Validator klases JUnit 4 testi.
 */
public class ValidatorTest {

    @Test
    public void isNotEmpty() {
        assertTrue(Validator.isNotEmpty("a"));
        assertFalse(Validator.isNotEmpty(""));
        assertFalse(Validator.isNotEmpty("   "));
        assertFalse(Validator.isNotEmpty(null));
    }

    @Test
    public void isValidName() {
        assertTrue(Validator.isValidName("Anna"));
        assertTrue(Validator.isValidName("Mārtiņš"));
        assertTrue(Validator.isValidName("Anna-Marija"));
        assertFalse(Validator.isValidName("A"));
        assertFalse(Validator.isValidName("Anna1"));
        assertFalse(Validator.isValidName(""));
        assertFalse(Validator.isValidName(null));
    }

    @Test
    public void isValidPersonalCode() {
        assertTrue(Validator.isValidPersonalCode("010101-12345"));
        assertFalse(Validator.isValidPersonalCode("01010112345"));
        assertFalse(Validator.isValidPersonalCode("010101-1234"));
        assertFalse(Validator.isValidPersonalCode("abcdef-12345"));
        assertFalse(Validator.isValidPersonalCode(null));
    }

    @Test
    public void isValidGrade() {
        assertTrue(Validator.isValidGrade(1));
        assertTrue(Validator.isValidGrade(10));
        assertFalse(Validator.isValidGrade(0));
        assertFalse(Validator.isValidGrade(11));
        assertFalse(Validator.isValidGrade(-5));
    }

    @Test
    public void isValidEmail() {
        assertTrue(Validator.isValidEmail("anna@example.com"));
        assertTrue(Validator.isValidEmail(""));
        assertTrue(Validator.isValidEmail(null));
        assertFalse(Validator.isValidEmail("anna@"));
        assertFalse(Validator.isValidEmail("anna.example.com"));
    }

    @Test
    public void isValidPhone() {
        assertTrue(Validator.isValidPhone("20000001"));
        assertTrue(Validator.isValidPhone("+37120000001"));
        assertTrue(Validator.isValidPhone(""));
        assertFalse(Validator.isValidPhone("1234567"));
        assertFalse(Validator.isValidPhone("2000000a"));
    }

    @Test
    public void isValidUsername() {
        assertTrue(Validator.isValidUsername("admin"));
        assertTrue(Validator.isValidUsername("teacher_1"));
        assertFalse(Validator.isValidUsername("ab"));
        assertFalse(Validator.isValidUsername("with space"));
        assertFalse(Validator.isValidUsername(null));
    }

    @Test
    public void isValidPassword() {
        assertTrue(Validator.isValidPassword("123456"));
        assertFalse(Validator.isValidPassword("12345"));
        assertFalse(Validator.isValidPassword(null));
    }
}
