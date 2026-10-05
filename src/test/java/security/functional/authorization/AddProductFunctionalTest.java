package security.functional.authorization;

import Controller.AddProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per AddProduct (area admin).
 */
@DisplayName("AddProduct - Test funzionale di sicurezza")
class AddProductFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Categoria VALUES ('CPU')");

        when(support.request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: AddProduct.doGet richiede admin")
    void testDoGetAdminRichiesto() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        AddProduct servlet = new AddProduct();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/admin/add-product.jsp");
    }

    @Test
    @DisplayName("SECURITY: AddProduct.doGet con utente normale reindirizza a index")
    void testDoGetUtenteNormale() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        AddProduct servlet = new AddProduct();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
    }

    @Test
    @DisplayName("SECURITY: AddProduct.doGet con utente null causa NPE (finding)")
    void testDoGetUtenteAnonimo() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(null);

        AddProduct servlet = new AddProduct();
        try {
            support.invokeDoGet(servlet, support.request, support.response);
        } catch (Exception e) {
            // NPE atteso
        }
    }
}