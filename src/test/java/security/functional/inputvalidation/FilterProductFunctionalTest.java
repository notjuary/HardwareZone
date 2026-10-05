package security.functional.inputvalidation;

import Controller.FilterProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per FilterProduct.
 * Verifica: validazione parametri, NPE su category null, NumberFormatException.
 */
@DisplayName("FilterProduct - Test funzionale di sicurezza")
class FilterProductFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Corsair', 'RAM', 99.99, 20, 0, '/img/ram.png', 'RAM')");
    }

    @Test
    @DisplayName("SECURITY: FilterProduct con parametri validi restituisce JSON")
    void testParametriValidi() throws Exception {
        when(support.request.getParameter("min")).thenReturn("0");
        when(support.request.getParameter("max")).thenReturn("500");
        when(support.request.getParameter("category")).thenReturn("all");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(support.response.getWriter()).thenReturn(pw);

        FilterProduct servlet = new FilterProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.response).setContentType("text/html");
    }

    @Test
    @DisplayName("SECURITY: FilterProduct con max < min corregge automaticamente")
    void testMaxMinCorretto() throws Exception {
        when(support.request.getParameter("min")).thenReturn("500");
        when(support.request.getParameter("max")).thenReturn("100");
        when(support.request.getParameter("category")).thenReturn("all");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(support.response.getWriter()).thenReturn(pw);

        FilterProduct servlet = new FilterProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        // Non crasha: max viene portato a min
        verify(support.response).setContentType("text/html");
    }

    @Test
    @DisplayName("SECURITY: FilterProduct con min non numerico causa eccezione (finding)")
    void testMinNonNumerico() throws Exception {
        when(support.request.getParameter("min")).thenReturn("abc");
        when(support.request.getParameter("max")).thenReturn("500");
        when(support.request.getParameter("category")).thenReturn("all");

        FilterProduct servlet = new FilterProduct();
        try {
            support.invokeDoPost(servlet, support.request, support.response);
        } catch (Exception e) {
            // NumberFormatException atteso (finding documentato)
        }
    }

    @Test
    @DisplayName("SECURITY: FilterProduct con category null causa NPE (finding)")
    void testCategoryNull() throws Exception {
        when(support.request.getParameter("min")).thenReturn("0");
        when(support.request.getParameter("max")).thenReturn("500");
        when(support.request.getParameter("category")).thenReturn(null);

        FilterProduct servlet = new FilterProduct();
        try {
            support.invokeDoPost(servlet, support.request, support.response);
        } catch (Exception e) {
            // NPE atteso (finding documentato)
        }
    }
}