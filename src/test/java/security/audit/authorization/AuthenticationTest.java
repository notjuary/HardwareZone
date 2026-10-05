package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per l'area "Authentication".
 * Verifica:
 *  - flusso di login (credenziali valide, errate, account disabilitato)
 *  - flusso di logout (invalidazione sessione)
 *  - sicurezza della gestione sessione
 *
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

    // ==========================================================
    // 1. LOGIN - Analisi statica del servlet
    // ==========================================================

    @Test
    @DisplayName("Login: usa UserDAO.doRetrieveByEmailAndPassword")
    void testLoginUsesCorrectDAO() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .contains("service.doRetrieveByEmailAndPassword(email, password)");
    }

    @Test
    @DisplayName("Login: verifica user.isActive() prima di creare la sessione")
    void testLoginChecksUserActive() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .as("Il login deve verificare che l'utente sia attivo")
                .contains("user.isActive().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("Login: crea la sessione solo dopo validazione")
    void testLoginCreatesSessionAfterValidation() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .contains("session.setAttribute(\"user\", user)");
    }

    @Test
    @DisplayName("Login: reindirizza admin a admin.jsp")
    void testLoginRedirectsAdminToAdminPanel() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .as("Il login deve reindirizzare gli admin al pannello di amministrazione")
                .contains("WEB-INF/admin/admin.jsp");
    }
    @Test
    @DisplayName("Login: reindirizza utente normale a index.jsp")
    void testLoginRedirectsNormalUserToHome() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .contains("index.jsp");
    }

    @Test
    @DisplayName("Login: mostra errore generico (non rivela se email esiste)")
    void testLoginShowsGenericError() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .as("Il messaggio di errore non deve rivelare se l'email esiste")
                .contains("Email o password errati");
    }

    @Test
    @DisplayName("Login: gestisce account disabilitato")
    void testLoginHandlesDisabledAccount() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .contains("Account disabilitato");
    }

    // ==========================================================
    // 2. LOGOUT - Analisi statica del servlet
    // ==========================================================

    @Test
    @DisplayName("Logout: chiama session.invalidate()")
    void testLogoutInvalidatesSession() throws Exception {
        String source = readSource("src/main/java/Controller/Logout.java");

        assertThat(source)
                .as("Il logout deve invalidare la sessione")
                .contains("session.invalidate()");
    }

    @Test
    @DisplayName("FINDING: Logout usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInLogout() throws Exception {
        String source = readSource("src/main/java/Controller/Logout.java");

        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("user.isAdmin()") &&
                        !source.contains("user != null");

        assertThat(unsafe)
                .as("FINDING: Logout.java chiama user.isAdmin() senza verificare user != null. "
                        + "Un utente non loggato che accede a /logout-servlet causa "
                        + "NullPointerException (CWE-476). Fix: aggiungere 'user != null &&'.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: Logout usa cartBean senza null check (NPE nel for)")
    void testDocumentNullCartBeanInLogout() throws Exception {
        String source = readSource("src/main/java/Controller/Logout.java");

        boolean unsafe =
                source.contains("CartBean cartBean = (CartBean) session.getAttribute(\"cart\")") &&
                        source.contains("for (ProductCartBean product : cartBean.getCartList())") &&
                        !source.contains("cartBean != null") &&
                        !source.contains("if (cartBean");

        assertThat(unsafe)
                .as("FINDING: Logout.java itera su cartBean.getCartList() senza verificare "
                        + "che cartBean != null. Se l'utente non ha mai aggiunto prodotti al "
                        + "carrello, cartBean e null e il for lancia NPE (CWE-476). "
                        + "Fix: 'if (cartBean != null && user != null) {...}'.")
                .isTrue();
    }

    // ==========================================================
    // 3. SESSION MANAGEMENT - Bean e logica
    // ==========================================================

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

        // Il web.xml attuale e vuoto: nessun session-timeout configurato
        boolean noSessionTimeout = !source.contains("session-timeout");

        assertThat(noSessionTimeout)
                .as("FINDING SICUREZZA: web.xml non definisce <session-timeout>. "
                        + "Viene usato il default di Tomcat (30 minuti). "
                        + "Un timeout cosi lungo aumenta il rischio di session hijacking "
                        + "(CWE-613: Insufficient Session Expiration, OWASP A07:2021). "
                        + "Fix consigliato: aggiungere <session-config><session-timeout>15</session-timeout></session-config> "
                        + "nel web.xml.")
                .isTrue();
    }

    // ==========================================================
    // 4. SECURITY HEADERS
    // ==========================================================

    @Test
    @DisplayName("Login: il servlet non deve loggare la password")
    void testLoginDoesNotLogPassword() throws Exception {
        String source = readSource("src/main/java/Controller/Login.java");

        assertThat(source)
                .as("Il servlet non deve mai loggare la password")
                .doesNotContain("System.out.println(password)")
                .doesNotContain("log(password)")
                .doesNotContain("printStackTrace(password)");
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}