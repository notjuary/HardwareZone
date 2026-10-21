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
    @DisplayName("FIXED: AddProduct verifica che user != null prima di isAdmin()")
    void testDocumentMissingNullCheckInAddProduct() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");

        // Ora ci aspettiamo che il null check ci sia
        boolean isFixed =
                source.contains("user == null") &&
                        source.contains("!user.isAdmin().equalsIgnoreCase(\"true\")");

        assertThat(isFixed)
                .as("FIXED: AddProduct ora verifica user == null (CWE-476 risolto)")
                .isTrue();
    }

    @Test
    @DisplayName("FIXED: EditProduct verifica user != null prima di isAdmin()")
    void testDocumentMissingNullCheckInEditProduct() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");
        boolean isFixed = source.contains("user == null") && source.contains("!user.isAdmin().equalsIgnoreCase(\"true\")");
        assertThat(isFixed).as("FIXED: EditProduct ora verifica user == null").isTrue();
    }

    @Test
    @DisplayName("FIXED: Users verifica userAdmin != null prima di isAdmin()")
    void testDocumentMissingNullCheckInUsers() throws Exception {
        String source = readSource("src/main/java/Controller/Users.java");

        boolean isFixed =
                source.contains("userAdmin == null") &&
                        source.contains("!userAdmin.isAdmin().equalsIgnoreCase(\"true\")");

        assertThat(isFixed)
                .as("FIXED: Users ora verifica che l'utente non sia null (CWE-476 risolto).")
                .isTrue();
    }

    @Test
    @DisplayName("AddProduct: il prezzo accetta numeri decimali (Regex semplificata)")
    void testAddProductPriceAcceptsDecimal() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");

        // Verifica la presenza della nuova regex sicura
        assertThat(source)
                .as("AddProduct deve usare una regex sicura per i prezzi")
                .contains("private static final Pattern DECIMAL_PATTERN = Pattern.compile(\"^\\\\d+(\\\\.\\\\d+)?$\");");
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
    @DisplayName("AddProduct: valida i campi con early return")
    void testAddProductRequiresAllSixFieldsValid() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");

        // Verifica che ci siano i controlli sui campi obbligatori
        assertThat(source)
                .as("AddProduct deve validare i campi e uscire in caso di errore")
                .contains("!isValidString(name, 20)")
                .contains("sendError(request, response, \"Errore inserimento\"");
    }
    @Test
    @DisplayName("AddProduct: rifiuta i duplicati con Early Return")
    void testAddProductRejectsDuplicates() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");

        boolean hasDuplicateCheck =
                source.contains("serviceProduct.isAlreadyRegistered(name, description)") &&
                        source.contains("return;");

        assertThat(hasDuplicateCheck)
                .as("AddProduct deve controllare i duplicati e interrompere l'esecuzione se esistono")
                .isTrue();
    }

    @Test
    @DisplayName("FIXED: EditProduct usa una regex sicura per i decimali")
    void testEditProductUsesDecimalRegex() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");

        // FIX S5853: Assertion chain
        assertThat(source)
                .as("La vecchia regex non deve esserci e la nuova deve essere presente")
                .doesNotContain("Pattern.compile(\"^\\\\d*\\\\.?\\\\d*$\")")
                .contains("private static final Pattern DECIMAL_PATTERN = Pattern.compile(\"^\\\\d+(\\\\.\\\\d+)?$\");");
    }

    @Test
    @DisplayName("EditProduct: valida i campi con Early Return")
    void testEditProductRequiresAllFieldsValid() throws Exception {
        String source = readSource("src/main/java/Controller/EditProduct.java");

        // Ora il codice usa i controlli diretti anziché la variabile 'level'
        boolean hasValidation =
                source.contains("!isValidString(name, 20)") &&
                        source.contains("!DECIMAL_PATTERN.matcher(price).find()") &&
                        source.contains("sendError(request, response, \"Errore modifica: campi non validi\")");

        assertThat(hasValidation)
                .as("EditProduct deve validare tutti i campi e interrompere l'esecuzione se uno non è valido")
                .isTrue();
    }

    @Test
    @DisplayName("FIXED: AddProduct valida l'estensione del file caricato (Whitelist)")
    void testDocumentUnrestrictedFileUploadInAddProduct() throws Exception {
        String source = readSource("src/main/java/Controller/AddProduct.java");

        // Ora ci aspettiamo che ci sia il controllo sull'estensione
        boolean isFixed =
                source.contains("part.getSubmittedFileName()") &&
                        (source.contains(".endsWith(\".jpg\")") || source.contains(".endsWith(\".png\")"));

        assertThat(isFixed)
                .as("FIXED: AddProduct valida l'estensione del file con una whitelist (CWE-434 risolto)")
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
    @DisplayName("EditProduct: preserva immagine se non c'è upload")
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