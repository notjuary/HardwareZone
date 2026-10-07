package security.audit.businesslogic;

import Model.CartBean;
import Model.OrderBean;
import Model.ProductBean;
import Model.ProductCartBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per l'area "Business Logic".
 * Verifica che le regole di business non possano essere aggirate
 * e documenta comportamenti anomali trovati nel codice.
 *
 * Riferimento: OWASP Testing Guide - OTG-BUSLOGIC
 */
@DisplayName("Business Logic - Carrello, Ordini e Prodotti")
class BusinessLogicTest {

    private CartBean cart;

    @BeforeEach
    void setUp() {
        cart = new CartBean();
    }

    // ==========================================================
    // 1. CartBean - addProduct
    // ==========================================================

    @Test
    @DisplayName("Aggiungere un nuovo prodotto lo inserisce con la quantita corretta")
    void testAddNewProductToCart() {
        cart.addProduct(1, 3);

        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getCartList().get(0).getId()).isEqualTo(1);
        assertThat(cart.getCartList().get(0).getQuantity()).isEqualTo(3);
        assertThat(cart.getNumberObject()).isEqualTo(3);
    }

    @Test
    @DisplayName("Aggiungere lo stesso prodotto due volte somma le quantita")
    void testAddSameProductIncrementsQuantity() {
        cart.addProduct(1, 3);
        cart.addProduct(1, 2);

        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getCartList().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.getNumberObject()).isEqualTo(5);
    }

    @Test
    @DisplayName("Aggiungere prodotti diversi li tiene separati")
    void testAddDifferentProductsAreSeparate() {
        cart.addProduct(1, 2);
        cart.addProduct(2, 5);

        assertThat(cart.getCartList()).hasSize(2);
        assertThat(cart.getNumberObject()).isEqualTo(7);
    }

    @Test
    @DisplayName("Aggiungere un prodotto con quantita zero non incrementa numberObject")
    void testAddProductWithZeroQuantity() {
        cart.addProduct(1, 0);

        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getNumberObject()).isEqualTo(0);
    }

    // ==========================================================
    // 2. CartBean - removeProduct
    // ==========================================================

    @Test
    @DisplayName("Rimuovere un prodotto esistente lo elimina dal carrello")
    void testRemoveExistingProduct() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 2);

        cart.removeProduct(1);

        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getCartList().get(0).getId()).isEqualTo(2);
        assertThat(cart.getNumberObject()).isEqualTo(2);
    }

    @Test
    @DisplayName("BUG DOCUMENTATO: removeProduct con id inesistente rimuove l'ultimo elemento")
    void testRemoveNonExistentProductIsBuggy() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 5);

        // Rimuoviamo un id che NON esiste
        cart.removeProduct(999);

        // BUG: la lista non dovrebbe cambiare, ma il codice attuale
        // rimuove comunque l'elemento in posizione (size - 1)
        assertThat(cart.getCartList())
                .as("BUG: removeProduct rimuove l'ultimo elemento se l'id non esiste. "
                        + "Fix consigliato: usare iteratore con remove() o return esplicito.")
                .hasSize(1);
    }

    @Test
    @DisplayName("Rimuovere tutti i prodotti svuota il carrello")
    void testRemoveAllProducts() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 5);

        cart.removeProduct(1);
        cart.removeProduct(2);

        assertThat(cart.getCartList()).isEmpty();
        assertThat(cart.getNumberObject()).isEqualTo(0);
    }

    // ==========================================================
    // 3. CartBean - setCartList
    // ==========================================================

    @Test
    @DisplayName("setCartList calcola numberObject dalla lista")
    void testSetCartListComputesNumberObject() {
        ProductCartBean p1 = new ProductCartBean();
        p1.setId(1);
        p1.setQuantity(3);

        ProductCartBean p2 = new ProductCartBean();
        p2.setId(2);
        p2.setQuantity(7);

        ArrayList<ProductCartBean> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);

        cart.setCartList(list);

        assertThat(cart.getNumberObject()).isEqualTo(10);
        assertThat(cart.getCartList()).hasSize(2);
    }

    // ==========================================================
    // 4. ProductBean - Validazione prezzi e quantita
    // ==========================================================

    @Test
    @DisplayName("ProductBean: prezzo zero e valido")
    void testProductWithZeroPrice() {
        ProductBean product = new ProductBean();
        product.setPrice(0.0);
        assertThat(product.getPrice()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("ProductBean: prezzo negativo e accettato dal bean (validazione a livello DAO)")
    void testProductWithNegativePriceIsAcceptedByBean() {
        ProductBean product = new ProductBean();
        product.setPrice(-10.0);

        // Il Bean non valida: il valore viene memorizzato cosi com'e.
        // La validazione deve avvenire nel controller o nel DAO.
        assertThat(product.getPrice()).isEqualTo(-10.0);
    }

    @Test
    @DisplayName("ProductBean: quantita zero e valida (prodotto non disponibile)")
    void testProductWithZeroQuantity() {
        ProductBean product = new ProductBean();
        product.setQuantity(0);
        assertThat(product.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("ProductBean: quantita negativa e accettata dal bean (validazione a livello DAO)")
    void testProductWithNegativeQuantityIsAcceptedByBean() {
        ProductBean product = new ProductBean();
        product.setQuantity(-5);
        assertThat(product.getQuantity()).isEqualTo(-5);
    }

    // ==========================================================
    // 5. OrderBean - Totali
    // ==========================================================

    @Test
    @DisplayName("OrderBean: totale positivo e valido")
    void testOrderWithPositiveTotal() {
        OrderBean order = new OrderBean();
        order.setTotal(99.99);
        assertThat(order.getTotal()).isEqualTo(99.99);
    }

    @Test
    @DisplayName("OrderBean: totale zero e valido")
    void testOrderWithZeroTotal() {
        OrderBean order = new OrderBean();
        order.setTotal(0.0);
        assertThat(order.getTotal()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("OrderBean: totale negativo e accettato dal bean (validazione a livello business)")
    void testOrderWithNegativeTotalIsAcceptedByBean() {
        OrderBean order = new OrderBean();
        order.setTotal(-50.0);
        assertThat(order.getTotal()).isEqualTo(-50.0);
    }

    // ==========================================================
    // 6. Integrita del carrello
    // ==========================================================

    @Test
    @DisplayName("Il numero di oggetti nel carrello corrisponde alla somma delle quantita")
    void testNumberObjectMatchesSumOfQuantities() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 4);
        cart.addProduct(3, 5);

        int sum = cart.getCartList().stream()
                .mapToInt(ProductCartBean::getQuantity)
                .sum();

        assertThat(cart.getNumberObject()).isEqualTo(sum);
        assertThat(cart.getNumberObject()).isEqualTo(12);
    }

    @Test
    @DisplayName("Il carrello non deve contenere duplicati dello stesso prodotto")
    void testCartHasNoDuplicates() {
        cart.addProduct(1, 2);
        cart.addProduct(1, 3);
        cart.addProduct(1, 4);

        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getCartList().get(0).getQuantity()).isEqualTo(9);
    }

    // ==========================================================
    // 7. DOCUMENTAZIONE - Problemi trovati in ProductDAO
    // ==========================================================

    @Test
    @DisplayName("REGRESSION: ProductDAO.doUpdate usa PreparedStatement (SQL Injection fixata)")
    void testProductDAOUpdateUsesPreparedStatement() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/java/Model/ProductDAO.java")));

        // Verifica che NON usi più Statement (vulnerabile)
        assertThat(source)
                .as("ProductDAO.doUpdate NON deve usare con.createStatement() (SQL Injection)")
                .doesNotContain("con.createStatement()");

        // Verifica che usi PreparedStatement (sicuro)
        assertThat(source)
                .as("ProductDAO.doUpdate deve usare PreparedStatement")
                .contains("PreparedStatement");

        // Verifica che usi placeholder parametrizzati (?)
        assertThat(source)
                .as("ProductDAO.doUpdate deve usare placeholder (?) invece di concatenazione")
                .contains("UPDATE Prodotto SET Nome = ?");
    }

    @Test
    @DisplayName("FINDING: ProductDAO.doRetrieveByFilter usa > e < invece di >= e <=")
    void testDocumentFilterBoundaryBug() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/java/Model/ProductDAO.java")));

        // Il filtro esclude i prodotti al prezzo esatto del min/max
        boolean hasStrictComparison =
                source.contains("Prezzo > ?") &&
                        source.contains("Prezzo < ?");

        assertThat(hasStrictComparison)
                .as("BUG FUNZIONALE: doRetrieveByFilter usa confronti stretti (> e <) "
                        + "invece di (>= e <=). I prodotti al prezzo esatto min/max "
                        + "vengono esclusi dai risultati.")
                .isTrue();
    }
}