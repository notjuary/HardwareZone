package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per SetAdmin e SetStateUser.
 * Riferimento: OWASP Testing Guide - OTG-AUTHZ-003, OWASP A01:2021
 */
@DisplayName("Admin User Management - SetAdmin e SetStateUser")
class AdminUserManagementTest {

    private UserBean adminUser;
    private UserBean normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new UserBean();
        adminUser.setId(1);
        adminUser.setAdmin("true");
        adminUser.setState("true");

        normalUser = new UserBean();
        normalUser.setId(2);
        normalUser.setAdmin("false");
        normalUser.setState("true");
    }

    @Test
    @DisplayName("SetAdmin: richiede che l'utente sia admin")
    void testSetAdminRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");
        assertThat(source).contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("SetAdmin: reindirizza a index.jsp se non admin")
    void testSetAdminRedirectsNonAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");
        assertThat(source).contains("index.jsp");
    }

    @Test
    @DisplayName("SetAdmin: impedisce di promuovere chi e gia admin")
    void testSetAdminPreventsAlreadyAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");
        assertThat(source).contains("L'utente selezionato è già un amministratore");
    }

    @Test
    @DisplayName("SetAdmin: impedisce di promuovere un utente disabilitato")
    void testSetAdminPreventsInactiveUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");
        assertThat(source).contains("Utente non attivo");
    }

    @Test
    @DisplayName("FINDING: SetAdmin usa userAdmin.isAdmin() senza null check")
    void testDocumentMissingNullCheckInSetAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");
        boolean unsafe =
                source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (userAdmin.isAdmin()") &&
                        !source.contains("userAdmin != null && userAdmin.isAdmin()");
        assertThat(unsafe).as("FINDING: SetAdmin.java NPE su userAdmin null").isTrue();
    }

    @Test
    @DisplayName("FINDING: SetAdmin e' un'azione GET (no protezione CSRF)")
    void testDocumentCsrfInSetAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");
        assertThat(source)
                .as("FINDING: SetAdmin GET senza CSRF (CWE-352)")
                .contains("doGet(HttpServletRequest");
    }

    @Test
    @DisplayName("FINDING: SetAdmin non limita chi puo promuovere")
    void testDocumentUnrestrictedPromotion() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");
        assertThat(source)
                .as("FINDING: nessun ruolo super admin, privilege escalation possibile")
                .doesNotContain("superAdmin")
                .doesNotContain("isSuperAdmin");
    }

    @Test
    @DisplayName("SetStateUser: richiede che l'utente sia admin")
    void testSetStateUserRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        assertThat(source).contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("SetStateUser: inverte lo stato dell'utente target")
    void testSetStateUserInvertsState() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        assertThat(source)
                .contains("user.setState(\"false\")")
                .contains("user.setState(\"true\")");
    }

    @Test
    @DisplayName("SetStateUser: reindirizza a users-servlet dopo operazione")
    void testSetStateUserRedirectsAfterOperation() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        assertThat(source).contains("users-servlet");
    }

    @Test
    @DisplayName("FINDING: SetStateUser usa userAdmin.isAdmin() senza null check")
    void testDocumentMissingNullCheckInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        boolean unsafe =
                source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (userAdmin.isAdmin()") &&
                        !source.contains("userAdmin != null && userAdmin.isAdmin()");
        assertThat(unsafe).as("FINDING: SetStateUser.java NPE su userAdmin null").isTrue();
    }

    @Test
    @DisplayName("FINDING CRITICO: SetStateUser permette a un admin di disabilitare un altro admin")
    void testDocumentPrivilegeEscalationInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        boolean noAdminCheck =
                !source.contains("user.isAdmin()") &&
                        !source.contains("target.isAdmin()");
        assertThat(noAdminCheck)
                .as("FINDING CRITICO: admin può disabilitare altri admin")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING CRITICO: SetStateUser permette a un admin di auto-disabilitarsi")
    void testDocumentSelfLockoutInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        boolean noSelfCheck =
                !source.contains("userAdmin.getId() != user.getId()") &&
                        !source.contains("userAdmin.getId() == user.getId()") &&
                        !source.contains("self");
        assertThat(noSelfCheck)
                .as("FINDING: admin può auto-disabilitarsi (self-lockout)")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: SetStateUser e' un'azione GET (no protezione CSRF)")
    void testDocumentCsrfInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        assertThat(source)
                .as("FINDING: SetStateUser GET senza CSRF")
                .contains("doGet(HttpServletRequest");
    }

    @Test
    @DisplayName("FINDING: SetStateUser non valida il parametro 'id'")
    void testDocumentMissingIdValidationInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");
        boolean noValidation =
                source.contains("Integer.parseInt(request.getParameter(\"id\"))") &&
                        !source.contains("try {");
        assertThat(noValidation)
                .as("FINDING: NumberFormatException su id non numerico (CWE-20)")
                .isTrue();
    }

    @Test
    @DisplayName("UserBean: e possibile modificare il ruolo admin")
    void testUserBeanAllowsAdminChange() {
        UserBean user = new UserBean();
        user.setAdmin("false");
        assertThat(user.isAdmin()).isEqualToIgnoringCase("false");

        user.setAdmin("true");
        assertThat(user.isAdmin()).isEqualToIgnoringCase("true");
    }

    @Test
    @DisplayName("UserBean: e possibile modificare lo stato attivo")
    void testUserBeanAllowsStateChange() {
        UserBean user = new UserBean();
        user.setState("true");
        assertThat(user.isActive()).isEqualToIgnoringCase("true");

        user.setState("false");
        assertThat(user.isActive()).isEqualToIgnoringCase("false");
    }

    @Test
    @DisplayName("UserBean: id e un intero univoco")
    void testUserBeanIdIsInteger() {
        UserBean u1 = new UserBean();
        u1.setId(100);
        UserBean u2 = new UserBean();
        u2.setId(200);
        assertThat(u1.getId()).isNotEqualTo(u2.getId());
    }

    @Test
    @DisplayName("Admin attivo con ruolo 'true' puo eseguire azioni amministrative")
    void testActiveAdminCanPerformActions() {
        boolean canPerformActions =
                adminUser.isAdmin().equalsIgnoreCase("true") &&
                        adminUser.isActive().equalsIgnoreCase("true");
        assertThat(canPerformActions).isTrue();
    }

    @Test
    @DisplayName("Un utente normale non puo eseguire azioni amministrative")
    void testNormalUserCannotPerformActions() {
        boolean canPerformActions =
                normalUser.isAdmin().equalsIgnoreCase("true") &&
                        normalUser.isActive().equalsIgnoreCase("true");
        assertThat(canPerformActions).isFalse();
    }

    @Test
    @DisplayName("Un utente disabilitato non puo eseguire azioni")
    void testDisabledUserCannotPerformActions() {
        UserBean disabled = new UserBean();
        disabled.setAdmin("true");
        disabled.setState("false");

        boolean canPerformActions =
                disabled.isAdmin().equalsIgnoreCase("true") &&
                        disabled.isActive().equalsIgnoreCase("true");
        assertThat(canPerformActions).isFalse();
    }

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}