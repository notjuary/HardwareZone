package security.functional.inputvalidation;

import Controller.EditProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per EditProfile.
 */
@DisplayName("EditProfile - Test funzionale di sicurezza")
class EditProfileFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','cbfdac6008f9cab4083784cbd1874f76618d2a97','3331234567','Salerno','SA','84100','Via Roma 1','2024-01-01','true','false')");

        when(support.request.getRequestDispatcher("WEB-INF/user/profile-user.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    private void setupValidProfile() {
        when(support.request.getParameter("name")).thenReturn("Mario");
        when(support.request.getParameter("surname")).thenReturn("Rossi");
        when(support.request.getParameter("email")).thenReturn("mario@test.com");
        when(support.request.getParameter("password")).thenReturn("newpassword123");
        when(support.request.getParameter("phone")).thenReturn("3331234567");
        when(support.request.getParameter("city")).thenReturn("Salerno");
        when(support.request.getParameter("province")).thenReturn("SA");
        when(support.request.getParameter("postalCode")).thenReturn("84100");
        when(support.request.getParameter("address")).thenReturn("Via Roma 1");
        when(support.request.getParameter("birthday")).thenReturn("1990-01-01");
    }

    @Test
    @DisplayName("SECURITY: edit profile con dati validi aggiorna l'utente")
    void testEditProfileValido() throws Exception {
        Model.UserBean user = support.createNormalUser();
        user.setId(1);
        when(support.session.getAttribute("user")).thenReturn(user);
        setupValidProfile();

        EditProfile servlet = new EditProfile();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("profileJSP"), any());
    }

    @Test
    @DisplayName("SECURITY: edit profile con campi invalidi non aggiorna")
    void testEditProfileInvalido() throws Exception {
        Model.UserBean user = support.createNormalUser();
        user.setId(1);
        when(support.session.getAttribute("user")).thenReturn(user);
        setupValidProfile();
        when(support.request.getParameter("email")).thenReturn("invalid");

        EditProfile servlet = new EditProfile();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    @Test
    @DisplayName("SECURITY: edit profile con utente null causa NPE (finding)")
    void testEditProfileUtenteNull() {
        when(support.session.getAttribute("user")).thenReturn(null);
        setupValidProfile();

        EditProfile servlet = new EditProfile();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso");
    }
}