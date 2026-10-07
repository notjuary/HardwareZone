package security.functional.businesslogic;

import Controller.ProductInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per ProductInfo.
 * Verifica autorizzazione admin e comportamento con utente anonimo.
 */
@DisplayName("ProductInfo - Test funzionale di sicurezza")
class ProductInfoFunctionalTest extends BaseFunctionalTest {

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
    @DisplayName("SECURITY: ProductInfo causa NPE se utente non loggato (finding documentato)")
    void testUtenteAnonimoCausaNPE()  {
        when(support.session.getAttribute("user")).thenReturn(null);
        when(support.request.getParameter("id")).thenReturn("1");

        ProductInfo servlet = new ProductInfo();

        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso: finding documentato");
    }

    @Test
    @DisplayName("SECURITY: ProductInfo con admin carica i dati")
    void testAdminCaricaDati() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("1");

        ProductInfo servlet = new ProductInfo();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("productJSP"), any());
        verify(support.request).setAttribute(eq("categories"), any());
    }
}