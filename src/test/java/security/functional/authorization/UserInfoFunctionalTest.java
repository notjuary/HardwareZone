package security.functional.authorization;

import Controller.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per UserInfo (area admin).
 */
@DisplayName("UserInfo - Test funzionale di sicurezza")
class UserInfoFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('User','Test','1995-01-01','user@test.com','hash','3339999999','NA','NA','80100','Via 2','2024-01-02','true','false')");

        when(support.request.getRequestDispatcher("WEB-INF/results/userinfo.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: UserInfo con admin mostra il profilo utente")
    void testAdminVedeProfilo() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("1");

        UserInfo servlet = new UserInfo();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("WEB-INF/results/userinfo.jsp");
    }

    @Test
    @DisplayName("SECURITY: UserInfo con utente normale reindirizza a index")
    void testUtenteNormale() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        UserInfo servlet = new UserInfo();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
    }

    @Test
    @DisplayName("SECURITY: UserInfo con utente null causa NPE (finding)")
    void testUtenteNull() {
        when(support.session.getAttribute("user")).thenReturn(null);

        UserInfo servlet = new UserInfo();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso");
    }
}