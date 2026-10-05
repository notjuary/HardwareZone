package security.audit.businesslogic;

import Model.CartBean;
import Model.ProductCartBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per la gestione del carrello.
 * Verifica AddToCart e RemoveFromCart.
 *
 * Riferimento: OWASP Testing Guide - OTG-BUSLOGIC, OWASP A04:2021
 */
@DisplayName("Cart Management - AddToCart e RemoveFromCart")
class CartManagementTest {

    private CartBean cart;

    @BeforeEach
    void setUp() {
        cart = new CartBean();
    }

    // ==========================================================
    // 1. ADD TOCART - Validazione parametri
    // ==========================================================

    @Test
    @DisplayName("FINDING: AddToCart non gestisce NumberFormatException su productId")
    void testDocumentNumberFormatExceptionOnProductId() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        boolean unsafe =
                source.contains("Integer.parseInt(request.getParameter(\"productId\"))") &&
                        !source.contains("try {");

        assertThat(unsafe)
                .as("FINDING: AddToCart chiama Integer.parseInt senza try/catch. "
                        + "Se un utente invia 'productId=abc', si verifica "
                        + "NumberFormatException (CWE-20, DoS).")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING CRITICO: AddToCart non gestisce quantity negativa")
    void testDocumentNegativeQuantityAcceptance() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        boolean noValidation =
                source.contains("int quantity = Integer.parseInt(request.getParameter(\"quantity\"))") &&
                        !source.contains("quantity > 0") &&
                        !source.contains("quantity >= 0");

        assertThat(noValidation)
                .as("FINDING CRITICO: AddToCart accetta quantita negative. "
                        + "Un utente puo inviare 'quantity=-100' e il carrello "
                        + "sottrarra 100 unita (o passera il controllo "
                        + "'productBean.getQuantity() >= quantity' sempre vero). "
                        + "CWE-20: Improper Input Validation, OWASP A04:2021. "
                        + "Fix: verificare che quantity > 0 prima di aggiungere.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: AddToCart non gestisce productId inesistente (NPE)")
    void testDocumentNullProductInAddToCart() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        boolean unsafe =
                source.contains("ProductBean productBean = service.doRetrieveById(productId)") &&
                        source.contains("productBean.getQuantity()") &&
                        !source.contains("productBean != null");

        assertThat(unsafe)
                .as("FINDING: AddToCart chiama productBean.getQuantity() senza verificare "
                        + "che productBean != null. Se un utente invia un productId "
                        + "inesistente, si verifica NPE (CWE-476).")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING CRITICO: AddToCart usa sendError(400) senza return")
    void testDocumentMissingReturnAfterSendError() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        // Dopo response.sendError(400) non c'è un return -> l'esecuzione continua
        boolean noReturn =
                source.contains("response.sendError(400)") &&
                        !source.contains("return;");

