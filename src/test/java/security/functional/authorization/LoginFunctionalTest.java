package security.functional.authorization;

import Controller.Login;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Login.
 * Verifica: autenticazione, redirect admin/user, account disabilitato.
 */
@DisplayName("Login - Test funzionale di sicurezza")
class LoginFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();


        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Admin','Test','1990-01-01','admin@test.com','cbfdac6008f9cab4083784cbd1874f76618d2a97','3331234567','SA','SA','84100','Via 1','2024-01-01','true','true')");
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('User','Test','1995-01-01','user@test.com','cbfdac6008f9cab4083784cbd1874f76618d2a97','3339999999','NA','NA','80100','Via 2','2024-01-02','true','false')");
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Disabled','Test','1995-01-01','disabled@test.com','cbfdac6008f9cab4083784cbd1874f76618d2a97','3338888888','NA','NA','80100','Via 3','2024-01-03','false','false')");

        when(support.request.getRequestDispatcher("WEB-INF/admin/admin.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: login admin crea la sessione e reindirizza a admin.jsp")
    void testLoginAdmin() throws Exception {
        when(support.request.getParameter("email")).thenReturn("admin@test.com");
        when(support.request.getParameter("password")).thenReturn("password123");

        Login servlet = new Login();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("user"), any());
    }

    @Test
    @DisplayName("SECURITY: login utente normale crea la sessione e reindirizza a index")
    void testLoginUtenteNormale() throws Exception {
        when(support.request.getParameter("email")).thenReturn("user@test.com");
        when(support.request.getParameter("password")).thenReturn("password123");

        Login servlet = new Login();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("user"), any());
    }

    @ParameterizedTest(name = "SECURITY: login fallito con email={0} e password={1} non crea sessione")
    @CsvSource({
            "user@test.com,       wrongpassword",
            "nonexistent@test.com, password123",
            "disabled@test.com,   password123"
    })
    @DisplayName("SECURITY: login fallito (password errata, email inesistente, account disabilitato)")
    void testLoginFallitoNonCreaSessione(String email, String password) throws Exception {
        when(support.request.getParameter("email")).thenReturn(email);
        when(support.request.getParameter("password")).thenReturn(password);

        Login servlet = new Login();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session, never()).setAttribute(eq("user"), any());
    }
}