package security.functional.authorization;

import Controller.OrderInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per OrderInfo.
 * Verifica: admin vede dettagli di qualsiasi ordine, utente vede solo i propri.
 * Documenta il BUG LOGICO dell'else dentro il for.
 */
@DisplayName("OrderInfo - Test funzionale di sicurezza")
class OrderInfoFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        // Admin (id 1)
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Admin','Test','1990-01-01','admin@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','true')");
        // Utente normale (id 2)
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('User','Test','1995-01-01','user@test.com','hash','3339999999','NA','NA','80100','Via 2','2024-01-02','true','false')");

        // Ordini utente 2
        executeSql("INSERT INTO Ordine (Utente, Totale) VALUES (2, 100.00)");
        executeSql("INSERT INTO Ordine (Utente, Totale) VALUES (2, 200.00)");

        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");
        executeSql("INSERT INTO Ordine_Prodotto (Prodotto, Quantità, Prezzo, Ordine) VALUES (1, 1, 100.00, 1)");

        when(support.request.getRequestDispatcher("/WEB-INF/results/orderInfo.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/user/orderInfo.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: admin vede dettagli di qualsiasi ordine")
    void testAdminVedeOrdine() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("1");

        OrderInfo servlet = new OrderInfo();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("products"), any());
        verify(support.request).setAttribute(eq("catalog"), any());
        verify(support.request).setAttribute(eq("userOrder"), any());
    }

    @Test
    @DisplayName("SECURITY: utente normale vede solo i propri ordini")
    void testUtenteVedePropriOrdini() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.request.getParameter("id")).thenReturn("1");

        OrderInfo servlet = new OrderInfo();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/user/orderInfo.jsp");
    }

    @Test
    @DisplayName("SECURITY: utente anonimo causa NPE (finding documentato)")
    void testUtenteAnonimo()  {
        when(support.session.getAttribute("user")).thenReturn(null);
        when(support.request.getParameter("id")).thenReturn("1");

        OrderInfo servlet = new OrderInfo();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "Atteso NPE: user.isAdmin() senza null check");

    }
}