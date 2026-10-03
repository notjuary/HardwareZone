package security.dataprotection;

import Model.ConPool;
import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test di sicurezza per l'area "Data Protection".
 * Verifica che:
 *  - le password non siano memorizzate in chiaro
 *  - venga applicato un algoritmo di hashing
 *  - il pool di connessione non contenga credenziali hardcoded
 *  - la configurazione del DB avvenga tramite variabili d'ambiente
 *
 * Riferimento: OWASP Testing Guide - OTG-CRYPST, OTG-CONFIG
 */
@DisplayName("Data Protection - Hashing password e configurazione sicura del DB")
class DataProtectionTest {

    private UserBean user;

    @BeforeEach
    void setUp() {
        user = new UserBean();
    }

    // ============================================================
    // TEST SU UserBean - Hashing della password
    // ============================================================

    @Test
    @DisplayName("La password NON deve essere memorizzata in chiaro")
    void testPasswordIsNotStoredInPlaintext() {
        String plainPassword = "MySecretPassword123!";

        user.setPassword(plainPassword);

        assertThat(user.getPassword())
                .as("La password memorizzata non deve essere uguale a quella in chiaro")
                .isNotNull()
                .isNotEqualTo(plainPassword);
    }

    @Test
    @DisplayName("L'hash della password deve essere lungo 40 caratteri (SHA-1 hex)")
    void testPasswordHashHasExpectedLength() {
        user.setPassword("AnyPassword123");

        assertThat(user.getPassword())
                .as("Un hash SHA-1 in formato hex è lungo 40 caratteri")
                .hasSize(40);
    }

    @Test
    @DisplayName("L'hash deve contenere solo caratteri esadecimali")
    void testPasswordHashIsHexadecimal() {
        user.setPassword("AnyPassword123");

        assertThat(user.getPassword())
                .as("L'hash SHA-1 hex contiene solo caratteri [0-9a-f]")
                .matches("[0-9a-f]{40}");
    }

    @Test
    @DisplayName("La stessa password produce lo stesso hash (SHA-1 senza salt)")
    void testSamePasswordProducesSameHash() {
        UserBean user1 = new UserBean();
        UserBean user2 = new UserBean();

        user1.setPassword("SamePassword");
        user2.setPassword("SamePassword");

        assertThat(user1.getPassword())
                .as("SHA-1 è deterministico: stessa password -> stesso hash")
                .isEqualTo(user2.getPassword());
    }

    @Test
    @DisplayName("L'hash corrisponde al valore SHA-1 calcolato manualmente")
    void testPasswordHashMatchesManualSha1Computation() throws Exception {
        String plainPassword = "HelloWorld";
        user.setPassword(plainPassword);

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(plainPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] expectedBytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : expectedBytes) {
            sb.append(String.format("%02x", b));
        }

        assertThat(user.getPassword())
                .as("L'hash deve corrispondere al risultato atteso dello SHA-1")
                .isEqualTo(sb.toString());
    }

    @Test
    @DisplayName("Password diverse devono produrre hash diversi")
    void testDifferentPasswordsProduceDifferentHashes() {
        UserBean user1 = new UserBean();
        UserBean user2 = new UserBean();

        user1.setPassword("PasswordOne");
        user2.setPassword("PasswordTwo");

        assertThat(user1.getPassword())
                .as("Password diverse devono produrre hash diversi")
                .isNotEqualTo(user2.getPassword());
    }

    @Test
    @DisplayName("Il metodo setPassword gestisce input UTF-8 correttamente")
    void testSetPasswordHandlesUtf8Characters() {
        user.setPassword("Pàsswòrd€123");

        assertThat(user.getPassword())
                .as("Caratteri UTF-8 non devono causare errori")
                .isNotNull()
                .hasSize(40);
    }

    // ============================================================
    // TEST SU ConPool - Configurazione sicura del database
    // ============================================================

    @Test
    @DisplayName("ConPool.java NON deve contenere password hardcoded")
    void testConPoolHasNoHardcodedPassword() throws Exception {
        Path conPoolPath = Paths.get("src/main/java/Model/ConPool.java");

        assertThat(conPoolPath)
                .as("Il file ConPool.java deve esistere nel percorso atteso")
                .exists();

        String source = new String(Files.readAllBytes(conPoolPath));

        // Pattern tipici di password hardcoded
        assertThat(source)
                .as("ConPool non deve contenere setPassword(\"...\") con stringa literal")
                .doesNotMatch(".*setPassword\\(\"[^\"]+\"\\).*");

        assertThat(source)
                .as("ConPool non deve contenere password come 'teograuso01' o simili")
                .doesNotContain("teograuso01");
    }

    @Test
    @DisplayName("ConPool.java deve leggere le credenziali da System.getenv")
    void testConPoolReadsCredentialsFromEnvironment() throws Exception {
        Path conPoolPath = Paths.get("src/main/java/Model/ConPool.java");
        String source = new String(Files.readAllBytes(conPoolPath));

        assertThat(source)
                .as("ConPool deve leggere MYSQL_PASSWORD da variabile d'ambiente")
                .contains("System.getenv(\"MYSQL_PASSWORD\")");

        assertThat(source)
                .as("ConPool deve leggere MYSQL_USER da variabile d'ambiente")
                .contains("System.getenv(\"MYSQL_USER\")");
    }

    @Test
    @DisplayName("ConPool.java deve avere fallback fail-fast sulla password")
    void testConPoolFailsFastOnMissingPassword() throws Exception {
        Path conPoolPath = Paths.get("src/main/java/Model/ConPool.java");
        String source = new String(Files.readAllBytes(conPoolPath));

        assertThat(source)
                .as("ConPool deve lanciare IllegalStateException se MYSQL_PASSWORD è null")
                .contains("IllegalStateException");
    }

    // ============================================================
    // TEST TRASVERSALI - Nessuna credenziale nei sorgenti
    // ============================================================

    @Test
    @DisplayName("Nessun file .java del progetto contiene credenziali comuni hardcoded")
    void testNoHardcodedCredentialsInAnyJavaFile() throws Exception {
        Path srcPath = Paths.get("src/main/java");

        assertThat(srcPath)
                .as("La cartella src/main/java deve esistere")
                .exists();

        // Pattern di credenziali note da cercare
        String[] forbiddenPatterns = {
                "teograuso01",
                "setPassword(\"root\")",
                "password=root"
        };

        try (Stream<Path> files = Files.walk(srcPath)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        try {
                            String content = new String(Files.readAllBytes(javaFile));
                            for (String pattern : forbiddenPatterns) {
                                assertThat(content)
                                        .as("Il file " + javaFile + " non deve contenere: " + pattern)
                                        .doesNotContain(pattern);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Errore leggendo " + javaFile, e);
                        }
                    });
        }
    }
}