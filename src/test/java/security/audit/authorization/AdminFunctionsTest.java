package security.audit.authorization;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per le funzioni amministrative.
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

    @Test
    @DisplayName("AddProduct: verifica autorizzazione admin nel doGet")
    void testAddProductGetRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        assertThat(source).contains("user.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("AddProduct: verifica autorizzazione admin nel doPost")
    void testAddProductPostRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        long count = source.split("user\\.isAdmin\\(\\)\\.equalsIgnoreCase\\(\"true\"\\)", -1).length - 1;
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("EditProduct: verifica autorizzazione admin")
    void testEditProductRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        assertThat(source).contains("user.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("Users: verifica autorizzazione admin")
    void testUsersRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/Users.java");
        assertThat(source).contains("userAdmin.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("FINDING: AddProduct usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInAddProduct() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (user.isAdmin()") &&
                        !source.contains("user != null && user.isAdmin()");
        assertThat(unsafe).as("FINDING: AddProduct.java NPE su utente null").isTrue();
    }

    @Test
    @DisplayName("FINDING: EditProduct usa user.isAdmin() senza null check")
    void testDocumentMissingNullCheckInEditProduct() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (user.isAdmin()") &&
                        !source.contains("user != null && user.isAdmin()");
        assertThat(unsafe).as("FINDING: EditProduct.java NPE su utente null").isTrue();
    }

    @Test
    @DisplayName("FINDING: Users usa userAdmin.isAdmin() senza null check")
    void testDocumentMissingNullCheckInUsers() throws Exception {
        String source = readSource("src/main/java/Controller/Users.java");
        boolean unsafe =
                source.contains("UserBean userAdmin = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("if (userAdmin.isAdmin()") &&
                        !source.contains("userAdmin != null && userAdmin.isAdmin()");
        assertThat(unsafe).as("FINDING: Users.java NPE su userAdmin null").isTrue();
    }

    @Test
    @DisplayName("AddProduct: regex prezzo accetta '19.99'")
    void testAddProductPriceAcceptsDecimal() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        assertThat(source).contains("Pattern.compile(\"^(\\\\d+(?:[.,]\\\\d{2})?)$\")");
    }

    @Test
    @DisplayName("AddProduct: regex quantita accetta solo interi positivi")
    void testAddProductQuantityAcceptsOnlyIntegers() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        assertThat(source).contains("Pattern.compile(\"^\\\\d+$\")");
    }

    @Test
    @DisplayName("AddProduct: sostituisce ',' con '.' nel prezzo")
    void testAddProductReplacesCommaInPrice() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        assertThat(source).contains(".replace(\",\", \".\")");
    }

    @Test
    @DisplayName("AddProduct: richiede level == 6 per salvare")
    void testAddProductRequiresAllSixFieldsValid() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        assertThat(source).contains("level == 6");
    }

    @Test
    @DisplayName("AddProduct: impedisce duplicati (name + description)")
    void testAddProductRejectsDuplicates() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        assertThat(source).contains("!service.isAlreadyRegistered(name, description)");
    }

    @Test
    @DisplayName("EditProduct: usa regex decimal_String per il prezzo")
    void testEditProductUsesDecimalRegex() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        assertThat(source).contains("Pattern.compile(\"^\\\\d*\\\\.?\\\\d*$\")");
    }

    @Test
    @DisplayName("EditProduct: richiede level == 6 per salvare")
    void testEditProductRequiresAllFieldsValid() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        assertThat(source).contains("if (level == 6)");
    }

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
                .as("FINDING: file upload senza whitelist (CWE-434)")
                .isTrue();
    }

    @Test
    @DisplayName("AddProduct: usa @MultipartConfig con limiti file size")
    void testAddProductHasMultipartConfig() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");
        assertThat(source).contains("@MultipartConfig").contains("maxFileSize");
    }

    @Test
    @DisplayName("EditProduct: usa @MultipartConfig con limiti")
    void testEditProductHasMultipartConfig() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        assertThat(source).contains("@MultipartConfig");
    }

    @Test
    @DisplayName("EditProduct: preserva immagine se non ne viene caricata una nuova")
    void testEditProductPreservesImageIfNoUpload() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        assertThat(source).contains("service.doRetrieveById(id).getImage()");
    }

    @Test
    @DisplayName("EditProduct: redirect a products-servlet dopo modifica")
    void testEditProductRedirectsAfterUpdate() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        assertThat(source).contains("response.sendRedirect(\"products-servlet\")");
    }

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

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}