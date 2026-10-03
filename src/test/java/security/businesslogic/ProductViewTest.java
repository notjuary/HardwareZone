package security.businesslogic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per la visualizzazione dei prodotti.
 * Verifica: ProductInfo, ShowProduct, ShowCatalog, ShowSales, ProductsHomepage.
 *
 * Riferimento: OWASP Testing Guide - OTG-BUSLOGIC, OWASP A04:2021
 */
@DisplayName("Product View - Info, show, catalog, sales, homepage")
class ProductViewTest {

    // ==========================================================
    // 1. PRODUCTINFO
    // ==========================================================

    @Test
    @DisplayName("ProductInfo: richiede admin (user.isAdmin() == true)")
    void testProductInfoRequiresAdmin() throws Exception {
        String source = readSource("src/main/java/Controller/ProductInfo.java");

        assertThat(source)
                .contains("user.isAdmin().equalsIgnoreCase(\"true\")");
    }

    @Test
    @DisplayName("FINDING CRITICO: ProductInfo usa user.isAdmin() senza null check")
    void testDocumentNullUserInProductInfo() throws Exception {
        String source = readSource("src/main/java/Controller/ProductInfo.java");

        boolean unsafe =
                source.contains("UserBean user = (UserBean) session.getAttribute(\"user\")") &&
                        source.contains("user.isAdmin()") &&
                        !source.contains("user != null");

        assertThat(unsafe)
                .as("FINDING: ProductInfo chiama user.isAdmin() senza verificare "
                        + "user != null. Un utente non loggato che accede a "
                        + "/product-info-servlet causa NPE (CWE-476).")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: ProductInfo non ha else per utenti non-admin")
    void testDocumentNoElseInProductInfo() throws Exception {
        String source = readSource("src/main/java/Controller/ProductInfo.java");

        boolean noElse =
                source.contains("if (user.isAdmin().equalsIgnoreCase(\"true\"))") &&
                        !source.contains("else");

        assertThat(noElse)
                .as("FINDING: ProductInfo non ha un 'else' per utenti non-admin. "
                        + "Se un utente normale accede, la servlet non fa nulla e "
                        + "restituisce una pagina vuota (possibile information disclosure "
                        + "o broken UX).")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: ProductInfo non gestisce NumberFormatException")
    void testDocumentNumberFormatExceptionInProductInfo() throws Exception {
        String source = readSource("src/main/java/Controller/ProductInfo.java");

        boolean unsafe =
                source.contains("Integer.parseInt(request.getParameter(\"id\"))") &&
                        !source.contains("try {");

        assertThat(unsafe)
                .as("FINDING: ProductInfo chiama Integer.parseInt senza try/catch.")
                .isTrue();
    }

    // ==========================================================
    // 2. SHOWPRODUCT
    // ==========================================================

    @Test
    @DisplayName("ShowProduct: usa doRetrieveById")
    void testShowProductUsesDao() throws Exception {
        String source = readSource("src/main/java/Controller/ShowProduct.java");

        assertThat(source)
                .contains("service.doRetrieveById(productId)");
    }

    @Test
    @DisplayName("FINDING: ShowProduct non verifica che il prodotto esista")
    void testDocumentNullProductInShowProduct() throws Exception {
        String source = readSource("src/main/java/Controller/ShowProduct.java");

        boolean unsafe =
                source.contains("ProductBean product = service.doRetrieveById(productId)") &&
                        !source.contains("product != null") &&
                        !source.contains("product == null");

        assertThat(unsafe)
                .as("FINDING: ShowProduct non verifica che product != null. Se un utente "
                        + "accede con un productId inesistente, la servlet passa null alla "
                        + "JSP /WEB-INF/product.jsp. Se la JSP non gestisce null, "
                        + "potrebbe verificarsi NPE o esporre messaggi di errore sensibili.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: ShowProduct non gestisce NumberFormatException")
    void testDocumentNumberFormatExceptionInShowProduct() throws Exception {
        String source = readSource("src/main/java/Controller/ShowProduct.java");

        boolean unsafe =
                source.contains("Integer.parseInt(request.getParameter(\"productId\"))") &&
                        !source.contains("try {");

        assertThat(unsafe)
                .as("FINDING: ShowProduct chiama Integer.parseInt senza try/catch.")
                .isTrue();
    }

    // ==========================================================
    // 3. SHOWCATALOG
    // ==========================================================

    @Test
    @DisplayName("ShowCatalog: recupera tutte le categorie")
    void testShowCatalogRetrievesCategories() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCatalog.java");

        assertThat(source)
                .contains("serviceCategory.doRetrieveAll()");
    }

    @Test
    @DisplayName("ShowCatalog: recupera tutti i prodotti")
    void testShowCatalogRetrievesAllProducts() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCatalog.java");

        assertThat(source)
                .contains("service.doRetrieveAll()");
    }

    @Test
    @DisplayName("ShowCatalog: forward a catalog.jsp")
    void testShowCatalogForwardsToJsp() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCatalog.java");

        assertThat(source)
                .contains("/WEB-INF/catalog.jsp");
    }

    @Test
    @DisplayName("ShowCatalog: pagina pubblica (nessuna autenticazione richiesta)")
    void testShowCatalogIsPublic() throws Exception {
        String source = readSource("src/main/java/Controller/ShowCatalog.java");

        // Non c'è controllo user.isAdmin() -> OK, catalogo è pubblico
        assertThat(source)
                .as("Il catalogo deve essere accessibile senza login")
                .doesNotContain("user.isAdmin()");
    }

    // ==========================================================
    // 4. SHOWSALES
    // ==========================================================

