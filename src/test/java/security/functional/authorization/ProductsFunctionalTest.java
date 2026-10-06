package security.functional.authorization;

import Controller.Products;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Products (area admin).
 * Verifica il controllo di autorizzazione e la gestione di utenti anonimi.
 */
@DisplayName("Products - Test funzionale di sicurezza")
class ProductsFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Admin','Test','1990-01-01','admin@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','true')");
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");

        when(support.request.getRequestDispatcher("/WEB-INF/results/products.jsp"))
                .thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp"))
                .thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: Products richiede admin per accedere")
    void testAdminRichiesto() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        Products servlet = new Products();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/results/products.jsp");
        verify(support.dispatcher).forward(support.request, support.response);
    }

    @Test
    @DisplayName("SECURITY: Products reindirizza utente normale a index.jsp")
    void testUtenteNormaleReindirizzato() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        Products servlet = new Products();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
        verify(support.dispatcher).include(support.request, support.response);
        verify(support.request, never()).getRequestDispatcher("/WEB-INF/results/products.jsp");
    }

    @Test
    @DisplayName("SECURITY: Products causa NPE se utente non loggato (finding)")
    void testUtenteAnonimo() {
        when(support.session.getAttribute("user")).thenReturn(null);

        Products servlet = new Products();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso: user.isAdmin() senza null check");
    }

    @Test
    @DisplayName("SECURITY: Products carica i prodotti solo per admin")
    void testAdminVedeTuttiProdotti() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        Products servlet = new Products();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).setAttribute(eq("products"), any());
    }
}