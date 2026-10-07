package security.functional.businesslogic;

import Controller.AddToCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per AddToCart.
 * I finding sono stati risolti e i test ora verificano il fix.
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
    @DisplayName("FIXED: AddToCart con quantity negativa rifiutata (sendError 400)")
    void testQuantityNegativa() throws Exception {
        when(support.request.getParameter("productId")).thenReturn("1");
        when(support.request.getParameter("quantity")).thenReturn("-5");

        AddToCart servlet = new AddToCart();
        support.invokeDoGet(servlet, support.request, support.response);

        // FIX: la quantity negativa ora viene rifiutata con sendError(400)
        verify(support.response).sendError(400);

        // FIX: il carrello NON deve essere settato in sessione
        verify(support.session, never()).setAttribute(eq("cart"), any());
    }

    @Test
    @DisplayName("FIXED: AddToCart con productId inesistente gestito con sendError(404)")
    void testProdottoInesistente() throws Exception {
        when(support.request.getParameter("productId")).thenReturn("999");
        when(support.request.getParameter("quantity")).thenReturn("1");

        AddToCart servlet = new AddToCart();

        // FIX: non deve più lanciare NPE
        assertDoesNotThrow(() ->
                support.invokeDoGet(servlet, support.request, support.response));

        // FIX: il servlet risponde con 404
        verify(support.response).sendError(404);

        // FIX: il carrello NON deve essere settato in sessione
        verify(support.session, never()).setAttribute(eq("cart"), any());
    }
}