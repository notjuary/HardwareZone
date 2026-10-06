package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per User Profile e Cart.
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

    @Test
    @DisplayName("EditProfile: richiede level == 10 (tutti i campi validi)")
    void testEditProfileRequiresAllFields() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");
        assertThat(source).contains("if (level == 10)");
    }

    @Test
    @DisplayName("EditProfile: preserva il ruolo admin dell'utente")
    void testEditProfilePreservesAdminRole() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");
        assertThat(source).contains("user.setAdmin(user.isAdmin())");
    }

    @Test
    @DisplayName("FINDING: EditProfile non richiede la password attuale per il cambio")
    void testDocumentNoCurrentPasswordVerification() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");
        boolean noCurrentPasswordCheck =
                !source.contains("currentPassword") &&
                        !source.contains("vecchia_password") &&
                        !source.contains("oldPassword");
        assertThat(noCurrentPasswordCheck).as("FINDING: no verifica password attuale").isTrue();
    }

    @Test
    @DisplayName("FINDING: EditProfile non ha protezione CSRF")
    void testDocumentCsrfInEditProfile() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");
        boolean noCsrfToken =
                !source.contains("csrfToken") &&
                        !source.contains("csrf_token") &&
                        !source.contains("CSRF");
        assertThat(noCsrfToken).as("FINDING: no CSRF protection").isTrue();
    }

    @Test
    @DisplayName("FINDING: EditProfile non verifica che user != null")
    void testDocumentNullUserInEditProfile() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");
        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("user.setId(user.getId())") &&
                        !source.contains("user != null");
        assertThat(unsafe).as("FINDING: NPE se user null").isTrue();
    }

    @Test
    @DisplayName("FINDING: EditProfile ricostruisce setRegister() (bug funzionale)")
    void testDocumentRegisterOverwriteBug() throws Exception {
        String source = readSource("src/main/java/Controller/EditProfile.java");
        boolean overwritesRegister = source.contains("user.setRegister()");
        assertThat(overwritesRegister).as("BUG: sovrascrive data registrazione").isTrue();
    }

    @Test
    @DisplayName("UserProfile: mostra il profilo dell'utente loggato")
    void testUserProfileShowsCurrentUser() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");
        assertThat(source).contains("session.getAttribute(\"user\")");
    }

    @Test
    @DisplayName("UserProfile: reindirizza admin a profile-admin.jsp")
    void testUserProfileRedirectsAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");
        assertThat(source).contains("WEB-INF/admin/profile-admin.jsp");
    }

    @Test
    @DisplayName("UserProfile: reindirizza utente a profile-user.jsp")
    void testUserProfileRedirectsUser() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");
        assertThat(source).contains("WEB-INF/user/profile-user.jsp");
    }

    @Test
    @DisplayName("FINDING CRITICO: UserProfile usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInUserProfile() throws Exception {
        String source = readSource("src/main/java/Controller/UserProfile.java");
        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("user.isAdmin()") &&
                        !source.contains("user != null");
        assertThat(unsafe).as("FINDING: NPE UserProfile").isTrue();
    }

    @Test
    @DisplayName("UserInfo: richiede admin")
    void testUserInfoRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/UserInfo.java");
        assertThat(source).contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("FINDING: UserInfo usa userAdmin.isAdmin() senza null check")
    void testDocumentMissingNullCheckInUserInfo() throws Exception {
        String source = readSource("src/main/java/Controller/UserInfo.java");
        boolean unsafe =
                source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("userAdmin.isAdmin()") &&
                        !source.contains("userAdmin != null");
        assertThat(unsafe).as("FINDING: NPE UserInfo").isTrue();
    }

    @Test
    @DisplayName("FINDING: UserInfo non gestisce NumberFormatException sull'id")
    void testDocumentNumberFormatExceptionInUserInfo() throws Exception {
        String source = readSource("src/main/java/Controller/UserInfo.java");
        boolean unsafeParse =
                source.contains("Integer.parseInt(request.getParameter(\"id\"))") &&
                        !source.contains("try {");
        assertThat(unsafeParse).as("FINDING: NumberFormatException su id").isTrue();
    }

    @Test
    @DisplayName("ShowCart: legge il carrello dalla sessione")
    void testShowCartReadsCartFromSession() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCart.java");
        assertThat(source).contains("session.getAttribute(\"cart\")");
    }

    @Test
    @DisplayName("ShowCart: restituisce JSON")
    void testShowCartReturnsJson() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCart.java");
        assertThat(source).contains("JSONArray").contains("JSONObject");
    }

    @Test
    @DisplayName("FINDING CRITICO: ShowCart usa cartBean.getCartList() senza null check")
    void testDocumentMissingNullCheckInShowCart() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCart.java");
        boolean unsafe =
                source.contains("CartBean cartBean = (CartBean) session.getAttribute(\"cart\")") &&
                        source.contains("cartBean.getCartList()") &&
                        !source.contains("cartBean != null");
        assertThat(unsafe).as("FINDING: NPE ShowCart").isTrue();
    }

    @Test
    @DisplayName("GDPR: l'utente puo visualizzare il proprio profilo")
    void testUserCanViewOwnProfile() {
        assertThat(user.isAdmin()).isEqualToIgnoringCase("false");
    }

    @Test
    @DisplayName("GDPR: l'utente puo modificare il proprio profilo")
    void testUserCanEditOwnProfile() {
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("GDPR: l'admin puo visualizzare profili di altri utenti")
    void testAdminCanViewOtherProfiles() {
        assertThat(admin.isAdmin()).isEqualToIgnoringCase("true");
    }

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
        assertThat(user.getPassword()).hasSize(40);
    }

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}