    @Test
    @DisplayName("ShowSales: recupera solo prodotti in sconto")
    void testShowSalesRetrievesDiscountedProducts() throws Exception {
        String source = readSource("src/main/java/Controller/ShowSales.java");

        assertThat(source)
                .contains("service.doRetrieveSales()");
    }

    @Test
    @DisplayName("ShowSales: forward a sales.jsp")
    void testShowSalesForwardsToJsp() throws Exception {
        String source = readSource("src/main/java/Controller/ShowSales.java");

        assertThat(source)
                .contains("/WEB-INF/sales.jsp");
    }

    @Test
    @DisplayName("ShowSales: pagina pubblica (nessuna autenticazione)")
    void testShowSalesIsPublic() throws Exception {
        String source = readSource("src/main/java/Controller/ShowSales.java");

        assertThat(source)
                .doesNotContain("user.isAdmin()");
    }

    // ==========================================================
    // 5. PRODUCTS HOMEPAGE - Finding CRITICO
    // ==========================================================

    @Test
    @DisplayName("FINDING CRITICO: ProductsHomepage ha loop potenzialmente INFINITO")
    void testDocumentInfiniteLoopInProductsHomepage() throws Exception {
        String source = readSource("src/main/java/Controller/ProductsHomepage.java");

        // Il codice: while (listProduct.size() != 12) con max = doRetrieveAll().size()
        // Se max < 12, il loop non termina mai
        boolean hasInfiniteLoopRisk =
                source.contains("while (listProduct.size() != 12)") &&
                        source.contains("int max = service.doRetrieveAll().size()");

        assertThat(hasInfiniteLoopRisk)
                .as("FINDING CRITICO DoS: ProductsHomepage ha un ciclo 'while (listProduct.size() != 12)' "
                        + "che si basa su 'max = doRetrieveAll().size()'. Se il database "
                        + "contiene MENO DI 12 prodotti, il loop non termina mai "
                        + "(infinite loop) bloccando il thread della servlet "
                        + "(CWE-835: Loop with Unreachable Exit Condition). "
                        + "Questo causa un Denial of Service. "
                        + "Fix: usare 'while (listProduct.size() != 12 && listProduct.size() < max)' "
                        + "o Collections.shuffle() su una lista limitata a 12 elementi.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: ProductsHomepage causa IllegalArgumentException se DB vuoto")
    void testDocumentEmptyDatabaseRisk() throws Exception {
        String source = readSource("src/main/java/Controller/ProductsHomepage.java");

        boolean hasEmptyDbRisk =
                source.contains("rand.nextInt(max)") &&
                        !source.contains("if (max == 0)");

        assertThat(hasEmptyDbRisk)
                .as("FINDING: ProductsHomepage chiama rand.nextInt(max) dove max = "
                        + "doRetrieveAll().size(). Se il database e vuoto, max == 0 e "
                        + "rand.nextInt(0) lancia IllegalArgumentException "
                        + "(CWE-20). Fix: if (max == 0) gestire il caso vuoto.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: ProductsHomepage non gestisce product null nel ciclo")
    void testDocumentNullProductRisk() throws Exception {
        String source = readSource("src/main/java/Controller/ProductsHomepage.java");

        // doRetrieveById può restituire null se l'id random non esiste
        boolean noNullCheck =
                source.contains("listProduct.add(service.doRetrieveById(random))") &&
                        !source.contains("doRetrieveById(random) != null");

        assertThat(noNullCheck)
                .as("FINDING: ProductsHomepage aggiunge il risultato di doRetrieveById(random) "
                        + "alla lista senza verificare null. Se il random id non esiste, "
                        + "viene aggiunto un null alla lista, causando NPE successivamente "
                        + "quando si itera su di essa (CWE-476).")
                .isTrue();
    }

    @Test
    @DisplayName("ProductsHomepage: usa Random per selezione casuale")
    void testProductsHomepageUsesRandom() throws Exception {
        String source = readSource("src/main/java/Controller/ProductsHomepage.java");

        assertThat(source)
                .contains("Random rand = new Random()");
    }

    @Test
    @DisplayName("ProductsHomepage: restituisce JSON")
    void testProductsHomepageReturnsJson() throws Exception {
        String source = readSource("src/main/java/Controller/ProductsHomepage.java");

        assertThat(source)
                .contains("JSONArray")
                .contains("JSONObject");
    }

    @Test
    @DisplayName("ProductsHomepage: pagina pubblica (accessibile senza login)")
    void testProductsHomepageIsPublic() throws Exception {
        String source = readSource("src/main/java/Controller/ProductsHomepage.java");

        assertThat(source)
                .as("La homepage deve essere accessibile senza login")
                .doesNotContain("session.getAttribute(\"user\")");
    }

    // ==========================================================
    // 6. Integrità ProductBean
    // ==========================================================

    @Test
    @DisplayName("ProductBean: campi vengono impostati correttamente")
    void testProductBeanFields() {
        Model.ProductBean product = new Model.ProductBean();
        product.setId(1);
        product.setName("AMD Ryzen");
        product.setPrice(305.00);
        product.setQuantity(12);
        product.setCategory("Processore");

        assertThat(product.getId()).isEqualTo(1);
        assertThat(product.getName()).isEqualTo("AMD Ryzen");
        assertThat(product.getPrice()).isEqualTo(305.00);
        assertThat(product.getQuantity()).isEqualTo(12);
        assertThat(product.getCategory()).isEqualTo("Processore");
    }

    @Test
    @DisplayName("ProductBean: default price e quantity sono 0")
    void testProductBeanDefaults() {
        Model.ProductBean product = new Model.ProductBean();
        assertThat(product.getPrice()).isEqualTo(0.0);
        assertThat(product.getQuantity()).isEqualTo(0);
        assertThat(product.getSales()).isEqualTo(0);
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}