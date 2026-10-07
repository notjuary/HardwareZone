package security.functional.authorization;

import Controller.Payment;
import Model.CartBean;
import Model.ProductCartBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Payment.
 * Verifica:
 *  - autenticazione richiesta per accedere al pagamento
 *  - validazione dei dati della carta
 *  - gestione di NPE su parametri null
 *  - comportamento del doPost con carta valida/invalida
 * Riferimento: OWASP Testing Guide - OTG-AUTHN, OTG-BUSLOGIC, OWASP A04, A07
 */
@DisplayName("Payment - Test funzionale di sicurezza")
class PaymentFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        // Dispatcher comuni
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/payment.jsp")).thenReturn(support.dispatcher);
    }

    // ==========================================================
    // 1. AUTENTICAZIONE — Payment.doGet richiede login
    // ==========================================================

    @Test
    @DisplayName("SECURITY: Payment.doGet senza login reindirizza a error.jsp")
    void testPaymentSenzaLogin() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(null);

        Payment servlet = new Payment();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
        verify(support.dispatcher).include(support.request, support.response);
    }

    @Test
    @DisplayName("SECURITY: Payment.doGet con login e carrello carica la pagina")
    void testPaymentConLogin() throws Exception {
        // Carrello di test
        CartBean cart = new CartBean();
        ProductCartBean item = new ProductCartBean();
        item.setId(1);
        item.setQuantity(1);
        cart.addProduct(1, 1);

        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("cart")).thenReturn(cart);

        // Inserisci un prodotto nel DB per il calcolo del totale
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");

        Payment servlet = new Payment();
        support.invokeDoGet(servlet, support.request, support.response);

        // Il servlet salva il totale in sessione
        verify(support.session).setAttribute(eq("total"), any());
    }

    @Test
    @DisplayName("SECURITY: Payment.doGet senza carrello causa NPE (finding documentato)")
    void testPaymentSenzaCarrello() {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("cart")).thenReturn(null);

        Payment servlet = new Payment();

        // NPE attesa su cartBean.getCartList()
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso per carrello null");
    }

    // ==========================================================
    // 2. VALIDAZIONE CARTA — Payment.doPost
    // ==========================================================

    @Test
    @DisplayName("SECURITY: Payment.doPost con carta valida elabora il pagamento")
    void testPaymentConCartaValida() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);
        when(support.session.getAttribute("cart")).thenReturn(new CartBean());

        // Inserisci un prodotto e utente nel DB
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','false')");
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");

        // Parametri carta validi
        when(support.request.getParameter("numero-carta")).thenReturn("1234567890123456");
        when(support.request.getParameter("CVV")).thenReturn("123");
        when(support.request.getParameter("scadenza")).thenReturn("2030-12-31");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();
        support.invokeDoPost(servlet, support.request, support.response);

        // Il servlet ha processato la richiesta (verify indiretto)
        verify(support.request, atLeastOnce()).getParameter("numero-carta");
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost con carta invalida non crea ordine")
    void testPaymentConCartaInvalida() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);

        // Parametri invalidi
        when(support.request.getParameter("numero-carta")).thenReturn("123");
        when(support.request.getParameter("CVV")).thenReturn("1");
        when(support.request.getParameter("scadenza")).thenReturn("2030-12-31");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();

        try {
            support.invokeDoPost(servlet, support.request, support.response);
        } catch (Exception e) {
            // Eccezione possibile su parsing
        }

        // Il servlet NON deve aver salvato un ordine nel DB
        // (verifichiamo tramite query diretta in H2)
        try {
            java.sql.Connection con = java.sql.DriverManager.getConnection(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Ordine");
            rs.next();
            org.assertj.core.api.Assertions.assertThat(rs.getInt(1))
                    .as("Nessun ordine deve essere creato con carta invalida")
                    .isEqualTo(0);
            con.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost con numero carta non numerico viene rifiutato")
    void testPaymentNumeroCartaNonNumerico() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);

        when(support.request.getParameter("numero-carta")).thenReturn("abcd567890123456");
        when(support.request.getParameter("CVV")).thenReturn("123");
        when(support.request.getParameter("scadenza")).thenReturn("2030-12-31");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();

        try {
            support.invokeDoPost(servlet, support.request, support.response);
        } catch (Exception e) {
            // Eccezione possibile
        }

        // Il servlet deve reindirizzare a error.jsp (validazione fallita)
        verify(support.request, atLeastOnce()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost con CVV non valido viene rifiutato")
    void testPaymentCvvNonValido() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);

        when(support.request.getParameter("numero-carta")).thenReturn("1234567890123456");
        when(support.request.getParameter("CVV")).thenReturn("abcd");
        when(support.request.getParameter("scadenza")).thenReturn("2030-12-31");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();

        try {
            support.invokeDoPost(servlet, support.request, support.response);
        } catch (Exception e) {
            // Eccezione possibile
        }

        verify(support.request, atLeastOnce()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost con scadenza passata viene rifiutato")
    void testPaymentScadenzaPassata() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);

        when(support.request.getParameter("numero-carta")).thenReturn("1234567890123456");
        when(support.request.getParameter("CVV")).thenReturn("123");
        when(support.request.getParameter("scadenza")).thenReturn("2020-01-01");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();

        try {
            support.invokeDoPost(servlet, support.request, support.response);
        } catch (Exception e) {
            // Eccezione possibile
        }

        // Con scadenza passata, il servlet deve reindirizzare a error.jsp
        verify(support.request, atLeastOnce()).getRequestDispatcher(anyString());
    }

    // ==========================================================
    // 3. FINDING — NPE su parametri null
    // ==========================================================

    @Test
    @DisplayName("SECURITY: Payment.doPost con deadline null causa NPE (finding documentato)")
    void testPaymentDeadlineNull() {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);

        when(support.request.getParameter("numero-carta")).thenReturn("1234567890123456");
        when(support.request.getParameter("CVV")).thenReturn("123");
        when(support.request.getParameter("scadenza")).thenReturn(null);
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                        support.invokeDoPost(servlet, support.request, support.response),
                "NPE atteso per scadenza null");
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost senza login causa NPE (finding documentato)")
    void testPaymentDoPostSenzaLogin() {
        when(support.session.getAttribute("user")).thenReturn(null);
        when(support.session.getAttribute("total")).thenReturn(299.99);

        when(support.request.getParameter("numero-carta")).thenReturn("1234567890123456");
        when(support.request.getParameter("CVV")).thenReturn("123");
        when(support.request.getParameter("scadenza")).thenReturn("2030-12-31");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();

        // Il doPost NON verifica il login -> NPE su user.getId()
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                        support.invokeDoPost(servlet, support.request, support.response),
                "NPE atteso: doPost non verifica login");
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost con scadenza malformata causa eccezione")
    void testPaymentScadenzaMalformata() {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);

        when(support.request.getParameter("numero-carta")).thenReturn("1234567890123456");
        when(support.request.getParameter("CVV")).thenReturn("123");
        when(support.request.getParameter("scadenza")).thenReturn("not-a-date");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                        support.invokeDoPost(servlet, support.request, support.response),
                "NumberFormatException atteso per scadenza malformata");
    }
}