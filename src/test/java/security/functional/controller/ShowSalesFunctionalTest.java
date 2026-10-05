package security.functional.controller;

import Controller.ShowSales;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per ShowSales con H2.
 * Verifica:
 *  - la pagina sconti è pubblica
 *  - vengono caricate categorie e prodotti in sconto
 *  - non vengono esposti dati amministrativi
 */
@DisplayName("ShowSales - Test funzionale di sicurezza")
class ShowSalesFunctionalTest extends BaseFunctionalTest {

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
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 30, '/img/ryzen.png', 'CPU')");
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Corsair 16GB', 'RAM DDR4', 79.99, 20, 0, '/img/ram.png', 'RAM')");

        when(support.request.getRequestDispatcher("/WEB-INF/sales.jsp"))
                .thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: la pagina sconti è accessibile senza autenticazione")
    void testPaginaScontiPubblica() throws Exception {
        ShowSales servlet = new ShowSales();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/sales.jsp");
        verify(support.dispatcher).forward(support.request, support.response);
    }

    @Test
    @DisplayName("SECURITY: la pagina sconti carica categorie e prodotti")
    void testCaricaCategorieEProdotti() throws Exception {
        ShowSales servlet = new ShowSales();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("categories"), any());
        verify(support.request).setAttribute(eq("products"), any());
    }

    @Test
    @DisplayName("SECURITY: la pagina sconti non espone dati amministrativi")
    void testNonEsponeDatiAdmin() throws Exception {
        ShowSales servlet = new ShowSales();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request, never()).setAttribute(eq("users"), any());
        verify(support.request, never()).setAttribute(eq("admin"), any());
    }

    @Test
    @DisplayName("SECURITY: la pagina sconti non richiede privilegi admin")
    void testNonRichiedeAdmin() throws Exception {
        ShowSales servlet = new ShowSales();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.session, never()).getAttribute("user");
    }
}