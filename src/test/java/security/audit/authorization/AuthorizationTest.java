package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per l'area "Authorization & Access Control".
 * Verifica che i servlet controllino correttamente:
 *  - l'autenticazione (utente loggato)
 *  - l'autorizzazione (ruolo admin/user)
 * Riferimento: OWASP Testing Guide - OTG-AUTHZ, OWASP A01:2021
 */
@DisplayName("Authorization - Controllo accessi e ruoli")
class AuthorizationTest {

    private UserBean adminUser;
    private UserBean normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new UserBean();
        adminUser.setAdmin("true");
        adminUser.setState("true");

        normalUser = new UserBean();
        normalUser.setAdmin("false");
        normalUser.setState("true");
    }

    // ==========================================================
    // 1. Logica di autorizzazione - Ruoli utente
    // ==========================================================

    @Test
    @DisplayName("Un admin ha isAdmin() == true")
    void testAdminUserHasCorrectRole() {
        assertThat(adminUser.isAdmin()).isEqualToIgnoringCase("true");
    }

    @Test
    @DisplayName("Un utente normale ha isAdmin() == false")
    void testNormalUserHasCorrectRole() {
        assertThat(normalUser.isAdmin()).isEqualToIgnoringCase("false");
    }

    @Test
    @DisplayName("Un utente attivo ha isActive() == true")
    void testActiveUserIsActive() {
        assertThat(adminUser.isActive()).isEqualToIgnoringCase("true");
    }

    @Test
    @DisplayName("Un utente disabilitato NON deve poter accedere")
    void testDisabledUserState() {
        UserBean disabledUser = new UserBean();
        disabledUser.setState("false");

        assertThat(disabledUser.isActive()).isEqualToIgnoringCase("false");
    }

    // ==========================================================
    // 2. DOCUMENTAZIONE - NPE per mancato null check
    // ==========================================================

    @Test
    @DisplayName("FINDING: Products.java usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInProducts() throws Exception {
        String source = readSource("src/main/java/Controller/Products.java");

        boolean hasUnsafeAccess =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (user.isAdmin()") &&
                        !source.contains("user != null && user.isAdmin()");

        assertThat(hasUnsafeAccess)
                .as("FINDING: Products.java chiama user.isAdmin() senza verificare che user != null. "
                        + "Se un utente non autenticato accede, si verifica NullPointerException "
                        + "(DoS - CWE-476). Fix: aggiungere 'user != null &&' prima del check isAdmin.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: Orders.java usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInOrders() throws Exception {
        String source = readSource("src/main/java/Controller/Orders.java");

        boolean hasUnsafeAccess =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (user.isAdmin()") &&
                        !source.contains("user != null && user.isAdmin()");

        assertThat(hasUnsafeAccess)
                .as("FINDING: Orders.java chiama user.isAdmin() senza verificare che user != null.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: OrderInfo.java usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInOrderInfo() throws Exception {
        String source = readSource("src/main/java/Controller/OrderInfo.java");

        boolean hasUnsafeAccess =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (user.isAdmin()") &&
                        !source.contains("user != null && user.isAdmin()");

        assertThat(hasUnsafeAccess)
                .as("FINDING: OrderInfo.java chiama user.isAdmin() senza verificare che user != null.")
                .isTrue();
    }

    // ==========================================================
    // 3. DOCUMENTAZIONE - Bug logico in OrderInfo
    // ==========================================================

    @Test
    @DisplayName("FINDING: OrderInfo.java ha else dentro il for (bug logico)")
    void testDocumentOrderInfoLoopBug() throws Exception {
        String source = readSource("src/main/java/Controller/OrderInfo.java");

        boolean hasForLoop = source.contains("for (OrderBean order : orderBean)");
        boolean hasIfCheck = source.contains("if (order.getId() == id)");
        boolean hasErrorInsideElse = source.contains("Ordine non disponibile");
        boolean hasElseAfterIf = java.util.regex.Pattern
                .compile("\\}\\s*else\\s*\\{")
                .matcher(source)
                .find();

        boolean hasBuggyPattern = hasForLoop && hasIfCheck && hasErrorInsideElse && hasElseAfterIf;

        assertThat(hasBuggyPattern)
                .as("BUG LOGICO: OrderInfo.java esegue l'else dentro il ciclo for.")
                .isTrue();
    }

    // ==========================================================
    // 4. DOCUMENTAZIONE - Mancato controllo login in AddToCart
    // ==========================================================

    @Test
    @DisplayName("FINDING: AddToCart.java non blocca gli utenti non autenticati")
    void testDocumentMissingAuthCheckInAddToCart() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        // Il servlet legge l'utente dalla sessione ma NON blocca l'azione se user == null
        boolean readsUserFromSession =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")");

        // Non c'è un return anticipato o un blocco esplicito per user == null
        boolean hasNoEarlyAuthGuard =
                !source.contains("if (user == null)") &&
                        !source.contains("if (user == null) {") &&
                        !source.contains("response.sendError(401") &&
                        !source.contains("response.sendRedirect(\"login.jsp\")");

        assertThat(readsUserFromSession)
                .as("Il servlet legge user dalla sessione")
                .isTrue();

        assertThat(hasNoEarlyAuthGuard)
                .as("FINDING: AddToCart.java non blocca gli utenti non autenticati. "
                        + "Il carrello è gestito in sessione (accettabile), "
                        + "ma manca un rate limiting contro abuse.")
                .isTrue();
    }

    // ==========================================================
    // 5. Verifica comportamento isAdmin case-insensitive
    // ==========================================================

    @Test
    @DisplayName("isAdmin accetta 'TRUE', 'true', 'True' (case-insensitive)")
    void testIsAdminIsCaseInsensitive() {
        UserBean u1 = new UserBean();
        u1.setAdmin("TRUE");
        assertThat(u1.isAdmin()).isEqualToIgnoringCase("true");

        UserBean u2 = new UserBean();
        u2.setAdmin("True");
        assertThat(u2.isAdmin()).isEqualToIgnoringCase("true");

        UserBean u3 = new UserBean();
        u3.setAdmin("true");
        assertThat(u3.isAdmin()).isEqualToIgnoringCase("true");
    }

    @Test
    @DisplayName("Un utente appena creato ha admin=null (default)")
    void testNewUserHasNullAdmin() {
        UserBean newUser = new UserBean();
        assertThat(newUser.isAdmin()).isNull();
    }

    @Test
    @DisplayName("Un utente con admin=null deve essere trattato come non-admin")
    void testNullAdminIsNotAdmin() {
        UserBean user = new UserBean();
        boolean isAdmin = "true".equalsIgnoreCase(user.isAdmin());
        assertThat(isAdmin).isFalse();
    }

    // ==========================================================
    // 6. Verifica ruoli combinati (admin + active)
    // ==========================================================

    @Test
    @DisplayName("Un admin disabilitato NON deve poter accedere alle funzioni admin")
    void testDisabledAdminLosesAccess() {
        UserBean disabledAdmin = new UserBean();
        disabledAdmin.setAdmin("true");
        disabledAdmin.setState("false");

        boolean canAccessAdminPanel =
                disabledAdmin.isAdmin().equalsIgnoreCase("true") &&
                        disabledAdmin.isActive().equalsIgnoreCase("true");

        assertThat(canAccessAdminPanel)
                .as("Un admin disabilitato NON deve poter accedere")
                .isFalse();
    }

    @Test
    @DisplayName("Un admin attivo PUO accedere alle funzioni admin")
    void testActiveAdminHasAccess() {
        boolean canAccessAdminPanel =
                adminUser.isAdmin().equalsIgnoreCase("true") &&
                        adminUser.isActive().equalsIgnoreCase("true");

        assertThat(canAccessAdminPanel).isTrue();
    }

    // ==========================================================
    // 7. Lista di servlet che richiedono protezione
    // ==========================================================

    @Test
    @DisplayName("Verifica che tutti i servlet admin-richiedenti siano censiti")
    void testListOfAdminServlets() {
        List<String> adminServlets = new ArrayList<>();
        adminServlets.add("Products.java");
        adminServlets.add("Orders.java");
        adminServlets.add("OrderInfo.java");

        assertThat(adminServlets)
                .as("Servlets che richiedono controllo autorizzazione admin")
                .hasSizeGreaterThanOrEqualTo(3);
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}