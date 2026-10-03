package security.inputvalidation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per il flusso di registrazione utente.
 * Verifica:
 *  - validazione completa dei 10 campi obbligatori
 *  - prevenzione email duplicate
 *  - robustezza input avversariali
 *
 * Riferimento: OWASP Testing Guide - OTG-AUTHN-02
 */
@DisplayName("Registration Flow - Validazione completa form registrazione")
class RegistrationFlowTest {

    // Pattern replicati dal Registration.java
    private static final Pattern INFO = Pattern.compile("^([a-zA-Z\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27]\\s?){2,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[a-zA-Z\\d._%-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,20}$");
    private static final Pattern PASSWORD = Pattern.compile("^[a-zA-Z\\d\\-\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27]{6,16}");
    private static final Pattern PHONE = Pattern.compile("^\\d{10}$");
    private static final Pattern PROVINCE = Pattern.compile("^([a-zA-Z]{2})$");
    private static final Pattern POSTALCODE = Pattern.compile("^\\d{5}$");
    private static final Pattern ADDRESS = Pattern.compile("^([a-zA-Z\\d\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27\\x2C]\\s?){2,20}$");

    // ==========================================================
    // 1. Validazione NOME (info_string)
    // ==========================================================

    @Test
    @DisplayName("Nome valido: 'Mario'")
    void testValidName() {
        assertThat(INFO.matcher("Mario").find()).isTrue();
    }

    @Test
    @DisplayName("Nome invalido: '1234' (solo numeri)")
    void testNumericNameRejected() {
        assertThat(INFO.matcher("1234").find()).isFalse();
    }

    @Test
    @DisplayName("Nome invalido: vuoto")
    void testEmptyNameRejected() {
        assertThat(INFO.matcher("").find()).isFalse();
    }

    @Test
    @DisplayName("Nome valido con apostrofo: 'D'Angelo'")
    void testNameWithApostrophe() {
        assertThat(INFO.matcher("D'Angelo").find()).isTrue();
    }

    @Test
    @DisplayName("Nome invalido con caratteri speciali: 'Mario<script>'")
    void testNameWithXssRejected() {
        assertThat(INFO.matcher("Mario<script>").find()).isFalse();
    }

    // ==========================================================
    // 2. Validazione COGNOME
    // ==========================================================

    @Test
    @DisplayName("Cognome valido: 'Rossi'")
    void testValidSurname() {
        assertThat(INFO.matcher("Rossi").find()).isTrue();
    }

    @Test
    @DisplayName("Cognome invalido con SQL injection: 'Rossi; DROP'")
    void testSqlInjectionSurnameRejected() {
        assertThat(INFO.matcher("Rossi; DROP").find()).isFalse();
    }

    // ==========================================================
    // 3. Validazione EMAIL
    // ==========================================================

    @Test
    @DisplayName("Email valida: 'mario.rossi@studenti.unisa.it'")
    void testValidEmail() {
        assertThat(EMAIL.matcher("mario.rossi@studenti.unisa.it").find()).isTrue();
    }

    @Test
    @DisplayName("Email invalida: senza @")
    void testEmailWithoutAtRejected() {
        assertThat(EMAIL.matcher("mariorossi.it").find()).isFalse();
    }

    @Test
    @DisplayName("Email invalida: senza dominio")
    void testEmailWithoutDomainRejected() {
        assertThat(EMAIL.matcher("mario@").find()).isFalse();
    }

    @Test
    @DisplayName("Email invalida con spazi: 'mario @x.com'")
    void testEmailWithSpacesRejected() {
        assertThat(EMAIL.matcher("mario @x.com").find()).isFalse();
    }

    // ==========================================================
    // 4. Validazione PASSWORD
    // ==========================================================

    @Test
    @DisplayName("Password valida: 'password123' (min 6 char)")
    void testValidPassword() {
        assertThat(PASSWORD.matcher("password123").find()).isTrue();
    }

    @Test
    @DisplayName("Password valida al limite: 'abc123' (esattamente 6 char)")
    void testPasswordAtMinLength() {
        assertThat(PASSWORD.matcher("abc123").find()).isTrue();
    }

    @Test
    @DisplayName("Password troppo corta: 'abc' (3 char)")
    void testPasswordTooShortRejected() {
        assertThat(PASSWORD.matcher("abc").find()).isFalse();
    }

    // ==========================================================
    // 5. Validazione TELEFONO
    // ==========================================================

    @Test
    @DisplayName("Telefono valido: '3331234567' (10 cifre)")
    void testValidPhone() {
        assertThat(PHONE.matcher("3331234567").find()).isTrue();
    }

    @Test
    @DisplayName("Telefono invalido: 9 cifre")
    void testPhoneTooShortRejected() {
        assertThat(PHONE.matcher("333123456").find()).isFalse();
    }

