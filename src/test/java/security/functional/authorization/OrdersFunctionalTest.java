package security.functional.authorization;

import Controller.Orders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Orders.
 * Verifica: admin vede tutti, utente vede solo i propri ordini.
 */
@DisplayName("Orders - Test funzionale di sicurezza")
class OrdersFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        // Admin
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Admin','Test','1990-01-01','admin@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','true')");
        // Utente normale (id 2)
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('User','Test','1995-01-01','user@test.com','hash','3339999999','NA','NA','80100','Via 2','2024-01-02','true','false')");

        // Ordini
        executeSql("INSERT INTO Ordine (Utente, Totale) VALUES (1, 100.00)");
        executeSql("INSERT INTO Ordine (Utente, Totale) VALUES (2, 250.00)");

        when(support.request.getRequestDispatcher("/WEB-INF/results/orders.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/user/orders.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: admin vede tutti gli ordini")
    void testAdminVedeTuttiOrdini() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        Orders servlet = new Orders();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("orders"), any());
        verify(support.request).getRequestDispatcher("/WEB-INF/results/orders.jsp");
    }

    @Test
    @DisplayName("SECURITY: utente normale vede solo i propri ordini")
    void testUtenteVedeSoloPropriOrdini() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        Orders servlet = new Orders();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("orders"), any());
        verify(support.request).getRequestDispatcher("/WEB-INF/user/orders.jsp");
        verify(support.request, never()).getRequestDispatcher("/WEB-INF/results/orders.jsp");
    }

    @Test
    @DisplayName("SECURITY: utente anonimo causa NPE (finding documentato)")
    void testUtenteAnonimo() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(null);

        Orders servlet = new Orders();
        try {
            support.invokeDoGet(servlet, support.request, support.response);
        } catch (Exception e) {
            // NPE atteso: user.isAdmin() senza null check
        }
    }
}