        assertThat(noReturn)
                .as("FINDING: AddToCart chiama response.sendError(400) ma non esegue "
                        + "'return;'. L'esecuzione prosegue e il codice tenta di "
                        + "salvare il carrello anche se la quantita era insufficiente. "
                        + "Fix: aggiungere 'return;' dopo sendError.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: AddToCart e' un'azione GET (no CSRF)")
    void testDocumentCsrfInAddToCart() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        assertThat(source)
                .as("FINDING: AddToCart espone l'azione via GET. Un sito malevolo "
                        + "puo aggiungere prodotti al carrello di un utente senza il suo "
                        + "consenso tramite un semplice <img src='...add-to-cart-servlet?...'> "
                        + "(CWE-352). Fix: usare POST + token CSRF.")
                .contains("doGet(HttpServletRequest");
    }

    @Test
    @DisplayName("AddToCart: crea un nuovo CartBean se non esiste in sessione")
    void testAddToCartCreatesNewCartIfNull() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        assertThat(source)
                .contains("if (cartBean == null)")
                .contains("cartBean = new CartBean()");
    }

    @Test
    @DisplayName("AddToCart: sincronizza il carrello con il DB se utente loggato")
    void testAddToCartSyncsWithDatabase() throws Exception {
        String source = readSource("src/main/java/Controller/AddToCart.java");

        assertThat(source)
                .contains("if (user != null)")
                .contains("serviceCart.doDelete(user.getId())")
                .contains("serviceCart.doSave(user.getId()");
    }

    // ==========================================================
    // 2. REMOVE FROM CART - Validazione parametri
    // ==========================================================

    @Test
    @DisplayName("FINDING CRITICO: RemoveFromCart usa cart senza null check")
    void testDocumentNullCartInRemoveFromCart() throws Exception {
        String source = readSource("src/main/java/Controller/RemoveFromCart.java");

        boolean unsafe =
                source.contains("CartBean cart = (CartBean) session.getAttribute(\"cart\")") &&
                        source.contains("cart.removeProduct(id)") &&
                        !source.contains("cart == null") &&
                        !source.contains("cart != null");

        assertThat(unsafe)
                .as("FINDING CRITICO: RemoveFromCart chiama cart.removeProduct(id) senza "
                        + "verificare che cart != null. Se un utente accede a "
                        + "/remove-from-cart-servlet prima di aver mai aggiunto un prodotto "
                        + "al carrello, si verifica NPE (CWE-476). "
                        + "Fix: if (cart == null) cart = new CartBean();")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: RemoveFromCart non gestisce NumberFormatException")
    void testDocumentNumberFormatExceptionInRemoveFromCart() throws Exception {
        String source = readSource("src/main/java/Controller/RemoveFromCart.java");

        boolean unsafe =
                source.contains("Integer.parseInt(request.getParameter(\"productId\"))") &&
                        !source.contains("try {");

        assertThat(unsafe)
                .as("FINDING: RemoveFromCart chiama Integer.parseInt senza try/catch.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: RemoveFromCart e' un'azione GET (no CSRF)")
    void testDocumentCsrfInRemoveFromCart() throws Exception {
        String source = readSource("src/main/java/Controller/RemoveFromCart.java");

        assertThat(source)
                .as("FINDING: RemoveFromCart espone l'azione via GET.")
                .contains("doGet(HttpServletRequest");
    }

    @Test
    @DisplayName("RemoveFromCart: sincronizza il carrello con il DB se utente loggato")
    void testRemoveFromCartSyncsWithDatabase() throws Exception {
        String source = readSource("src/main/java/Controller/RemoveFromCart.java");

        assertThat(source)
                .contains("if (user != null)")
                .contains("serviceCart.doDelete(user.getId())");
    }

    // ==========================================================
    // 3. LOGICA CARRELLO - Integrità
    // ==========================================================

    @Test
    @DisplayName("CartBean: rimuovere un prodotto inesistente causa bug (documentato)")
    void testCartRemoveNonExistentBug() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 5);

        // BUG: removeProduct rimuove l'ultimo elemento se l'id non esiste
        cart.removeProduct(999);

        assertThat(cart.getCartList())
                .as("BUG: RemoveFromCart con id inesistente rimuove un prodotto non richiesto")
                .hasSize(1);
    }

    @Test
    @DisplayName("CartBean: aggiungere quantita multiple mantiene coerenza")
    void testCartMultipleAddsKeepConsistency() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 5);
        cart.addProduct(1, 2);

        assertThat(cart.getCartList()).hasSize(2);
        assertThat(cart.getNumberObject()).isEqualTo(10);
    }

    @Test
    @DisplayName("CartBean: setNumberObject e getNumberObject funzionano")
    void testCartSetNumberObject() {
        cart.setNumberObject(42);
        assertThat(cart.getNumberObject()).isEqualTo(42);
    }

    @Test
    @DisplayName("ProductCartBean: id e quantity corretti")
    void testProductCartBeanFields() {
        ProductCartBean pcb = new ProductCartBean();
        pcb.setId(7);
        pcb.setQuantity(99);

        assertThat(pcb.getId()).isEqualTo(7);
        assertThat(pcb.getQuantity()).isEqualTo(99);
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}