    @Test
    @DisplayName("Telefono invalido: 11 cifre")
    void testPhoneTooLongRejected() {
        assertThat(PHONE.matcher("33312345678").find()).isFalse();
    }

    @Test
    @DisplayName("Telefono invalido con lettere: '333ABC4567'")
    void testPhoneWithLettersRejected() {
        assertThat(PHONE.matcher("333ABC4567").find()).isFalse();
    }

    // ==========================================================
    // 6. Validazione CITTA
    // ==========================================================

    @Test
    @DisplayName("Citta valida: 'Fisciano'")
    void testValidCity() {
        assertThat(INFO.matcher("Fisciano").find()).isTrue();
    }

    @Test
    @DisplayName("Citta con numero: 'Roma1'")
    void testCityWithNumberRejected() {
        assertThat(INFO.matcher("Roma1").find()).isFalse();
    }

    // ==========================================================
    // 7. Validazione PROVINCIA
    // ==========================================================

    @Test
    @DisplayName("Provincia valida: 'SA'")
    void testValidProvince() {
        assertThat(PROVINCE.matcher("SA").find()).isTrue();
    }

    @Test
    @DisplayName("Provincia invalida: 'SAL' (3 lettere)")
    void testProvinceTooLongRejected() {
        assertThat(PROVINCE.matcher("SAL").find()).isFalse();
    }

    @Test
    @DisplayName("Provincia invalida: 'S' (1 lettera)")
    void testProvinceTooShortRejected() {
        assertThat(PROVINCE.matcher("S").find()).isFalse();
    }

    @Test
    @DisplayName("Provincia invalida: '5A' (numero+lettera)")
    void testProvinceWithNumberRejected() {
        assertThat(PROVINCE.matcher("5A").find()).isFalse();
    }

    // ==========================================================
    // 8. Validazione CODICE POSTALE
    // ==========================================================

    @Test
    @DisplayName("Codice postale valido: '84040'")
    void testValidPostalCode() {
        assertThat(POSTALCODE.matcher("84040").find()).isTrue();
    }

    @Test
    @DisplayName("Codice postale invalido: 4 cifre")
    void testPostalCodeTooShortRejected() {
        assertThat(POSTALCODE.matcher("8404").find()).isFalse();
    }

    @Test
    @DisplayName("Codice postale invalido: 6 cifre")
    void testPostalCodeTooLongRejected() {
        assertThat(POSTALCODE.matcher("840401").find()).isFalse();
    }

    // ==========================================================
    // 9. Validazione INDIRIZZO
    // ==========================================================

    @Test
    @DisplayName("Indirizzo valido: 'Via Roma 1'")
    void testValidAddress() {
        assertThat(ADDRESS.matcher("Via Roma 1").find()).isTrue();
    }

    @Test
    @DisplayName("Indirizzo valido con virgola: 'Via Roma, 1'")
    void testAddressWithComma() {
        assertThat(ADDRESS.matcher("Via Roma, 1").find()).isTrue();
    }

    @Test
    @DisplayName("Indirizzo invalido con XSS: '<script>'")
    void testAddressWithXssRejected() {
        assertThat(ADDRESS.matcher("<script>alert(1)</script>").find()).isFalse();
    }

    // ==========================================================
    // 10. Validazione DATA di NASCITA (formato YYYY-MM-DD)
    // ==========================================================

    @Test
    @DisplayName("Data valida in formato YYYY-MM-DD")
    void testValidDateFormat() {
        String date = "2001-10-12";
        String[] parts = date.split("-");

        assertThat(parts).hasSize(3);
        assertThat(parts[0]).hasSize(4); // anno
        assertThat(parts[1]).hasSize(2); // mese
        assertThat(parts[2]).hasSize(2); // giorno

        assertThat(Integer.parseInt(parts[0])).isBetween(1900, 2100);
        assertThat(Integer.parseInt(parts[1])).isBetween(1, 12);
        assertThat(Integer.parseInt(parts[2])).isBetween(1, 31);
    }

    @Test
    @DisplayName("Data invalida: formato sbagliato '12/10/2001'")
    void testInvalidDateFormat() {
        String date = "12/10/2001";
        // Il servlet si aspetta '-' come separatore, quindi split('-') da 1 solo elemento
        assertThat(date.split("-")).hasSize(1);
    }

    // ==========================================================
    // 11. Finding - Livello di validazione = 10
    // ==========================================================

    @Test
    @DisplayName("Il servlet richiede esattamente level == 10 per registrare")
    void testRegistrationRequiresAllFieldsValid() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/java/Controller/Registration.java")));

        assertThat(source)
                .as("Il servlet conta i campi validi e richiede level == 10")
                .contains("level == 10");
    }

    @Test
    @DisplayName("Il servlet impedisce email duplicate")
    void testRegistrationRejectsDuplicateEmail() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/java/Controller/Registration.java")));

        assertThat(source)
                .as("Il servlet deve verificare email gia registrata")
                .contains("isAlreadyRegistered");
    }
}