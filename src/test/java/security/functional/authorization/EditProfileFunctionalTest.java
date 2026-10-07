package security.functional.authorization;

import Controller.EditProfile;
import Model.UserBean;
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
 * Test funzionali di sicurezza per EditProfile.
 * Verifica: modifica profilo con dati validi, rifiuto input invalidi, NPE su utente null.
 * Riferimento: OWASP Testing Guide - OTG-AUTHZ, OWASP A04, GDPR
 */
@DisplayName("EditProfile - Test funzionale di sicurezza")
class EditProfileFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        // Utente di test nel DB
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','hash','3331234567','Salerno','SA','84100','Via Roma 1','2024-01-01','true','false')");

        // Dispatcher
        when(support.request.getRequestDispatcher("WEB-INF/admin/profile-admin.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("WEB-INF/user/profile-user.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    private void setupValidProfileParameters() {
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

    // ==========================================================
    // 1. Modifica profilo con dati validi
    // ==========================================================

    @Test
    @DisplayName("SECURITY: EditProfile con dati validi aggiorna il profilo")
    void testEditProfileValido() throws Exception {
        UserBean user = support.createNormalUser();
        user.setId(1);
        when(support.session.getAttribute("user")).thenReturn(user);
        setupValidProfileParameters();

        EditProfile servlet = new EditProfile();
        support.invokeDoPost(servlet, support.request, support.response);

        // Il servlet passa l'utente aggiornato alla JSP
        verify(support.request).setAttribute(eq("profileJSP"), any());
    }

    // ==========================================================
    // 2. Modifica rifiutata con input invalido
    // ==========================================================

    @Test
    @DisplayName("SECURITY: EditProfile con email invalida non aggiorna il profilo")
    void testEditProfileInvalido() throws Exception {
        UserBean user = support.createNormalUser();
        user.setId(1);
        when(support.session.getAttribute("user")).thenReturn(user);
        setupValidProfileParameters();

        // Email malformata
        when(support.request.getParameter("email")).thenReturn("invalid-email");

        EditProfile servlet = new EditProfile();
        support.invokeDoPost(servlet, support.request, support.response);

        // Il servlet reindirizza a error.jsp (validazione fallita)
        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    // ==========================================================
    // 3. NPE su utente null
    // ==========================================================

    @Test
    @DisplayName("SECURITY: EditProfile con utente null causa NPE (finding documentato)")
    void testEditProfileUtenteNull() {
        when(support.session.getAttribute("user")).thenReturn(null);
        setupValidProfileParameters();

        EditProfile servlet = new EditProfile();

        // NPE atteso: user.setId(user.getId()) senza null check
        assertThrows(Exception.class, () ->
                        support.invokeDoPost(servlet, support.request, support.response),
                "NPE atteso per utente null senza check");
    }
}