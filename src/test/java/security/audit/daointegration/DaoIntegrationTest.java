package security.audit.daointegration;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per l'area "DAO Integration".
 * Verifica che i DAO utilizzino correttamente PreparedStatement
 * e che lo schema del database sia coerente con il codice.
 *
 * NOTA: i test di integrazione con H2 non sono eseguibili senza
 * modificare ConPool (driver MySQL hardcoded). Si utilizzano
 * quindi tecniche di analisi statica per garantire i controlli.
 *
 * Riferimento: OWASP Testing Guide - OTG-DATA, OWASP A03:2021
 */
@DisplayName("DAO Integration - PreparedStatement, schema e SQL injection")
class DaoIntegrationTest {

    // ==========================================================
    // 1. Verifica uso di PreparedStatement nei DAO corretti
    // ==========================================================

    @Test
    @DisplayName("OrderDAO usa PreparedStatement (no SQL injection)")
    void testOrderDAO_UsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/OrderDAO.java");

        assertThat(source)
                .as("OrderDAO deve usare PreparedStatement e non Statement con createStatement()")
                .contains("PreparedStatement")
                .doesNotContain("con.createStatement()");
    }

    @Test
    @DisplayName("OrderProductDAO usa PreparedStatement (no SQL injection)")
    void testOrderProductDAO_UsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/OrderProductDAO.java");

        assertThat(source)
                .as("OrderProductDAO deve usare PreparedStatement e non Statement")
                .contains("PreparedStatement")
                .doesNotContain("con.createStatement()");
    }

    @Test
    @DisplayName("CartDAO usa PreparedStatement")
    void testCartDAO_UsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/CartDAO.java");

        assertThat(source)
                .as("CartDAO deve usare PreparedStatement e non Statement")
                .contains("PreparedStatement")
                .doesNotContain("con.createStatement()");
    }

    // ==========================================================
    // 2. DOCUMENTAZIONE - Finding SQL Injection
    // ==========================================================

    @Test
    @DisplayName("REGRESSION: UserDAO.doUpdate usa PreparedStatement (SQL Injection fixata)")
    void testUserDAOUpdateUsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/UserDAO.java");

        assertThat(source)
                .as("UserDAO deve usare PreparedStatement e non Statement")
                .doesNotContain("con.createStatement()")
                .contains("PreparedStatement");
    }

    @Test
    @DisplayName("REGRESSION: UserDAO.doUpdateState usa PreparedStatement")
    void testUserDAOUpdateStateUsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/UserDAO.java");

        assertThat(source)
                .as("UserDAO.doUpdateState non deve usare concatenazione")
                .doesNotContain("UPDATE Utente SET Stato = '\" +");
    }

    @Test
    @DisplayName("REGRESSION: UserDAO.doUpdateAdmin usa PreparedStatement")
    void testUserDAOUpdateAdminUsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/UserDAO.java");

        assertThat(source)
                .as("UserDAO.doUpdateAdmin non deve usare concatenazione")
                .doesNotContain("UPDATE Utente SET Amministratore = 'true' WHERE Id_Utente = \" +");
    }

    @Test
    @DisplayName("REGRESSION: ProductDAO.doUpdate usa PreparedStatement (SQL Injection fixata)")
    void testProductDAOUpdateUsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/ProductDAO.java");

        assertThat(source)
                .as("ProductDAO.doUpdate NON deve usare con.createStatement() (SQL Injection)")
                .doesNotContain("con.createStatement()")
                .contains("PreparedStatement")
                .contains("UPDATE Prodotto SET Nome = ?");
    }

    // ==========================================================
    // 3. Verifica schema SQL - nessuna password in chiaro
    // ==========================================================

    @Test
    @DisplayName("Schema DB: la colonna Accesso (password) e VARCHAR(40)")
    void testSchemaPasswordColumnIsHashSized() throws Exception {
        String schema = readSource("database/createDB.sql");

        assertThat(schema)
                .as("La colonna Accesso in Utente deve essere VARCHAR(40) "
                        + "per contenere un hash SHA-1 hex")
                .containsPattern("Accesso\\s+VARCHAR\\(40\\)");
    }

    @Test
    @DisplayName("Schema DB: la tabella Utente non contiene password in chiaro")
    void testSchemaHasNoPlaintextPasswordColumn() throws Exception {
        String schema = readSource("database/createDB.sql");

        assertThat(schema.toLowerCase())
                .as("Lo schema non deve avere una colonna 'password' in chiaro")
                .doesNotContain("password varchar")
                .doesNotContain("passwd varchar");
    }

    @Test
    @DisplayName("Schema DB: la tabella Utente ha un vincolo UNIQUE su Email")
    void testSchemaEmailIsUnique() throws Exception {
        String schema = readSource("database/createDB.sql");

        assertThat(schema)
                .as("La colonna Email deve avere vincolo UNIQUE (impedisce duplicati)")
                .containsPattern("Email\\s+VARCHAR\\(20\\)\\s+UNIQUE");
    }

    @Test
    @DisplayName("Schema DB: le tabelle hanno chiavi primarie (AUTO_INCREMENT)")
    void testSchemaTablesHavePrimaryKeys() throws Exception {
        String schema = readSource("database/createDB.sql");

        assertThat(schema)
                .as("Utente deve avere ID_Utente AUTO_INCREMENT PRIMARY KEY")
                .contains("ID_Utente INT AUTO_INCREMENT PRIMARY KEY")
                .as("Prodotto deve avere ID_Prodotto AUTO_INCREMENT PRIMARY KEY")
                .contains("ID_Prodotto INT AUTO_INCREMENT PRIMARY KEY")
                .as("Ordine deve avere ID_Ordine AUTO_INCREMENT PRIMARY KEY")
                .contains("ID_Ordine INT AUTO_INCREMENT PRIMARY KEY");
    }

    // ==========================================================
    // 4. Verifica coerenza DAO - nomi colonne/tabelle
    // ==========================================================

    @Test
    @DisplayName("OrderDAO: nomi tabelle e colonne corrispondono allo schema")
    void testOrderDAOColumnsMatchSchema() throws Exception {
        String dao = readSource("src/main/java/Model/OrderDAO.java");
        String schema = readSource("database/createDB.sql");

        // Assertion su soggetti diversi: uso SoftAssertions per raggruppare
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dao).contains("FROM Ordine");
        softly.assertThat(schema).contains("CREATE TABLE Ordine");
        softly.assertThat(dao).contains("ID_Ordine");
        softly.assertThat(schema).contains("ID_Ordine");
        softly.assertThat(dao).contains("Utente");
        softly.assertThat(schema).contains("Utente INT REFERENCES");
        softly.assertAll();
    }

    @Test
    @DisplayName("OrderProductDAO: nomi tabelle e colonne corrispondono allo schema")
    void testOrderProductDAOColumnsMatchSchema() throws Exception {
        String dao = readSource("src/main/java/Model/OrderProductDAO.java");
        String schema = readSource("database/createDB.sql");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dao).contains("Ordine_Prodotto");
        softly.assertThat(schema).contains("CREATE TABLE Ordine_Prodotto");
        softly.assertThat(dao).contains("Prodotto");
        softly.assertThat(dao).contains("Ordine");
        softly.assertAll();
    }

    // ==========================================================
    // 5. Verifica integrita DAO vs Bean
    // ==========================================================

    @Test
    @DisplayName("OrderBean ha gli stessi campi delle colonne in OrderDAO")
    void testOrderBeanFieldsMatchDAOColumns() throws Exception {
        String bean = readSource("src/main/java/Model/OrderBean.java");
        String dao = readSource("src/main/java/Model/OrderDAO.java");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dao).contains("order.getUser()");
        softly.assertThat(bean).contains("public int getUser()");
        softly.assertThat(dao).contains("order.getTotal()");
        softly.assertThat(bean).contains("public double getTotal()");
        softly.assertThat(dao).contains("orderBean.setId(");
        softly.assertThat(bean).contains("public void setId(");
        softly.assertAll();
    }

    @Test
    @DisplayName("OrderProductBean ha gli stessi campi delle colonne in OrderProductDAO")
    void testOrderProductBeanFieldsMatchDAOColumns() throws Exception {
        String bean = readSource("src/main/java/Model/OrderProductBean.java");
        String dao = readSource("src/main/java/Model/OrderProductDAO.java");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dao).contains("orderProductBean.getProduct()");
        softly.assertThat(bean).contains("public int getProduct()");
        softly.assertThat(dao).contains("orderProductBean.getQuantity()");
        softly.assertThat(bean).contains("public int getQuantity()");
        softly.assertThat(dao).contains("orderProductBean.getPrice()");
        softly.assertThat(bean).contains("public double getPrice()");
        softly.assertAll();
    }

    // ==========================================================
    // 6. Verifica ConPool - pattern Singleton
    // ==========================================================

    @Test
    @DisplayName("ConPool: DataSource e dichiarato static (Singleton)")
    void testConPoolIsSingleton() throws Exception {
        String source = readSource("src/main/java/Model/ConPool.java");

        assertThat(source)
                .as("Il DataSource deve essere static per garantire il pattern Singleton")
                .contains("private static DataSource datasource");
    }

    @Test
    @DisplayName("ConPool: getConnection e dichiarato static")
    void testConPoolGetConnectionIsStatic() throws Exception {
        String source = readSource("src/main/java/Model/ConPool.java");

        assertThat(source)
                .as("getConnection deve essere static per essere chiamato senza istanziare ConPool")
                .contains("public static Connection getConnection()");
    }

    @Test
    @DisplayName("ConPool: configurazione di sicurezza (useSSL, serverTimezone)")
    void testConPoolUrlHasSecurityParams() throws Exception {
        String source = readSource("src/main/java/Model/ConPool.java");

        assertThat(source)
                .as("La URL JDBC deve specificare serverTimezone e useSSL/allowPublicKeyRetrieval")
                .contains("serverTimezone")
                .containsPattern("useSSL|allowPublicKeyRetrieval");
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}