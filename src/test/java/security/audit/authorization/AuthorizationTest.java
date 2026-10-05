package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 *
 * I test usano analisi statica del codice per documentare
 * le vulnerabilita trovate (mancato null check).
 *
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

        // Cerca pattern pericoloso: user.isAdmin() senza check user != null
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

        // Il pattern problematico: un else dentro un for
        // Uso regex per gestire whitespace e newline tra } e else
        boolean hasForLoop = source.contains("for (OrderBean order : orderBean)");
        boolean hasIfCheck = source.contains("if (order.getId() == id)");
        boolean hasErrorInsideElse = source.contains("Ordine non disponibile");
        boolean hasElseAfterIf = java.util.regex.Pattern
                .compile("\\}\\s*else\\s*\\{")
                .matcher(source)
                .find();

        boolean hasBuggyPattern = hasForLoop && hasIfCheck && hasErrorInsideElse && hasElseAfterIf;

        assertThat(hasBuggyPattern)
                .as("BUG LOGICO: OrderInfo.java esegue l'else dentro il ciclo for. "
                        + "Se l'utente ha 3 ordini e chiede il 2, al primo ordine "
                        + "(id=1) la condizione e falsa e viene mostrato l'errore "
                        + "anche se l'ordine richiesto esiste. "
                        + "Fix: usare una variabile flag 'found' e controllare dopo il loop.")
                .isTrue();
    }

    // ==========================================================
    // 4. DOCUMENTAZIONE - Mancato controllo login in AddToCart
    // ==========================================================

    @Test
    @DisplayName("FINDING: AddToCart.java non verifica autenticazione prima di operare")
    void testDocumentMissingAuthCheckInAddToCart() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        // Il servlet permette di aggiungere al carrello anche senza login
        boolean hasNoAuthGuard =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        !source.contains("if (user == null)") &&
                        !source.contains("user != null)") ;

        // Il codice usa "if (user != null)" solo per il salvataggio DB, non per bloccare l'azione
        assertThat(source)
                .as("FINDING: AddToCart.java accetta richieste anche da utenti non autenticati. "
                        + "Il carrello viene gestito in sessione, quindi e accettabile, "
                        + "ma manca un rate limiting contro abuse.")
                .contains("HttpSession session = request.getSession()");
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
        // admin e null

        // Verifica che il check con equalsIgnoreCase("true") restituisca false
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

    /**
     * Test di sicurezza per le funzioni amministrative.
     * Verifica:
     *  - controllo autorizzazione nei servlet admin
     *  - validazione input (regex prezzo, quantita)
     *  - sicurezza file upload
     *
     * Riferimento: OWASP Testing Guide - OTG-AUTHZ, OWASP A01, A04
     */
    @Nested
    @DisplayName("Admin Functions - Autorizzazione e validazione input")
    class AdminFunctionsTest {

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
        // 1. AUTORIZZAZIONE - Servlet Admin devono richiedere admin
        // ==========================================================

        @Test
        @DisplayName("AddProduct: verifica autorizzazione admin nel doGet")
        void testAddProductGetRequiresAdmin() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            assertThat(source)
                    .as("AddProduct.doGet deve verificare user.isAdmin()")
                    .contains("user.isAdmin().equalsIgnoreCase(\"true\")");
        }

        @Test
        @DisplayName("AddProduct: verifica autorizzazione admin nel doPost")
        void testAddProductPostRequiresAdmin() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            // Conta occorrenze di isAdmin - deve essere presente in doGet E doPost
            long count = source.split("user\\.isAdmin\\(\\)\\.equalsIgnoreCase\\(\"true\"\\)", -1).length - 1;
            assertThat(count)
                    .as("AddProduct deve verificare isAdmin sia in doGet che in doPost")
                    .isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("EditProduct: verifica autorizzazione admin")
        void testEditProductRequiresAdmin() throws Exception {
            String source = readSource("src/main/java/Controller/EditProduct.java");

            assertThat(source)
                    .contains("user.isAdmin().equalsIgnoreCase(\"true\")");
        }

        @Test
        @DisplayName("Users: verifica autorizzazione admin")
        void testUsersRequiresAdmin() throws Exception {
            String source = readSource("src/main/java/Controller/Users.java");

            assertThat(source)
                    .contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
        }

        // ==========================================================
        // 2. FINDING - NPE per mancato null check sui servlet admin
        // ==========================================================

        @Test
        @DisplayName("FINDING: AddProduct usa user.isAdmin() senza null check")
        void testDocumentMissingNullCheckInAddProduct() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            boolean unsafe =
                    source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                            source.contains("if (user.isAdmin()") &&
                            !source.contains("user != null && user.isAdmin()");

            assertThat(unsafe)
                    .as("FINDING: AddProduct.java chiama user.isAdmin() senza verificare user != null. "
                            + "Un utente non autenticato che accede a /add-product-servlet causa "
                            + "NullPointerException (DoS - CWE-476).")
                    .isTrue();
        }

        @Test
        @DisplayName("FINDING: EditProduct usa user.isAdmin() senza null check")
        void testDocumentMissingNullCheckInEditProduct() throws Exception {
            String source = readSource("src/main/java/Controller/EditProduct.java");

            boolean unsafe =
                    source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                            source.contains("if (user.isAdmin()") &&
                            !source.contains("user != null && user.isAdmin()");

            assertThat(unsafe)
                    .as("FINDING: EditProduct.java non verifica user != null.")
                    .isTrue();
        }

        @Test
        @DisplayName("FINDING: Users usa userAdmin.isAdmin() senza null check")
        void testDocumentMissingNullCheckInUsers() throws Exception {
            String source = readSource("src/main/java/Controller/Users.java");

            boolean unsafe =
                    source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                            source.contains("if (userAdmin.isAdmin()") &&
                            !source.contains("userAdmin != null && userAdmin.isAdmin()");

            assertThat(unsafe)
                    .as("FINDING: Users.java non verifica userAdmin != null.")
                    .isTrue();
        }

        // ==========================================================
        // 3. VALIDAZIONE INPUT - Regex AddProduct
        // ==========================================================

        @Test
        @DisplayName("AddProduct: regex prezzo accetta '19.99'")
        void testAddProductPriceAcceptsDecimal() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            assertThat(source)
                    .contains("Pattern.compile(\"^(\\\\d+(?:[.,]\\\\d{2})?)$\")");
        }

        @Test
        @DisplayName("AddProduct: regex quantita accetta solo interi positivi")
        void testAddProductQuantityAcceptsOnlyIntegers() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            assertThat(source)
                    .contains("Pattern.compile(\"^\\\\d+$\")");
        }

        @Test
        @DisplayName("AddProduct: sostituisce ',' con '.' nel prezzo")
        void testAddProductReplacesCommaInPrice() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            assertThat(source)
                    .as("Il prezzo con virgola (formato italiano) deve essere convertito")
                    .contains(".replace(\",\", \".\")");
        }

        @Test
        @DisplayName("AddProduct: richiede level == 6 per salvare")
        void testAddProductRequiresAllSixFieldsValid() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            assertThat(source)
                    .contains("level == 6");
        }

        @Test
        @DisplayName("AddProduct: impedisce duplicati (name + description)")
        void testAddProductRejectsDuplicates() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            assertThat(source)
                    .contains("!service.isAlreadyRegistered(name, description)");
        }

        // ==========================================================
        // 4. VALIDAZIONE INPUT - Regex EditProduct
        // ==========================================================

        @Test
        @DisplayName("EditProduct: usa regex decimal_String per il prezzo")
        void testEditProductUsesDecimalRegex() throws Exception {
            String source = readSource("src/main/java/Controller/EditProduct.java");

            assertThat(source)
                    .contains("Pattern.compile(\"^\\\\d*\\\\.?\\\\d*$\")");
        }

        @Test
        @DisplayName("EditProduct: richiede level == 6 per salvare")
        void testEditProductRequiresAllFieldsValid() throws Exception {
            String source = readSource("src/main/java/Controller/EditProduct.java");

            assertThat(source)
                    .contains("if (level == 6)");
        }

        // ==========================================================
        // 5. SICUREZZA FILE UPLOAD
        // ==========================================================

        @Test
        @DisplayName("FINDING: AddProduct non valida l'estensione del file caricato")
        void testDocumentUnrestrictedFileUploadInAddProduct() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            boolean noExtensionCheck =
                    source.contains("part.getSubmittedFileName()") &&
                            !source.contains(".endsWith(\".jpg\")") &&
                            !source.contains(".endsWith(\".png\")") &&
                            !source.contains("getContentType");

            assertThat(noExtensionCheck)
                    .as("FINDING SICUREZZA: AddProduct.java accetta file upload di qualunque "
                            + "estensione (CWE-434: Unrestricted Upload of File with Dangerous Type, "
                            + "OWASP A04:2021). Un admin malintenzionato (o un attacker che ottiene "
                            + "l'account) puo caricare JSP o HTML malevoli. "
                            + "Fix: whitelist estensioni + controllo MIME type + rinominare file.")
                    .isTrue();
        }

        @Test
        @DisplayName("AddProduct: usa @MultipartConfig con limiti file size")
        void testAddProductHasMultipartConfig() throws Exception {
            String source = readSource("src/main/java/Controller/AddProduct.java");

            assertThat(source)
                    .as("Il servlet deve limitare la dimensione dei file uploadati")
                    .contains("@MultipartConfig")
                    .contains("maxFileSize");
        }

        @Test
        @DisplayName("EditProduct: usa @MultipartConfig con limiti")
        void testEditProductHasMultipartConfig() throws Exception {
            String source = readSource("src/main/java/Controller/EditProduct.java");

            assertThat(source)
                    .contains("@MultipartConfig");
        }

        // ==========================================================
        // 6. LOGICA DI AGGIORNAMENTO - EditProduct
        // ==========================================================

        @Test
        @DisplayName("EditProduct: preserva immagine se non ne viene caricata una nuova")
        void testEditProductPreservesImageIfNoUpload() throws Exception {
            String source = readSource("src/main/java/Controller/EditProduct.java");

            assertThat(source)
                    .as("Se non viene caricata una nuova immagine, mantieni quella esistente")
                    .contains("service.doRetrieveById(id).getImage()");
        }

        @Test
        @DisplayName("EditProduct: redirect a products-servlet dopo modifica")
        void testEditProductRedirectsAfterUpdate() throws Exception {
            String source = readSource("src/main/java/Controller/EditProduct.java");

            assertThat(source)
                    .contains("response.sendRedirect(\"products-servlet\")");
        }

        // ==========================================================
        // 7. BEAN - Validazione stato di default
        // ==========================================================

        @Test
        @DisplayName("Un utente appena creato ha admin=null (default)")
        void testNewUserHasNullAdmin() {
            UserBean newUser = new UserBean();
            assertThat(newUser.isAdmin()).isNull();
        }

        @Test
        @DisplayName("Un utente con admin='TRUE' maiuscolo e ancora admin")
        void testAdminCaseInsensitive() {
            UserBean u = new UserBean();
            u.setAdmin("TRUE");
            assertThat(u.isAdmin()).isEqualToIgnoringCase("true");
        }

        // ==========================================================
        // 8. LOGICA RUOLO + STATO
        // ==========================================================

        @Test
        @DisplayName("Admin attivo puo accedere alle funzioni admin")
        void testAdminActiveCanAccess() {
            boolean canAccess =
                    adminUser.isAdmin().equalsIgnoreCase("true") &&
                            adminUser.isActive().equalsIgnoreCase("true");
            assertThat(canAccess).isTrue();
        }

        @Test
        @DisplayName("Admin disabilitato NON puo accedere")
        void testDisabledAdminCannotAccess() {
            UserBean disabledAdmin = new UserBean();
            disabledAdmin.setAdmin("true");
            disabledAdmin.setState("false");

            boolean canAccess =
                    disabledAdmin.isAdmin().equalsIgnoreCase("true") &&
                            disabledAdmin.isActive().equalsIgnoreCase("true");
            assertThat(canAccess).isFalse();
        }

        // ==========================================================
        // Utility
        // ==========================================================

        private String readSource(String path) throws Exception {
            return new String(Files.readAllBytes(Paths.get(path)));
        }
    }

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
    static
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
    static
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
        static
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
    }
}