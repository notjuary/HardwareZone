package security.functional.businesslogic;

import Controller.ShowCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per ShowCatalog con H2.
 */
@DisplayName("ShowCatalog - Test funzionale di sicurezza")
class ShowCatalogFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();

        support = new ServletTestSupport() {};
        support.setUpBase();

        // Inserisci dati di test
        executeSql("INSERT INTO Categoria VALUES ('CPU')");
        executeSql("INSERT INTO Categoria VALUES ('RAM')");
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");

        when(support.request.getRequestDispatcher("/WEB-INF/catalog.jsp"))
                .thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: il catalogo è accessibile senza autenticazione")
    void testCatalogoPubblico() throws Exception {
        ShowCatalog servlet = new ShowCatalog();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/catalog.jsp");
        verify(support.dispatcher).forward(support.request, support.response);
    }

    @Test
    @DisplayName("SECURITY: il catalogo carica i dati correttamente")
    void testCatalogoCaricaDati() throws Exception {
        ShowCatalog servlet = new ShowCatalog();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("categories"), any());
        verify(support.request).setAttribute(eq("products"), any());
    }

    @Test
    @DisplayName("SECURITY: il catalogo non espone dati amministrativi")
    void testCatalogoNonEsponeAdmin() throws Exception {
        ShowCatalog servlet = new ShowCatalog();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request, never()).setAttribute(eq("users"), any());
        verify(support.request, never()).setAttribute(eq("admin"), any());
    }

    @Test
    @DisplayName("SECURITY: il catalogo non richiede privilegi admin")
    void testCatalogoNonRichiedeAdmin() throws Exception {
        ShowCatalog servlet = new ShowCatalog();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.session, never()).getAttribute("user");
    }
}