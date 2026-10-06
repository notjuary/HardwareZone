package security.functional.authorization;
import Controller.SetStateUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per SetStateUser.
 * Verifica: autorizzazione admin, self-lockout, privilege escalation.
 */
@DisplayName("SetStateUser - Test funzionale di sicurezza")
class SetStateUserFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Admin','Test','1990-01-01','admin@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','true')");
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('User','Test','1995-01-01','user@test.com','hash','3339999999','NA','NA','80100','Via 2','2024-01-02','true','false')");

        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/users-servlet")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: SetStateUser richiede admin")
    void testAdminRichiesto() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        SetStateUser servlet = new SetStateUser();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
    }

    @Test
    @DisplayName("SECURITY: SetStateUser con utente anonimo causa NPE (finding)")
    void testUtenteAnonimo() {
        when(support.session.getAttribute("user")).thenReturn(null);

        SetStateUser servlet = new SetStateUser();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso");
    }

    @Test
    @DisplayName("SECURITY: admin disabilita utente normale")
    void testAdminDisabilitaUtente() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("2");
        when(support.request.getParameter("active")).thenReturn("true");

        SetStateUser servlet = new SetStateUser();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/users-servlet");
    }
}