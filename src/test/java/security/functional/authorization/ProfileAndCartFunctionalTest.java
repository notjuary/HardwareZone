package security.functional.authorization;

import Controller.EditProfile;
import Controller.ShowCart;
import Controller.UserInfo;
import Controller.UserProfile;
import Model.CartBean;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per le aree "User Profile" e "Cart".
 * Verifica:
 *  - visualizzazione profilo (admin/user/anonimo)
 *  - modifica profilo
 *  - visualizzazione carrello
 *  - vista utente admin
 * Riferimento: OWASP Testing Guide - OTG-AUTHZ, OWASP A01, A04, GDPR
 */
@DisplayName("Profile & Cart - Test funzionale di sicurezza")
class ProfileAndCartFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        // Inserisci utente nel DB
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','hash','3331234567','Salerno','SA','84100','Via Roma 1','2024-01-01','true','false')");

        // Dispatcher comuni
        when(support.request.getRequestDispatcher("WEB-INF/admin/profile-admin.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("WEB-INF/user/profile-user.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("WEB-INF/results/userinfo.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
    }

    // ==========================================================
    // 1. USERPROFILE — Visualizzazione profilo
    // ==========================================================

    @Test
    @DisplayName("SECURITY: UserProfile con admin mostra profile-admin.jsp")
    void testUserProfileAdmin() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        UserProfile servlet = new UserProfile();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("WEB-INF/admin/profile-admin.jsp");
        verify(support.dispatcher).include(support.request, support.response);
    }

    @Test
    @DisplayName("SECURITY: UserProfile con utente normale mostra profile-user.jsp")
    void testUserProfileUtenteNormale() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        UserProfile servlet = new UserProfile();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("WEB-INF/user/profile-user.jsp");
        verify(support.dispatcher).include(support.request, support.response);
    }

    @Test
    @DisplayName("SECURITY: UserProfile con utente null causa NPE (finding documentato)")
    void testUserProfileUtenteNull() {
        when(support.session.getAttribute("user")).thenReturn(null);

        UserProfile servlet = new UserProfile();

        // NPE atteso: user.isAdmin() senza null check
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso: user null senza check");
    }

    // ==========================================================
    // 2. EDITPROFILE — Modifica profilo
    // ==========================================================

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

    @Test
    @DisplayName("SECURITY: EditProfile con dati validi aggiorna il profilo")
    void testEditProfileValido() throws Exception {
        UserBean user = support.createNormalUser();
        user.setId(1);
        when(support.session.getAttribute("user")).thenReturn(user);
        setupValidProfileParameters();

        EditProfile servlet = new EditProfile();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("profileJSP"), any());
    }

    @Test
    @DisplayName("SECURITY: EditProfile con email invalida non aggiorna")
    void testEditProfileEmailInvalida() throws Exception {
        UserBean user = support.createNormalUser();
        user.setId(1);
        when(support.session.getAttribute("user")).thenReturn(user);
        setupValidProfileParameters();
        when(support.request.getParameter("email")).thenReturn("invalid-email");

        EditProfile servlet = new EditProfile();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    @Test
    @DisplayName("SECURITY: EditProfile con nome vuoto non aggiorna")
    void testEditProfileNomeVuoto() throws Exception {
        UserBean user = support.createNormalUser();
        user.setId(1);
        when(support.session.getAttribute("user")).thenReturn(user);
        setupValidProfileParameters();
        when(support.request.getParameter("name")).thenReturn("");

        EditProfile servlet = new EditProfile();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    @Test
    @DisplayName("SECURITY: EditProfile con utente null causa NPE (finding documentato)")
    void testEditProfileUtenteNull() {
        when(support.session.getAttribute("user")).thenReturn(null);
        setupValidProfileParameters();

        EditProfile servlet = new EditProfile();

        // NPE atteso: user.setId(user.getId()) senza check
        assertThrows(Exception.class, () ->
                        support.invokeDoPost(servlet, support.request, support.response),
                "NPE atteso: user null senza check");
    }

    // ==========================================================
    // 3. SHOWCART — Visualizzazione carrello
    // ==========================================================

    @Test
    @DisplayName("SECURITY: ShowCart con carrello valido restituisce JSON")
    void testShowCartCarrelloValido() throws Exception {
        CartBean cart = new CartBean();
        cart.addProduct(1, 2);
        when(support.session.getAttribute("cart")).thenReturn(cart);

        // Inserisci prodotto nel DB per il lookup
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(support.response.getWriter()).thenReturn(pw);

        ShowCart servlet = new ShowCart();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.response).setContentType("text/html");
        verify(support.response).getWriter();
    }

    @Test
    @DisplayName("SECURITY: ShowCart con carrello null causa NPE (finding documentato)")
    void testShowCartCarrelloNull() {
        when(support.session.getAttribute("cart")).thenReturn(null);

        ShowCart servlet = new ShowCart();

        // NPE atteso: cartBean.getCartList() senza null check
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso: carrello null senza check");
    }

    // ==========================================================
    // 4. USERINFO — Vista admin di altri utenti
    // ==========================================================

    @Test
    @DisplayName("SECURITY: UserInfo con admin mostra il profilo utente")
    void testUserInfoAdmin() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("1");

        UserInfo servlet = new UserInfo();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("WEB-INF/results/userinfo.jsp");
        verify(support.request).setAttribute(eq("profileJSP"), any());
    }

    @Test
    @DisplayName("SECURITY: UserInfo con utente normale reindirizza a index.jsp")
    void testUserInfoUtenteNormale() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        UserInfo servlet = new UserInfo();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
        verify(support.dispatcher).include(support.request, support.response);
    }

    @Test
    @DisplayName("SECURITY: UserInfo con utente null causa NPE (finding documentato)")
    void testUserInfoUtenteNull() {
        when(support.session.getAttribute("user")).thenReturn(null);

        UserInfo servlet = new UserInfo();

        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso: userAdmin null senza check");
    }

    @Test
    @DisplayName("SECURITY: UserInfo con id non numerico causa NumberFormatException (finding)")
    void testUserInfoIdNonNumerico() {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("abc");

        UserInfo servlet = new UserInfo();

        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NumberFormatException atteso: id non numerico");
    }
}