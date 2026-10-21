package security.functional.authorization;

import Controller.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Users (area admin).
 */
@DisplayName("Users - Test funzionale di sicurezza")
class UsersFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    // Hash BCrypt valido per la password "password" (utile se in futuro testi il login)
    private final String bcryptHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Admin','Test','1990-01-01','admin@test.com','" + bcryptHash + "','3331234567','SA','SA','84100','Via 1','2024-01-01','true','true')");
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('User','Test','1995-01-01','user@test.com','" + bcryptHash + "','3339999999','NA','NA','80100','Via 2','2024-01-02','true','false')");

        when(support.request.getRequestDispatcher("/WEB-INF/results/users.jsp"))
                .thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp"))
                .thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: Users mostra tutti gli utenti solo ad admin")
    void testAdminVedeTuttiUtenti() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        Users servlet = new Users();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("users"), any());
        verify(support.request).getRequestDispatcher("/WEB-INF/results/users.jsp");
    }

    @Test
    @DisplayName("SECURITY: Users reindirizza utente normale a index.jsp")
    void testUtenteNormaleReindirizzato() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        Users servlet = new Users();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
        verify(support.request, never()).getRequestDispatcher("/WEB-INF/results/users.jsp");
    }

    @Test
    @DisplayName("FIXED: Users non causa NPE se utente non loggato")
    void testUtenteAnonimo() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(null);

        Users servlet = new Users();

        // Assicuriamoci che non lanci eccezioni (il fix va fatto anche in Users.java)
        assertDoesNotThrow(() ->
                support.invokeDoGet(servlet, support.request, support.response)
        );

        // Deve reindirizzare alla index
        verify(support.request).getRequestDispatcher("index.jsp");
    }
}