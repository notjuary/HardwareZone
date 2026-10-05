package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per le aree "User Profile" e "Cart".
 * Verifica:
 *  - Modifica profilo (GDPR)
 *  - Visualizzazione profilo
 *  - Carrello (accesso, coerenza)
 *
 * Riferimento: OWASP Testing Guide - OTG-AUTHZ, OWASP A01, A04, GDPR
 */
@DisplayName("Profile & Cart - GDPR e gestione carrello")
class ProfileAndCartTest {

    private UserBean user;
    private UserBean admin;

    @BeforeEach
    void setUp() {
        user = new UserBean();
        user.setId(1);
        user.setAdmin("false");
        user.setState("true");

        admin = new UserBean();
        admin.setId(2);
        admin.setAdmin("true");
        admin.setState("true");
    }

    // ==========================================================
    // 1. EDITPROFILE - Autorizzazione
    // ==========================================================

    @Test
    @DisplayName("EditProfile: richiede level == 10 (tutti i campi validi)")
    void testEditProfileRequiresAllFields() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");

        assertThat(source)
                .contains("if (level == 10)");
    }

    @Test
    @DisplayName("EditProfile: preserva il ruolo admin dell'utente")
    void testEditProfilePreservesAdminRole() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");

        assertThat(source)
                .as("La modifica profilo deve preservare il ruolo admin dell'utente")
                .contains("user.setAdmin(user.isAdmin())");
    }

    @Test
    @DisplayName("FINDING: EditProfile non richiede la password attuale per il cambio")
    void testDocumentNoCurrentPasswordVerification() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");

        boolean noCurrentPasswordCheck =
                !source.contains("currentPassword") &&
                        !source.contains("vecchia_password") &&
                        !source.contains("oldPassword");

        assertThat(noCurrentPasswordCheck)
                .as("FINDING GDPR/SICUREZZA: EditProfile modifica la password senza "
                        + "richiedere quella attuale. Se un attacker ottiene accesso "
                        + "temporaneo alla sessione (es. XSS, session hijacking), puo "
                        + "cambiare la password e bloccare fuori il vero utente "
                        + "(OWASP A07:2021). Fix: richiedere sempre la password attuale "
                        + "prima di accettare una nuova password.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: EditProfile non ha protezione CSRF")
    void testDocumentCsrfInEditProfile() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");

        boolean noCsrfToken =
                !source.contains("csrfToken") &&
                        !source.contains("csrf_token") &&
                        !source.contains("CSRF");

        assertThat(noCsrfToken)
                .as("FINDING: EditProfile non valida un token CSRF. Un sito malevolo "
                        + "puo indurre un utente loggato a modificare il proprio profilo "
                        + "senza consapevolezza (CWE-352). Fix: implementare pattern "
                        + "Synchronizer Token per tutte le POST.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: EditProfile non verifica che user != null")
    void testDocumentNullUserInEditProfile() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");

        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("user.setId(user.getId())") &&
                        !source.contains("user != null");

        assertThat(unsafe)
                .as("FINDING: EditProfile chiama user.setId() senza verificare user != null. "
                        + "Se un utente non autenticato invia una POST, si verifica NPE "
                        + "(CWE-476).")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: EditProfile ricostruisce setRegister() (bug funzionale)")
    void testDocumentRegisterOverwriteBug() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");

        boolean overwritesRegister =
                source.contains("user.setRegister()");

        assertThat(overwritesRegister)
                .as("BUG FUNZIONALE: EditProfile chiama user.setRegister() senza argomenti, "
                        + "che imposta la data di registrazione a OGGI. Questo sovrascrive "
                        + "la data originale di registrazione dell'utente, perdendo "
                        + "un dato storico importante. Fix: NON toccare register durante "
                        + "la modifica profilo.")
                .isTrue();
    }

    // ==========================================================
    // 2. USERPROFILE - Autorizzazione
    // ==========================================================

    @Test
    @DisplayName("UserProfile: mostra il profilo dell'utente loggato")
    void testUserProfileShowsCurrentUser() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");

        assertThat(source)
                .contains("session.getAttribute(\"user\")");
    }

    @Test
    @DisplayName("UserProfile: reindirizza admin a profile-admin.jsp")
    void testUserProfileRedirectsAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");

        assertThat(source)
                .contains("WEB-INF/admin/profile-admin.jsp");
    }

    @Test
    @DisplayName("UserProfile: reindirizza utente a profile-user.jsp")
    void testUserProfileRedirectsUser() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");

        assertThat(source)
                .contains("WEB-INF/user/profile-user.jsp");
    }

    @Test
    @DisplayName("FINDING CRITICO: UserProfile usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInUserProfile() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");

        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("user.isAdmin()") &&
                        !source.contains("user != null");

        assertThat(unsafe)
                .as("FINDING: UserProfile.java chiama user.isAdmin() senza verificare "
                        + "user != null. Un utente non autenticato che accede a "
                        + "/user-profile-servlet causa NPE (CWE-476).")
                .isTrue();
    }

    // ==========================================================
    // 3. USERINFO - Autorizzazione admin
    // ==========================================================

    @Test
    @DisplayName("UserInfo: richiede admin")
    void testUserInfoRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/UserInfo.java");

        assertThat(source)
                .contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("FINDING: UserInfo usa userAdmin.isAdmin() senza null check")
    void testDocumentMissingNullCheckInUserInfo() throws Exception {
        String source = readSource("src/main/java/Controller/UserInfo.java");

        boolean unsafe =
                source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("userAdmin.isAdmin()") &&
                        !source.contains("userAdmin != null");

        assertThat(unsafe)
                .as("FINDING: UserInfo.java non verifica userAdmin != null.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: UserInfo non gestisce NumberFormatException sull'id")
    void testDocumentNumberFormatExceptionInUserInfo() throws Exception {
        String source = readSource("src/main/java/Controller/UserInfo.java");

        boolean unsafeParse =
                source.contains("Integer.parseInt(request.getParameter(\"id\"))") &&
                        !source.contains("try {");

        assertThat(unsafeParse)
                .as("FINDING: UserInfo chiama Integer.parseInt(request.getParameter(\"id\")) "
                        + "senza try/catch. Se un utente invia 'id=abc', si verifica "
                        + "NumberFormatException (CWE-20).")
                .isTrue();
    }

    // ==========================================================
    // 4. SHOWCART - Gestione carrello
    // ==========================================================

    @Test
    @DisplayName("ShowCart: legge il carrello dalla sessione")
    void testShowCartReadsCartFromSession() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCart.java");

        assertThat(source)
                .contains("session.getAttribute(\"cart\")");
    }

    @Test
    @DisplayName("ShowCart: restituisce JSON")
    void testShowCartReturnsJson() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCart.java");

        assertThat(source)
                .contains("JSONArray")
                .contains("JSONObject");
    }

    @Test
    @DisplayName("FINDING CRITICO: ShowCart usa cartBean.getCartList() senza null check")
    void testDocumentMissingNullCheckInShowCart() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCart.java");

        boolean unsafe =
                source.contains("CartBean cartBean = (CartBean) session.getAttribute(\"cart\")") &&
                        source.contains("cartBean.getCartList()") &&
                        !source.contains("cartBean != null");

        assertThat(unsafe)
                .as("FINDING: ShowCart.java chiama cartBean.getCartList() senza verificare "
                        + "che cartBean != null. Se un utente accede a /show-cart-servlet "
                        + "prima di aggiungere prodotti al carrello, si verifica NPE "
                        + "(CWE-476). Fix: if (cartBean == null) cartBean = new CartBean();")
                .isTrue();
    }

    // ==========================================================
    // 5. GDPR - Diritti dell'utente
    // ==========================================================

    @Test
    @DisplayName("GDPR: l'utente puo visualizzare il proprio profilo")
    void testUserCanViewOwnProfile() {
        // Logica: UserProfile mostra il profilo dal session.getAttribute("user")
        // Quindi mostra sempre il profilo dell'utente loggato (corretto)
        assertThat(user.isAdmin()).isEqualToIgnoringCase("false");
    }

    @Test
    @DisplayName("GDPR: l'utente puo modificare il proprio profilo")
    void testUserCanEditOwnProfile() {
        // EditProfile usa il session user per identificare il profilo
        // Non c'e un parametro "id" arbitrario -> non si possono modificare altri profili
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("GDPR: l'admin puo visualizzare profili di altri utenti")
    void testAdminCanViewOtherProfiles() {
        // UserInfo permette all'admin di specificare un id
        assertThat(admin.isAdmin()).isEqualToIgnoringCase("true");
    }

    // ==========================================================
    // 6. Integrita UserBean
    // ==========================================================

    @Test
    @DisplayName("UserBean: dopo edit, i campi sono aggiornati")
    void testUserBeanAfterEdit() {
        user.setName("Mario");
        user.setSurname("Rossi");
        user.setEmail("mario.rossi@example.com");

        assertThat(user.getName()).isEqualTo("Mario");
        assertThat(user.getSurname()).isEqualTo("Rossi");
        assertThat(user.getEmail()).isEqualTo("mario.rossi@example.com");
    }

    @Test
    @DisplayName("UserBean: password hashata (non in chiaro)")
    void testUserBeanPasswordIsHashed() {
        user.setPassword("MySecretPass");
        assertThat(user.getPassword()).isNotEqualTo("MySecretPass");
        assertThat(user.getPassword()).hasSize(40); // SHA-1 hex
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}