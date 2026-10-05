package security.functional;

import Model.ConPool;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Classe base per tutti i test funzionali.
 * Configura H2 in-memory con lo schema del progetto.
 */
public abstract class BaseFunctionalTest {

    private static DataSource h2DataSource;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        PoolProperties p = new PoolProperties();
        p.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        p.setDriverClassName("org.h2.Driver");
        p.setUsername("sa");
        p.setPassword("");
        p.setMaxActive(5);
        p.setInitialSize(1);
        p.setMinIdle(1);
        p.setMaxIdle(5);

        h2DataSource = new DataSource();
        h2DataSource.setPoolProperties(p);

        createSchema();

        ConPool.setTestDataSource(h2DataSource);
    }

    @AfterAll
    static void tearDownDatabase() {
        ConPool.clearTestDataSource();
    }

    /**
     * UDF (User Defined Function) registrata come SHA1 in H2.
     * Chiamata dall'alias SQL quando UserDAO usa SHA1(?).
     */
    public static String sha1(String input) throws Exception {
        if (input == null) return null;
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static void createSchema() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement st = con.createStatement()) {

            st.execute("DROP TABLE IF EXISTS Ordine_Prodotto");
            st.execute("DROP TABLE IF EXISTS Pagamento");
            st.execute("DROP TABLE IF EXISTS Carrello");
            st.execute("DROP TABLE IF EXISTS Ordine");
            st.execute("DROP TABLE IF EXISTS Prodotto");
            st.execute("DROP TABLE IF EXISTS Categoria");
            // Registra SHA1 come alias che chiama il metodo Java
            st.execute("CREATE ALIAS IF NOT EXISTS SHA1 FOR \"security.functional.BaseFunctionalTest.sha1\"");
            st.execute("DROP TABLE IF EXISTS Utente");

            st.execute("CREATE TABLE Utente (" +
                    "ID_Utente INT AUTO_INCREMENT PRIMARY KEY," +
                    "Nome VARCHAR(20) NOT NULL," +
                    "Cognome VARCHAR(20) NOT NULL," +
                    "Data_Nascita DATE," +
                    "Email VARCHAR(100) UNIQUE NOT NULL," +
                    "Accesso VARCHAR(40) NOT NULL," +
                    "Telefono CHAR(10)," +
                    "Citta VARCHAR(20)," +
                    "Provincia CHAR(2)," +
                    "Codice_Postale CHAR(5)," +
                    "Indirizzo VARCHAR(50)," +
                    "Data_Registrazione DATE," +
                    "Stato VARCHAR(5) DEFAULT 'true'," +
                    "Amministratore VARCHAR(5) DEFAULT 'false'" +
                    ")");

            st.execute("CREATE TABLE Prodotto (" +
                    "ID_Prodotto INT AUTO_INCREMENT PRIMARY KEY," +
                    "Nome VARCHAR(20) NOT NULL," +
                    "Descrizione VARCHAR(255) NOT NULL," +
                    "Prezzo DOUBLE NOT NULL," +
                    "Quantita_Disponibile INT NOT NULL DEFAULT 0," +
                    "Sconto INT NOT NULL DEFAULT 0," +
                    "Immagine VARCHAR(255) NOT NULL," +
                    "Categoria VARCHAR(20) NOT NULL" +
                    ")");

            st.execute("CREATE TABLE Ordine (" +
                    "ID_Ordine INT AUTO_INCREMENT PRIMARY KEY," +
                    "Utente INT," +
                    "Totale DOUBLE NOT NULL" +
                    ")");

            st.execute("CREATE TABLE Ordine_Prodotto (" +
                    "Prodotto INT NOT NULL," +
                    "Quantità INT NOT NULL," +
                    "Prezzo DOUBLE," +
                    "Ordine INT" +
                    ")");

            st.execute("CREATE TABLE Pagamento (" +
                    "ID_Pagamento INT AUTO_INCREMENT PRIMARY KEY," +
                    "Ordine INT," +
                    "Data_Pagamento DATE NOT NULL," +
                    "Numero_Carta CHAR(16) NOT NULL," +
                    "CVV CHAR(3) NOT NULL," +
                    "Scadenza DATE NOT NULL," +
                    "Titolare_Carta VARCHAR(60) NOT NULL" +
                    ")");

            st.execute("CREATE TABLE Carrello (" +
                    "Prodotto INT NOT NULL," +
                    "Quantità INT NOT NULL," +
                    "Utente INT NOT NULL" +
                    ")");

            st.execute("CREATE TABLE Categoria (" +
                    "Nome_Categoria VARCHAR(20) PRIMARY KEY" +
                    ")");
        }
    }

    protected void executeSql(String sql) throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    protected void cleanDatabase() throws Exception {
        executeSql("SET REFERENTIAL_INTEGRITY FALSE");
        executeSql("TRUNCATE TABLE Ordine_Prodotto RESTART IDENTITY");
        executeSql("TRUNCATE TABLE Pagamento RESTART IDENTITY");
        executeSql("TRUNCATE TABLE Carrello RESTART IDENTITY");
        executeSql("TRUNCATE TABLE Ordine RESTART IDENTITY");
        executeSql("TRUNCATE TABLE Prodotto RESTART IDENTITY");
        executeSql("TRUNCATE TABLE Categoria RESTART IDENTITY");
        executeSql("TRUNCATE TABLE Utente RESTART IDENTITY");
        executeSql("SET REFERENTIAL_INTEGRITY TRUE");
    }

    /**
     * UDF (User Defined Function) registrata come SHA1 in H2.
     * Chiamata dall'alias SQL quando UserDAO usa SHA1(?).
     */

}