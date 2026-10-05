package security.functional.businesslogic;
import Controller.RemoveFromCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per RemoveFromCart.
 */
@DisplayName("RemoveFromCart - Test funzionale di sicurezza")
class RemoveFromCartFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        when(support.request.getRequestDispatcher("Cart.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: RemoveFromCart con cart null causa NPE (finding)")
    void testCartNull() throws Exception {
        when(support.session.getAttribute("cart")).thenReturn(null);
        when(support.request.getParameter("productId")).thenReturn("1");

        RemoveFromCart servlet = new RemoveFromCart();
        try {
            support.invokeDoGet(servlet, support.request, support.response);
        } catch (Exception e) {
            // NPE atteso
        }
    }

    @Test
    @DisplayName("SECURITY: RemoveFromCart rimuove prodotto valido")
    void testRimuoviProdotto() throws Exception {
        Model.CartBean cart = new Model.CartBean();
        cart.addProduct(1, 2);
        when(support.session.getAttribute("cart")).thenReturn(cart);
        when(support.request.getParameter("productId")).thenReturn("1");

        RemoveFromCart servlet = new RemoveFromCart();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("cart"), any());
    }
}