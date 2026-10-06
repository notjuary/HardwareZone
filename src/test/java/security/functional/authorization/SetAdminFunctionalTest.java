package security.functional.authorization;


import Controller.SetAdmin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per SetAdmin.
 * Verifica: autorizzazione admin, privilege escalation.
 */
@DisplayName("SetAdmin - Test funzionale di sicurezza")
class SetAdminFunctionalTest extends BaseFunctionalTest {

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

        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: SetAdmin richiede admin")
    void testAdminRichiesto() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        SetAdmin servlet = new SetAdmin();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
    }

    @Test
    @DisplayName("SECURITY: SetAdmin con utente non loggato causa NPE (finding)")
    void testUtenteAnonimo()  {
        when(support.session.getAttribute("user")).thenReturn(null);

        SetAdmin servlet = new SetAdmin();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso");
    }

    @Test
    @DisplayName("SECURITY: SetAdmin con admin promuove utente normale")
    void testAdminPromuoveUtente() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("2");

        SetAdmin servlet = new SetAdmin();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    @Test
    @DisplayName("SECURITY: SetAdmin non promuove chi è già admin")
    void testNonPromuoveChiGiaAdmin() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("1");  // è già admin

        SetAdmin servlet = new SetAdmin();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }
}