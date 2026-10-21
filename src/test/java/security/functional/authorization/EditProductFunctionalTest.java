package security.functional.authorization;

import Controller.EditProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per EditProduct.
 */
@DisplayName("EditProduct - Test funzionale di sicurezza")
class EditProductFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");

        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: EditProduct richiede admin")
    void testAdminRichiesto() throws Exception {
        // Uso un utente normale per verificare che venga respinto
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        EditProduct servlet = new EditProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
    }

    // QUESTO È IL TEST CHE CHIEDEVI: va messo qui!
    @Test
    @DisplayName("FIXED: EditProduct gestisce utente anonimo senza NPE")
    void testUtenteAnonimo() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(null);

        EditProduct servlet = new EditProduct();

        // FIX: non deve più lanciare NPE
        assertDoesNotThrow(() ->
                support.invokeDoPost(servlet, support.request, support.response)
        );

        // Deve reindirizzare alla index.jsp
        verify(support.request).getRequestDispatcher("index.jsp");
    }

    @Test
    @DisplayName("FIXED: EditProduct con admin non crasha su input invalido")
    void testAdminInputInvalido() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());
        when(support.request.getParameter("id")).thenReturn("1");
        when(support.request.getParameter("name")).thenReturn("");
        when(support.request.getParameter("description")).thenReturn("");
        when(support.request.getParameter("price")).thenReturn("abc");
        when(support.request.getParameter("quantity")).thenReturn("x");
        when(support.request.getParameter("sales")).thenReturn("y");
        when(support.request.getParameter("category")).thenReturn("");

        EditProduct servlet = new EditProduct();

        // FIX: Usa invokeDoPost, non invokeDoGet! E non lancia eccezioni.
        assertDoesNotThrow(() ->
                support.invokeDoPost(servlet, support.request, support.response)
        );

        // Verifica che venga mostrata la pagina di errore (grazie all'early return)
        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }
}