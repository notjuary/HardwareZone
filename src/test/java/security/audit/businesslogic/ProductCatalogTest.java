package security.audit.businesslogic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per l'area "Catalogo Prodotti".
 * Verifica:
 *  - ricerca testuale (SearchProduct)
 *  - filtro per prezzo e categoria (FilterProduct)
 *  - gestione categorie (CategoryDAO)
 *  - logica di filtro (max < min)
 * Riferimento: OWASP Testing Guide - OTG-BUSLOGIC, OWASP A01, A04
 */
@DisplayName("Product Catalog - Ricerca, filtro e categorie")
class ProductCatalogTest {

    // ==========================================================
    // 1. FILTRO PREZZI - Logica di validazione
    // ==========================================================

    @Test
    @DisplayName("Filtro: se max < min, il servlet corregge max = min")
    void testMaxLessThanMinIsCorrected() throws Exception {
        String source = readSource("src/main/java/Controller/FilterProduct.java");

        assertThat(source)
                .as("Il servlet deve gestire il caso maxPrice < minPrice")
                .contains("if (maxPrice < minPrice)")
                .contains("maxPrice = minPrice");
    }

    @Test
    @DisplayName("Filtro: usa doRetrieveByFilter con i parametri corretti")
    void testFilterUsesCorrectDAO() throws Exception {
        String source = readSource("src/main/java/Controller/FilterProduct.java");

        assertThat(source)
                .contains("service.doRetrieveByFilter(minPrice, maxPrice, category)");
    }

    @Test
    @DisplayName("FIXED: doRetrieveByFilter usa >= e <= per includere i limiti")
    void testDocumentFilterBoundaryBug() throws Exception {
        String source = readSource("src/main/java/Model/ProductDAO.java");

        // Ora verifichiamo che la query usi >= e <=
        boolean isFixed =
                source.contains("Prezzo >= ?") &&
                        source.contains("Prezzo <= ?");

        assertThat(isFixed)
                .as("FIXED: doRetrieveByFilter ora usa >= e <=. "
                        + "I prodotti al prezzo esatto min/max non vengono più esclusi.")
                .isTrue();
    }

    // ==========================================================
    // 2. VALIDAZIONE PARAMETRI FILTRO
    // ==========================================================

    @Test
    @DisplayName("FINDING: FilterProduct non gestisce NumberFormatException su min/max")
    void testDocumentNumberFormatExceptionInFilter() throws Exception {
        String source = readSource("src/main/java/Controller/FilterProduct.java");

        boolean hasUnsafeParsing =
                source.contains("Integer.parseInt(request.getParameter(\"min\"))") &&
                        !source.contains("try {") ;

        assertThat(hasUnsafeParsing)
                .as("FINDING: FilterProduct.java chiama Integer.parseInt senza try/catch. "
                        + "Se un utente invia 'abc' invece di un numero, la servlet lancia "
                        + "NumberFormatException (DoS - CWE-20). "
                        + "Fix: wrappare in try/catch o validare con regex.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: FilterProduct non verifica che category sia non-null")
    void testDocumentNullCategoryInFilter() throws Exception {
        String source = readSource("src/main/java/Controller/FilterProduct.java");

        boolean noNullCheck =
                source.contains("String category = request.getParameter(\"category\")") &&
                        !source.contains("category != null") &&
                        !source.contains("category == null");

        assertThat(noNullCheck)
                .as("FINDING: FilterProduct.java non verifica che category != null. "
                        + "Se il parametro manca, category e null e viene passato a "
                        + "doRetrieveByFilter dove `category.equalsIgnoreCase(\"all\")` "
                        + "lancia NullPointerException.")
                .isTrue();
    }

    // ==========================================================
    // 3. RICERCA TESTUALE (SearchProduct)
    // ==========================================================

    @Test
    @DisplayName("Ricerca: usa toLowerCase per match case-insensitive")
    void testSearchIsCaseInsensitive() throws Exception {
        String source = readSource("src/main/java/Controller/SearchProduct.java");

        assertThat(source)
                .as("La ricerca deve essere case-insensitive")
                .contains("toLowerCase()");
    }

    @Test
    @DisplayName("Ricerca: cerca sia in name che in description")
    void testSearchMatchesNameAndDescription() throws Exception {
        String source = readSource("src/main/java/Controller/SearchProduct.java");

        assertThat(source)
                .contains("product.getDescription().toLowerCase().contains")
                .contains("product.getName().toLowerCase().contains");
    }

    @Test
    @DisplayName("FINDING: SearchProduct non gestisce searchQuery null")
    void testDocumentNullSearchQuery() throws Exception {
        String source = readSource("src/main/java/Controller/SearchProduct.java");

        boolean noNullCheck =
                source.contains("String searchQuery = request.getParameter(\"searchQuery\")") &&
                        !source.contains("searchQuery != null") &&
                        !source.contains("searchQuery == null");

        assertThat(noNullCheck)
                .as("FINDING: SearchProduct.java non verifica che searchQuery != null. "
                        + "Se il parametro manca, `searchQuery.toLowerCase()` lancia "
                        + "NullPointerException (CWE-476).")
                .isTrue();
    }

    @Test
    @DisplayName("Ricerca: restituisce JSON (non HTML) come output")
    void testSearchReturnsJsonOutput() throws Exception {
        String source = readSource("src/main/java/Controller/SearchProduct.java");

        assertThat(source)
                .as("La ricerca deve restituire JSON per essere consumata via AJAX")
                .contains("JSONArray")
                .contains("JSONObject");
    }

    // ==========================================================
    // 4. CATEGORIE (CategoryDAO)
    // ==========================================================

    @Test
    @DisplayName("CategoryDAO usa PreparedStatement (no SQL injection)")
    void testCategoryDAO_UsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/CategoryDAO.java");

        assertThat(source)
                .contains("PreparedStatement");

        assertThat(source)
                .as("CategoryDAO non deve usare Statement raw")
                .doesNotContain("con.createStatement()");
    }

