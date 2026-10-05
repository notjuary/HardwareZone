package security.functional.businesslogic;

import Controller.ShowProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per ShowProduct.
 * Verifica accessibilità pubblica e gestione sicura degli id prodotto.
 */
@DisplayName("ShowProduct - Test funzionale di sicurezza")
class ShowProductFunctionalTest extends BaseFunctionalTest {

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
    @DisplayName("SECURITY: ShowProduct è accessibile senza autenticazione")
    void testPubblico() throws Exception {
        when(support.request.getParameter("productId")).thenReturn("1");

        ShowProduct servlet = new ShowProduct();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.response, never()).sendError(eq(403), anyString());
    }

    @Test
    @DisplayName("SECURITY: ShowProduct non espone dati amministrativi")
    void testNonEsponeAdmin() throws Exception {
        when(support.request.getParameter("productId")).thenReturn("1");

        ShowProduct servlet = new ShowProduct();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.session, never()).getAttribute("user");
    }
}