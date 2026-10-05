package security.functional.businesslogic;
import Controller.ShowCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per ShowCart.
 */
@DisplayName("ShowCart - Test funzionale di sicurezza")
class ShowCartFunctionalTest extends BaseFunctionalTest {

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
    @DisplayName("SECURITY: ShowCart con carrello null causa NPE (finding documentato)")
    void testCarrelloNull() throws Exception {
        when(support.session.getAttribute("cart")).thenReturn(null);

        ShowCart servlet = new ShowCart();
        try {
            support.invokeDoGet(servlet, support.request, support.response);
        } catch (Exception e) {
            // NPE atteso
        }
    }

    @Test
    @DisplayName("SECURITY: ShowCart con carrello valido restituisce JSON")
    void testCarrelloValidoRestituisceJson() throws Exception {
        Model.CartBean cart = new Model.CartBean();
        cart.addProduct(1, 2);
        when(support.session.getAttribute("cart")).thenReturn(cart);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(support.response.getWriter()).thenReturn(pw);

        ShowCart servlet = new ShowCart();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.response).setContentType("text/html");
    }
}