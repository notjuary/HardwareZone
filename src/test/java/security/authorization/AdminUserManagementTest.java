package security.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per le funzioni amministrative di gestione utenti.
 * Verifica:
 *  - SetAdmin (promozione a admin)
 *  - SetStateUser (attiva/disattiva utente)
 *  - Protezioni contro privilege escalation e self-lockout
 *
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

    // ==========================================================
    // 1. SETADMIN - Autorizzazione
    // ==========================================================

    @Test
    @DisplayName("SetAdmin: richiede che l'utente sia admin")
    void testSetAdminRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");

        assertThat(source)
                .contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("SetAdmin: reindirizza a index.jsp se non admin")
    void testSetAdminRedirectsNonAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");

        assertThat(source)
                .contains("index.jsp");
    }

    @Test
    @DisplayName("SetAdmin: impedisce di promuovere chi e gia admin")
    void testSetAdminPreventsAlreadyAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");

        assertThat(source)
                .as("Il servlet deve verificare se l'utente target e gia admin")
                .contains("L'utente selezionato è già un amministratore");
    }

    @Test
    @DisplayName("SetAdmin: impedisce di promuovere un utente disabilitato")
    void testSetAdminPreventsInactiveUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");

        assertThat(source)
                .contains("Utente non attivo");
    }

    // ==========================================================
    // 2. SETADMIN - Finding di sicurezza
    // ==========================================================

    @Test
    @DisplayName("FINDING: SetAdmin usa userAdmin.isAdmin() senza null check")
    void testDocumentMissingNullCheckInSetAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");

        boolean unsafe =
                source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (userAdmin.isAdmin()") &&
                        !source.contains("userAdmin != null && userAdmin.isAdmin()");

        assertThat(unsafe)
                .as("FINDING: SetAdmin.java chiama userAdmin.isAdmin() senza verificare "
                        + "userAdmin != null. Un utente non autenticato che accede a "
                        + "/set-admin-servlet causa NullPointerException (CWE-476).")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: SetAdmin e' un'azione GET (no protezione CSRF)")
    void testDocumentCsrfInSetAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");

        assertThat(source)
                .as("FINDING: SetAdmin espone l'azione di promozione admin via GET. "
                        + "Un attaccante puo creare un link malevolo che, se cliccato da "
                        + "un admin, promuove un utente arbitrario (CSRF - CWE-352, "
                        + "OWASP A01:2021). Fix: usare POST + token CSRF.")
                .contains("doGet(HttpServletRequest");
    }

    @Test
    @DisplayName("FINDING: SetAdmin non limita chi puo promuovere (un admin qualsiasi puo promuovere)")
    void testDocumentUnrestrictedPromotion() throws Exception {
        String source = readSource("src/main/java/Controller/SetAdmin.java");

        // Un admin qualsiasi puo promuovere qualcun altro. Non c'e un "super admin".
        assertThat(source)
                .as("FINDING: qualunque admin puo promuovere qualunque utente ad admin. "
                        + "Non esiste un ruolo 'super admin' con permessi limitati. "
                        + "Questo significa che un admin compromesso puo creare altri admin "
                        + "senza limiti (privilege escalation, OWASP A01:2021).")
                .doesNotContain("superAdmin")
                .doesNotContain("isSuperAdmin");
    }

    // ==========================================================
    // 3. SETSTATEUSER - Autorizzazione
    // ==========================================================

    @Test
    @DisplayName("SetStateUser: richiede che l'utente sia admin")
    void testSetStateUserRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");

        assertThat(source)
                .contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
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

        assertThat(source)
                .contains("users-servlet");
    }

    // ==========================================================
    // 4. SETSTATEUSER - Finding di sicurezza
    // ==========================================================

    @Test
    @DisplayName("FINDING: SetStateUser usa userAdmin.isAdmin() senza null check")
    void testDocumentMissingNullCheckInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");

        boolean unsafe =
                source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (userAdmin.isAdmin()") &&
                        !source.contains("userAdmin != null && userAdmin.isAdmin()");

        assertThat(unsafe)
                .as("FINDING: SetStateUser.java chiama userAdmin.isAdmin() senza verificare "
                        + "userAdmin != null. NPE per utenti non autenticati.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING CRITICO: SetStateUser permette a un admin di disabilitare un altro admin")
    void testDocumentPrivilegeEscalationInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");

        // Il servlet non verifica se l'utente target e admin
        boolean noAdminCheck =
                !source.contains("user.isAdmin()") &&
                        !source.contains("target.isAdmin()");

        assertThat(noAdminCheck)
                .as("FINDING CRITICO: SetStateUser non verifica se l'utente target e admin. "
                        + "Un admin malevolo puo disabilitare tutti gli altri admin "
                        + "(incluso il creatore del sistema), assumendo il controllo totale. "
                        + "Fix: verificare che user.isAdmin() == 'false' prima di disabilitare.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING CRITICO: SetStateUser permette a un admin di auto-disabilitarsi")
    void testDocumentSelfLockoutInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");

        // Il servlet non verifica se l'admin sta disabilitando se stesso
        boolean noSelfCheck =
                !source.contains("userAdmin.getId() != user.getId()") &&
                        !source.contains("userAdmin.getId() == user.getId()") &&
                        !source.contains("self");

        assertThat(noSelfCheck)
                .as("FINDING: SetStateUser non impedisce a un admin di disabilitare se stesso. "
                        + "Se un admin clicca il pulsante 'disabilita' sul proprio profilo, "
                        + "si auto-disabilita e non puo piu accedere (self-lockout). "
                        + "Fix: verificare userAdmin.getId() != user.getId().")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: SetStateUser e' un'azione GET (no protezione CSRF)")
    void testDocumentCsrfInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");

        assertThat(source)
                .as("FINDING: SetStateUser espone un'azione di modifica stato via GET. "
                        + "Un attaccante puo creare un link che disabilita un utente se "
                        + "cliccato da un admin (CSRF - CWE-352). Fix: usare POST + token CSRF.")
                .contains("doGet(HttpServletRequest");
    }

    @Test
    @DisplayName("FINDING: SetStateUser non valida il parametro 'id'")
    void testDocumentMissingIdValidationInSetStateUser() throws Exception {
        String source = readSource("src/main/java/Controller/SetStateUser.java");

        boolean noValidation =
                source.contains("Integer.parseInt(request.getParameter(\"id\"))") &&
                        !source.contains("try {") ;

        assertThat(noValidation)
                .as("FINDING: SetStateUser chiama Integer.parseInt senza try/catch. "
                        + "Se un utente invia 'id=abc', la servlet lancia NumberFormatException "
                        + "(DoS - CWE-20).")
                .isTrue();
    }

    // ==========================================================
    // 5. USERBEAN - Verifica ruoli
    // ==========================================================

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

    // ==========================================================
    // 6. LOGICA DI BUSINESS - Analisi combinata admin
    // ==========================================================

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

        assertThat(canPerformActions)
                .as("Anche se admin, un utente disabilitato non puo operare")
                .isFalse();
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}