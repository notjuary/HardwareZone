package security.audit.inputvalidation;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per l'area "Input Validation".
 * Verifica che gli input malevoli non causino comportamenti anomali
 * e che le regex di validazione del Registration blocchino payload noti.
 *
 * Riferimento: OWASP Testing Guide - OTG-INPVAL
 */
@DisplayName("Input Validation - SQL Injection, XSS e validazione formati")
class InputValidationTest {

    private UserBean user;

    // Regex replicate dal Registration servlet (contract test)
    private static final Pattern INFO_STRING = Pattern.compile("^([a-zA-Z\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27]\\s?){2,20}$");
    private static final Pattern EMAIL_STRING = Pattern.compile("^[a-zA-Z\\d._%-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,20}$");
    private static final Pattern PASSWORD_STRING = Pattern.compile("^[a-zA-Z\\d\\-\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27]{6,16}");
    private static final Pattern PHONE_STRING = Pattern.compile("^\\d{10}$");
    private static final Pattern PROVINCE_STRING = Pattern.compile("^([a-zA-Z]{2})$");
    private static final Pattern POSTALCODE_STRING = Pattern.compile("^\\d{5}$");

    @BeforeEach
    void setUp() {
        user = new UserBean();
    }

    // ==========================================================
    // 1. ROBUSTEZZA DEI SETTER - Input malevoli non devono crashare
    // ==========================================================

    @Test
    @DisplayName("setName gestisce input null senza crashare")
    void testSetNameHandlesNull() {
        user.setName(null);
        assertThat(user.getName()).isNull();
    }

    @Test
    @DisplayName("setName accetta payload SQL injection ma NON lo esegue")
    void testSetNameAcceptsSqlPayloadWithoutExecution() {
        String sqlPayload = "'; DROP TABLE Utente; --";
        user.setName(sqlPayload);
        assertThat(user.getName()).isEqualTo(sqlPayload);
    }

    @Test
    @DisplayName("setName accetta payload XSS ma lo memorizza inerte")
    void testSetNameAcceptsXssPayload() {
        String xssPayload = "<script>alert('XSS')</script>";
        user.setName(xssPayload);
        assertThat(user.getName()).contains("<script>");
    }

    @Test
    @DisplayName("setEmail accetta email valide")
    void testSetEmailAcceptsValidEmail() {
        user.setEmail("user@example.com");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("setPassword gestisce stringa vuota senza crashare")
    void testSetPasswordHandlesEmptyString() {
        user.setPassword("");
        assertThat(user.getPassword()).hasSize(40);
    }

    // ==========================================================
    // 2. VALIDAZIONE REGEX - Contract test sui pattern del Registration
    // ==========================================================

    @Test
    @DisplayName("Email: 'user@example.com' è valida")
    void testValidEmailPassesRegex() {
        assertThat(EMAIL_STRING.matcher("user@example.com").find()).isTrue();
    }

    @Test
    @DisplayName("Email: payload SQL injection viene RESPINTO dalla regex")
    void testSqlInjectionInEmailIsRejected() {
        assertThat(EMAIL_STRING.matcher("' OR '1'='1").find()).isFalse();
        assertThat(EMAIL_STRING.matcher("admin'--@x.com").find()).isFalse();
    }

    @Test
    @DisplayName("Email: payload XSS viene RESPINTO dalla regex")
    void testXssInEmailIsRejected() {
        assertThat(EMAIL_STRING.matcher("<script>alert(1)</script>").find()).isFalse();
    }

    @Test
    @DisplayName("Password: 'password123' è valida (almeno 6 caratteri)")
    void testValidPasswordPassesRegex() {
        assertThat(PASSWORD_STRING.matcher("password123").find()).isTrue();
    }

    @Test
    @DisplayName("Password: payload SQL injection viene RESPINTO")
    void testSqlInjectionInPasswordIsRejected() {
        assertThat(PASSWORD_STRING.matcher("' OR 1=1--").find()).isFalse();
    }

    @Test
    @DisplayName("Phone: '1234567890' è valido (10 cifre)")
    void testValidPhonePassesRegex() {
        assertThat(PHONE_STRING.matcher("1234567890").find()).isTrue();
    }

    @Test
    @DisplayName("Phone: 'abc' viene RESPINTO")
    void testNonNumericPhoneIsRejected() {
        assertThat(PHONE_STRING.matcher("abc").find()).isFalse();
        assertThat(PHONE_STRING.matcher("12345abcde").find()).isFalse();
    }

    @Test
    @DisplayName("PostalCode: '80100' è valido (5 cifre)")
    void testValidPostalCodePassesRegex() {
        assertThat(POSTALCODE_STRING.matcher("80100").find()).isTrue();
    }

    @Test
    @DisplayName("PostalCode: '80A00' viene RESPINTO")
    void testAlphanumericPostalCodeIsRejected() {
        assertThat(POSTALCODE_STRING.matcher("80A00").find()).isFalse();
    }

    @Test
    @DisplayName("Province: 'NA' è valida (2 lettere)")
    void testValidProvincePassesRegex() {
        assertThat(PROVINCE_STRING.matcher("NA").find()).isTrue();
    }

    @Test
    @DisplayName("Province: 'N' o 'NAP' vengono RESPINTI")
    void testInvalidProvinceIsRejected() {
        assertThat(PROVINCE_STRING.matcher("N").find()).isFalse();
        assertThat(PROVINCE_STRING.matcher("NAP").find()).isFalse();
    }

    @Test
    @DisplayName("Info string: nome con apostrofo 'D'Angelo' è valido")
    void testNameWithApostropheIsValid() {
        assertThat(INFO_STRING.matcher("D'Angelo").find()).isTrue();
    }

    @Test
    @DisplayName("Info string: payload SQL injection viene RESPINTO")
    void testSqlInjectionInNameIsRejected() {
        assertThat(INFO_STRING.matcher("'; DROP TABLE--").find()).isFalse();
        assertThat(INFO_STRING.matcher("<script>alert(1)</script>").find()).isFalse();
    }

    // ==========================================================
    // 3. DETERMINISMO SHA-1 CON INPUT SPECIALI
    // ==========================================================

    @Test
    @DisplayName("SHA-1 produce hash deterministici anche con input speciali")
    void testSha1DeterminismWithSpecialInput() {
        UserBean u1 = new UserBean();
        UserBean u2 = new UserBean();

        u1.setPassword("P@ssw0rd!€#");
        u2.setPassword("P@ssw0rd!€#");

        assertThat(u1.getPassword()).isEqualTo(u2.getPassword());
    }

    @Test
    @DisplayName("SHA-1 gestisce input con soli caratteri Unicode")
    void testSha1HandlesUnicode() {
        user.setPassword("ÀÈÌÒÙ");
        assertThat(user.getPassword()).hasSize(40);
    }
}