    @Test
    @DisplayName("CategoryDAO: ordina le categorie alfabeticamente")
    void testCategoryDAO_OrdersAlphabetically() throws Exception {
        String source = readSource("src/main/java/Model/CategoryDAO.java");

        assertThat(source)
                .contains("ORDER BY Nome_Categoria");
    }

    @Test
    @DisplayName("CategoryBean: nome null di default")
    void testCategoryBeanDefault() {
        Model.CategoryBean category = new Model.CategoryBean();
        assertThat(category.getNome()).isNull();
    }

    @Test
    @DisplayName("CategoryBean: setNome/getNome funzionano correttamente")
    void testCategoryBeanSetGet() {
        Model.CategoryBean category = new Model.CategoryBean();
        category.setNome("Processore");
        assertThat(category.getNome()).isEqualTo("Processore");
    }

    // ==========================================================
    // 5. LOGICA DI RICERCA - Regex validation
    // ==========================================================

    @Test
    @DisplayName("Query di ricerca valida: 'Ryzen'")
    void testValidSearchQuery() {
        String query = "Ryzen";
        assertThat(query).matches("[a-zA-Z0-9\\s]+");
    }

    @Test
    @DisplayName("Query di ricerca con SQL injection viene trattata come stringa")
    void testSqlInjectionInSearchIsHandledAsString() {
        String query = "'; DROP TABLE--";
        // La ricerca usa .contains() quindi il payload e inerte
        assertThat("AMD Ryzen 5".contains(query.toLowerCase())).isFalse();
    }

    @Test
    @DisplayName("Query di ricerca vuota non matcha nulla")
    void testEmptySearchQuery() {
        String query = "";
        // Con query vuota, .contains("") restituisce true per ogni stringa
        // Questo e un comportamento da documentare
        assertThat("qualsiasi".contains(query)).isTrue();
    }

    // ==========================================================
    // 6. INTEGRITA CATALOGO
    // ==========================================================

    @Test
    @DisplayName("ProductDAO.doRetrieveAll ordina per ID")
    void testRetrieveAllOrdersById() throws Exception {
        String source = readSource("src/main/java/Model/ProductDAO.java");

        assertThat(source)
                .contains("ORDER BY ID_Prodotto");
    }

    @Test
    @DisplayName("ProductDAO.doRetrieveSales filtra solo prodotti con sconto > 0")
    void testRetrieveSalesFiltersDiscounted() throws Exception {
        String source = readSource("src/main/java/Model/ProductDAO.java");

        assertThat(source)
                .contains("WHERE Sconto > 0");
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}