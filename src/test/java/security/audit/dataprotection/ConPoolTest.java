package security.audit.dataprotection;

import Model.ConPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test di sicurezza per l'area "Data Protection" (SAST manuale su ConPool).
 * Verifica che ConPool.java rispetti le best practice di sicurezza:
 *  - Credenziali lette da variabili d'ambiente (fix GitGuardian)
 *  - Nessuna password hardcoded (regression test)
 *  - Fail-fast se MYSQL_PASSWORD non è impostata

 * Compatibile con Java 8 (no Files.readString, no Path.of).

 * Riferimento: OWASP Testing Guide - OTG-CONFIG, CWE-798
 */
@DisplayName("Data Protection - ConPool credenziali DB (audit)")
class ConPoolTest {

    private static final String SOURCE_PATH = "src/main/java/Model/ConPool.java";

    @BeforeEach
    @AfterEach
    void resetDatasource() throws Exception {
        // Reset dello stato statico di ConPool per isolare i test
        try {
            Field field = ConPool.class.getDeclaredField("datasource");
            field.setAccessible(true);
            field.set(null, null);
        } catch (NoSuchFieldException e) {
            // Se il campo non esiste (versione diversa), ignora
        }
    }

    /**
     * Helper compatibile con Java 8: legge un file come stringa.
     */
    private String readSourceFile() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(SOURCE_PATH));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ==========================================================
    // 1. Credenziali lette da variabili d'ambiente
    // ==========================================================

    @Test
    @DisplayName("ConPool legge MYSQL_PASSWORD da variabile d'ambiente")
    void testLeggeMysqlPasswordDaEnv() throws Exception {
        String source = readSourceFile();
        assertTrue(source.contains("System.getenv(\"MYSQL_PASSWORD\")"),
                "ConPool deve leggere MYSQL_PASSWORD tramite System.getenv()");
    }

    @Test
    @DisplayName("ConPool legge tutte le credenziali da variabili d'ambiente")
    void testLeggeTutteLeCredenzialiDaEnv() throws Exception {
        String source = readSourceFile();

        assertTrue(source.contains("System.getenv(\"MYSQL_HOST\")"),
                "deve leggere MYSQL_HOST");
        assertTrue(source.contains("System.getenv(\"MYSQL_PORT\")"),
                "deve leggere MYSQL_PORT");
        assertTrue(source.contains("System.getenv(\"MYSQL_DATABASE\")"),
                "deve leggere MYSQL_DATABASE");
        assertTrue(source.contains("System.getenv(\"MYSQL_USER\")"),
                "deve leggere MYSQL_USER");
        assertTrue(source.contains("System.getenv(\"MYSQL_PASSWORD\")"),
                "deve leggere MYSQL_PASSWORD");
    }

    // ==========================================================
    // 2. Nessuna password hardcoded (regression GitGuardian)
    // ==========================================================

    @Test
    @DisplayName("ConPool non contiene password hardcoded (regression GitGuardian)")
    void testNessunaPasswordHardcoded() throws Exception {
        String source = readSourceFile();

        // Pattern tipici di password hardcoded
        assertFalse(source.matches("(?s).*setPassword\\(\"[^\"]+\"\\).*"),
                "ConPool non deve contenere setPassword(\"...\") con stringa literal");
        assertFalse(source.contains("teograuso01"),
                "ConPool non deve contenere la vecchia password compromessa");
        assertFalse(source.contains("password=root"),
                "ConPool non deve contenere password comuni hardcoded");
        assertFalse(source.contains("password=admin"),
                "ConPool non deve contenere password comuni hardcoded");
    }

    // ==========================================================
    // 3. Fail-fast se MYSQL_PASSWORD non è impostata
    // ==========================================================

    @Test
    @DisplayName("ConPool applica il fail-fast se MYSQL_PASSWORD non è impostata")
    void testFailFastSenzaPassword() {
        // Il test si esegue solo se la variabile d'ambiente non è impostata
        Assumptions.assumeTrue(System.getenv("MYSQL_PASSWORD") == null,
                "Test saltato: MYSQL_PASSWORD è impostata nell'ambiente corrente");

        assertThrows(IllegalStateException.class, ConPool::getConnection,
                "Senza MYSQL_PASSWORD, getConnection deve lanciare IllegalStateException");
    }

    // ==========================================================
    // 4. Configurazione driver e URL
    // ==========================================================

    @Test
    @DisplayName("ConPool usa il driver MySQL corretto")
    void testUsaDriverMySQLCorretto() throws Exception {
        String source = readSourceFile();

        assertTrue(source.contains("com.mysql.cj.jdbc.Driver"),
                "ConPool deve usare il driver 'com.mysql.cj.jdbc.Driver'");
        assertFalse(source.contains("\"com.mysql.jdbc.Driver\""),
                "ConPool non deve usare il vecchio driver deprecato");
    }

    @Test
    @DisplayName("ConPool costruisce correttamente l'URL JDBC")
    void testUrlJdbcCorretto() throws Exception {
        String source = readSourceFile();

        assertTrue(source.contains("jdbc:mysql://"),
                "L'URL JDBC deve iniziare con 'jdbc:mysql://'");
        assertTrue(source.contains("serverTimezone"),
                "L'URL JDBC deve specificare il serverTimezone");
    }

    // ==========================================================
    // 5. Parametri del pool
    // ==========================================================

    @Test
    @DisplayName("ConPool configura il pool con parametri di sicurezza")
    void testParametriPoolConfigurati() throws Exception {
        String source = readSourceFile();

        assertTrue(source.contains("setRemoveAbandoned(true)"),
                "ConPool deve abilitare la rimozione delle connessioni abbandonate");
        assertTrue(source.contains("setRemoveAbandonedTimeout"),
                "ConPool deve impostare un timeout per le connessioni abbandonate");
        assertTrue(source.contains("setMaxActive"),
                "ConPool deve configurare il numero massimo di connessioni attive");
    }
}