package studentuuzskaitesistema.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * JUnit4 testi klasei {@link Validator}: ievades datu validācijas pārbaude
 * (derīgi dati, nederīgi dati un robežgadījumi).
 */
public class ValidatorTest {

    /** Teksts nedrīkst būt null, tukšs vai sastāvēt tikai no atstarpēm. */
    @Test
    public void isNotEmptyRejectsNullEmptyAndBlankText() {
        assertFalse(Validator.isNotEmpty(null));
        assertFalse(Validator.isNotEmpty(""));
        assertFalse(Validator.isNotEmpty("   "));
        assertTrue(Validator.isNotEmpty("a"));
        assertTrue(Validator.isNotEmpty("  a  "));
    }

    /** Derīgi vārdi: latviešu burti, defise, apostrofs un atstarpe. */
    @Test
    public void isValidNameAcceptsLettersHyphenApostropheAndSpace() {
        assertTrue(Validator.isValidName("Anna"));
        assertTrue(Validator.isValidName("Mārtiņš"));
        assertTrue(Validator.isValidName("Jānis-Pēteris"));
        assertTrue(Validator.isValidName("O'Neil"));
        assertTrue(Validator.isValidName("Anna Marija"));
        assertTrue(Validator.isValidName("  Anna  "));
    }

    /** Nederīgi vārdi: null, tukšs, ar cipariem vai speciālajiem simboliem. */
    @Test
    public void isValidNameRejectsDigitsSymbolsAndEmpty() {
        assertFalse(Validator.isValidName(null));
        assertFalse(Validator.isValidName(""));
        assertFalse(Validator.isValidName("1Anna"));
        assertFalse(Validator.isValidName("Anna1"));
        assertFalse(Validator.isValidName("-Anna"));
        assertFalse(Validator.isValidName("Anna@"));
    }

    /** Robežgadījumi vārda garumam: 1 simbols nav derīgs, 2 un 50 ir derīgi, 51 nav. */
    @Test
    public void isValidNameChecksLengthBoundaries() {
        assertFalse(Validator.isValidName("A"));
        assertTrue(Validator.isValidName("Jo"));
        assertTrue(Validator.isValidName("A".repeat(50)));
        assertFalse(Validator.isValidName("A".repeat(51)));
    }

    /** Personas kods ar pareizu formātu 123456-12345 (arī ar atstarpēm apkārt). */
    @Test
    public void isValidPersonalCodeAcceptsCorrectFormat() {
        assertTrue(Validator.isValidPersonalCode("010101-12345"));
        assertTrue(Validator.isValidPersonalCode(" 010101-12345 "));
    }

    /** Personas kods ar nepareizu formātu tiek noraidīts. */
    @Test
    public void isValidPersonalCodeRejectsWrongFormat() {
        assertFalse(Validator.isValidPersonalCode(null));
        assertFalse(Validator.isValidPersonalCode(""));
        assertFalse(Validator.isValidPersonalCode("01010112345"));
        assertFalse(Validator.isValidPersonalCode("010101-1234"));
        assertFalse(Validator.isValidPersonalCode("010101-123456"));
        assertFalse(Validator.isValidPersonalCode("01010a-12345"));
    }

    /** Sekme ir derīga tikai diapazonā no 1 līdz 10 (ieskaitot robežas). */
    @Test
    public void isValidGradeAcceptsOnlyOneToTen() {
        assertTrue(Validator.isValidGrade(1));
        assertTrue(Validator.isValidGrade(7));
        assertTrue(Validator.isValidGrade(10));
        assertFalse(Validator.isValidGrade(0));
        assertFalse(Validator.isValidGrade(11));
        assertFalse(Validator.isValidGrade(-1));
        assertFalse(Validator.isValidGrade(Integer.MIN_VALUE));
        assertFalse(Validator.isValidGrade(Integer.MAX_VALUE));
    }

    /** E-pasts nav obligāts: tukšs un null ir atļauts, derīgas adreses tiek pieņemtas. */
    @Test
    public void isValidEmailAllowsEmptyAndValidAddresses() {
        assertTrue(Validator.isValidEmail(null));
        assertTrue(Validator.isValidEmail(""));
        assertTrue(Validator.isValidEmail("   "));
        assertTrue(Validator.isValidEmail("anna@example.com"));
        assertTrue(Validator.isValidEmail("a.b+c@mail.example.lv"));
    }

    /** Nepareizi veidotas e-pasta adreses tiek noraidītas. */
    @Test
    public void isValidEmailRejectsMalformedAddresses() {
        assertFalse(Validator.isValidEmail("anna"));
        assertFalse(Validator.isValidEmail("anna@"));
        assertFalse(Validator.isValidEmail("@example.com"));
        assertFalse(Validator.isValidEmail("anna@example"));
        assertFalse(Validator.isValidEmail("anna @example.com"));
    }

    /** Tālrunis nav obligāts; ja norādīts, tam jābūt 8-15 cipariem (var sākties ar +). */
    @Test
    public void isValidPhoneAllowsEmptyAndEightToFifteenDigits() {
        assertTrue(Validator.isValidPhone(null));
        assertTrue(Validator.isValidPhone(""));
        assertTrue(Validator.isValidPhone("20000001"));
        assertTrue(Validator.isValidPhone("+37120000001"));
        assertTrue(Validator.isValidPhone("123456789012345"));
    }

    /** Tālrunis ar nepareizu garumu vai nederīgiem simboliem tiek noraidīts. */
    @Test
    public void isValidPhoneRejectsWrongLengthAndSymbols() {
        assertFalse(Validator.isValidPhone("1234567"));
        assertFalse(Validator.isValidPhone("1234567890123456"));
        assertFalse(Validator.isValidPhone("2000 0001"));
        assertFalse(Validator.isValidPhone("abc12345"));
        assertFalse(Validator.isValidPhone("++37120000001"));
    }

    /** Derīgs lietotājvārds: 3-50 simboli (latīņu burti, cipari, pasvītra). */
    @Test
    public void isValidUsernameAcceptsLettersDigitsAndUnderscore() {
        assertTrue(Validator.isValidUsername("abc"));
        assertTrue(Validator.isValidUsername("user_1"));
        assertTrue(Validator.isValidUsername("A".repeat(50)));
    }

    /** Par īss, par garš vai ar nederīgiem simboliem lietotājvārds tiek noraidīts. */
    @Test
    public void isValidUsernameRejectsShortLongAndInvalidSymbols() {
        assertFalse(Validator.isValidUsername(null));
        assertFalse(Validator.isValidUsername("ab"));
        assertFalse(Validator.isValidUsername("a b"));
        assertFalse(Validator.isValidUsername("user-1"));
        assertFalse(Validator.isValidUsername("jānis"));
        assertFalse(Validator.isValidUsername("A".repeat(51)));
    }

    /** Parolei jābūt vismaz 6 simbolus garai. */
    @Test
    public void isValidPasswordRequiresAtLeastSixCharacters() {
        assertFalse(Validator.isValidPassword(null));
        assertFalse(Validator.isValidPassword(""));
        assertFalse(Validator.isValidPassword("12345"));
        assertTrue(Validator.isValidPassword("123456"));
        assertTrue(Validator.isValidPassword("garaParole123"));
    }
}
