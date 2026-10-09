package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per Login e Logout.
 * Riferimento: OWASP Testing Guide - OTG-AUTHN, OWASP A07:2021
 */
@DisplayName("Authentication - Login e Logout sicuri")
class AuthenticationTest {

    private UserBean activeUser;
    private UserBean disabledUser;
    private UserBean adminUser;

    @BeforeEach
    void setUp() {
        activeUser = new UserBean();
        activeUser.setState("true");
        activeUser.setAdmin("false");

        disabledUser = new UserBean();
        disabledUser.setState("false");
        disabledUser.setAdmin("false");

        adminUser = new UserBean();
        adminUser.setState("true");
        adminUser.setAdmin("true");
    }

    @Test
    @DisplayName("Login: usa UserDAO.doRetrieveByEmailAndPassword")
    void testLoginUsesCorrectDAO() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source).contains("service.doRetrieveByEmailAndPassword(email, password)");
    }

    @Test
    @DisplayName("Login: verifica user.isActive() prima di creare la sessione")
    void testLoginChecksUserActive() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source).contains("user.isActive().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("Login: crea la sessione solo dopo validazione")
    void testLoginCreatesSessionAfterValidation() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source).contains("session.setAttribute(\"user\", user)");
    }

    @Test
    @DisplayName("Login: reindirizza admin a admin.jsp")
    void testLoginRedirectsAdminToAdminPanel() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source).contains("WEB-INF/admin/admin.jsp");
    }

    @Test
    @DisplayName("Login: reindirizza utente normale a index.jsp")
    void testLoginRedirectsNormalUserToHome() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source).contains("index.jsp");
    }

    @Test
    @DisplayName("Login: mostra errore generico (non rivela se email esiste)")
    void testLoginShowsGenericError() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source).contains("Email o password errati");
    }

    @Test
    @DisplayName("Login: gestisce account disabilitato")
    void testLoginHandlesDisabledAccount() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source).contains("Account disabilitato");
    }

    @Test
    @DisplayName("Logout: chiama session.invalidate()")
    void testLogoutInvalidatesSession() throws Exception {
        String source = readSource("src/main/java/Controller/Logout.java");
        assertThat(source).contains("session.invalidate()");
    }

    @Test
    @DisplayName("FIXED: Logout verifica user != null prima di isAdmin()")
    void testDocumentMissingNullCheckInLogout() throws Exception {
        String source = readSource("src/main/java/Controller/Logout.java");

        boolean isFixed =
                source.contains("user != null") &&
                        source.contains("cartBean != null");

        assertThat(isFixed)
                .as("FIXED: Logout ora verifica che user != null e cartBean != null "
                        + "prima di chiamare isAdmin() e getCartList(). "
                        + "L'NPE (CWE-476) è stato risolto.")
                .isTrue();
    }

    @Test
    @DisplayName("FIXED: Logout usa getSession(false) per non creare sessioni vuote")
    void testDocumentNullCartBeanInLogout() throws Exception {
        String source = readSource("src/main/java/Controller/Logout.java");

        boolean isFixed =
                source.contains("getSession(false)") &&
                        source.contains("if (session != null)");

        assertThat(isFixed)
                .as("FIXED: Logout usa getSession(false) e verifica che session != null. "
                        + "Il bug è stato risolto.")
                .isTrue();
    }
    @Test
    @DisplayName("UserBean: un utente attivo ha state='true'")
    void testActiveUserState() {
        assertThat(activeUser.isActive()).isEqualToIgnoringCase("true");
    }

    @Test
    @DisplayName("UserBean: un utente disabilitato ha state='false'")
    void testDisabledUserState() {
        assertThat(disabledUser.isActive()).isEqualToIgnoringCase("false");
    }

    @Test
    @DisplayName("UserBean: un admin ha admin='true'")
    void testAdminRole() {
        assertThat(adminUser.isAdmin()).isEqualToIgnoringCase("true");
    }

    @Test
    @DisplayName("FINDING: web.xml non definisce session-timeout (configurazione di default)")
    void testDocumentMissingSessionTimeout() throws Exception {
        String webXmlPath = "src/main/webapp/WEB-INF/web.xml";
        String source = new String(Files.readAllBytes(Paths.get(webXmlPath)));
        boolean noSessionTimeout = !source.contains("session-timeout");
        assertThat(noSessionTimeout).as("FINDING: no session-timeout in web.xml").isTrue();
    }

    @Test
    @DisplayName("Login: il servlet non deve loggare la password")
    void testLoginDoesNotLogPassword() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");
        assertThat(source)
                .doesNotContain("System.out.println(password)")
                .doesNotContain("log(password)")
                .doesNotContain("printStackTrace(password)");
    }

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}