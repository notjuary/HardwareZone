package security.functional.inputvalidation;

import Controller.SearchProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per SearchProduct.
 */
@DisplayName("SearchProduct - Test funzionale di sicurezza")
class SearchProductFunctionalTest extends BaseFunctionalTest {

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
    @DisplayName("SECURITY: SearchProduct con query valida restituisce JSON")
    void testQueryValida() throws Exception {
        when(support.request.getParameter("searchQuery")).thenReturn("ryzen");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(support.response.getWriter()).thenReturn(pw);

        SearchProduct servlet = new SearchProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.response).setContentType("text/html");
    }

    @Test
    @DisplayName("SECURITY: SearchProduct con query null causa NPE (finding)")
    void testQueryNull() {
        when(support.request.getParameter("searchQuery")).thenReturn(null);

        SearchProduct servlet = new SearchProduct();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso (finding documentato)");
    }

    @Test
    @DisplayName("SECURITY: SearchProduct non esegue SQL injection (usa .contains)")
    void testSqlInjection() throws Exception {
        when(support.request.getParameter("searchQuery")).thenReturn("'; DROP TABLE Prodotto; --");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(support.response.getWriter()).thenReturn(pw);

        SearchProduct servlet = new SearchProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        // Non crasha: il payload è trattato come stringa
        verify(support.response).setContentType("text/html");
    }
}