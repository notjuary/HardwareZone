package security.functional.businesslogic;

import Controller.AddToCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per AddToCart.
 */
@DisplayName("AddToCart - Test funzionale di sicurezza")
class AddToCartFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");
    }

    @Test
    @DisplayName("SECURITY: AddToCart aggiunge prodotto valido")
    void testAggiungiProdottoValido() throws Exception {
        when(support.request.getParameter("productId")).thenReturn("1");
        when(support.request.getParameter("quantity")).thenReturn("2");

        AddToCart servlet = new AddToCart();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("cart"), any());
    }

    @Test
    @DisplayName("SECURITY: AddToCart con quantity negativa (finding documentato)")
    void testQuantityNegativa() throws Exception {
        when(support.request.getParameter("productId")).thenReturn("1");
        when(support.request.getParameter("quantity")).thenReturn("-5");

        AddToCart servlet = new AddToCart();
        support.invokeDoGet(servlet, support.request, support.response);

        // Il codice accetta quantity negative → finding
        verify(support.session).setAttribute(eq("cart"), any());
    }

    @Test
    @DisplayName("SECURITY: AddToCart con productId inesistente causa NPE (finding)")
    void testProdottoInesistente() throws Exception {
        when(support.request.getParameter("productId")).thenReturn("999");
        when(support.request.getParameter("quantity")).thenReturn("1");

        AddToCart servlet = new AddToCart();
        try {
            support.invokeDoGet(servlet, support.request, support.response);
        } catch (Exception e) {
            // NPE atteso su productBean.getQuantity()
        }
    }
}