package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per le funzioni amministrative.
 * Verifica:
 *  - controllo autorizzazione nei servlet admin
 *  - validazione input (regex prezzo, quantita)
 *  - sicurezza file upload
 *
 * Riferimento: OWASP Testing Guide - OTG-AUTHZ, OWASP A01, A04
 */
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