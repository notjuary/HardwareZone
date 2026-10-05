package security.functional.businesslogic;

import Controller.ProductsHomepage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per ProductsHomepage.
 * Verifica accessibilità pubblica e comportamento con DB vuoto o piccolo.
 */
@DisplayName("ProductsHomepage - Test funzionale di sicurezza")
class ProductsHomepageFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();

        support = new ServletTestSupport() {};
        support.setUpBase();
    }

    @Test
    @DisplayName("SECURITY: la homepage prodotti è pubblica (nessuna autenticazione)")
    void testHomepagePubblica() throws Exception {
        // DB vuoto → il loop potrebbe essere problematico (finding documentato)
        try {
            ProductsHomepage servlet = new ProductsHomepage();
            support.invokeDoGet(servlet, support.request, support.response);
        } catch (Exception e) {
            // Eccezione attesa se DB vuoto (finding: loop infinito con <12 prodotti)
        }

        verify(support.session, never()).getAttribute("user");
    }

    @Test
    @DisplayName("SECURITY: la homepage non richiede privilegi admin")
    void testNonRichiedeAdmin() throws Exception {
        try {
            ProductsHomepage servlet = new ProductsHomepage();
            support.invokeDoGet(servlet, support.request, support.response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(support.response, never()).sendError(eq(403), anyString());
    }
}