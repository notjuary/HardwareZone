package security.functional.daointegration;

import Model.PaymentBean;
import Model.PaymentDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per PaymentDAO con H2.
 */
@DisplayName("PaymentDAO - Test funzionale DAO")
class PaymentDAOFunctionalTest extends BaseFunctionalTest {

    private PaymentDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        dao = new PaymentDAO();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','false')");
        executeSql("INSERT INTO Ordine (Utente, Totale) VALUES (1, 299.99)");
    }

    @Test
    @DisplayName("doSave inserisce un pagamento")
    void testDoSave() {
        PaymentBean payment = new PaymentBean();
        payment.setOrder(1);
        payment.setDatePayment("2024-10-05");
        payment.setCardNumber("1234567890123456");
        payment.setCVV("123");
        payment.setDeadline("2030-12-31");
        payment.setHolder("Mario Rossi");

        dao.doSave(payment);

        // Verifica tramite query diretta H2
        try {
            java.sql.Connection con = java.sql.DriverManager.getConnection(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Pagamento");
            rs.next();
            assertThat(rs.getInt(1)).isEqualTo(1);
            con.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("SECURITY: numero carta viene salvato in chiaro (finding PCI-DSS)")
    void testNumeroCartaInChiaro() {
        PaymentBean payment = new PaymentBean();
        payment.setOrder(1);
        payment.setDatePayment("2024-10-05");
        payment.setCardNumber("1234567890123456");
        payment.setCVV("123");
        payment.setDeadline("2030-12-31");
        payment.setHolder("Mario Rossi");

        dao.doSave(payment);

        // Verifica che il numero sia in chiaro (finding documentato)
        try {
            java.sql.Connection con = java.sql.DriverManager.getConnection(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery("SELECT Numero_Carta FROM Pagamento WHERE Ordine=1");
            rs.next();
            assertThat(rs.getString(1))
                    .as("FINDING PCI-DSS: numero carta in chiaro")
                    .isEqualTo("1234567890123456");
            con.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}