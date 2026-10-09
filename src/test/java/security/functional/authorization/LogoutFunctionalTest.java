package security.functional.authorization;

import Controller.Logout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Logout.
 */
@DisplayName("Logout - Test funzionale di sicurezza")
class LogoutFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('User','Test','1995-01-01','user@test.com','hash','3339999999','NA','NA','80100','Via 2','2024-01-02','true','false')");

        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: logout invalida la sessione")
    void testLogoutInvalidaSessione() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("cart")).thenReturn(new Model.CartBean());

        Logout servlet = new Logout();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.session).invalidate();
    }

    @Test
    @DisplayName("FIXED: Logout utente anonimo non lancia NPE")
    void testLogoutUtenteAnonimo() throws Exception {
        // Setup: sessione senza user
        when(support.session.getAttribute("user")).thenReturn(null);
        when(support.session.getAttribute("cart")).thenReturn(null);
        when(support.request.getSession(false)).thenReturn(support.session);

        Logout servlet = new Logout();

        // FIX: non deve più lanciare NPE
        assertDoesNotThrow(() ->
                support.invokeDoGet(servlet, support.request, support.response));

        // FIX: la sessione viene invalidata
        verify(support.session).invalidate();
    }
    @Test
    @DisplayName("SECURITY: logout reindirizza a index.jsp")
    void testLogoutRedirect() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("cart")).thenReturn(new Model.CartBean());

        Logout servlet = new Logout();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
    }
}