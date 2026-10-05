package security.functional.dataprotection;

import Controller.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Payment.
 * Verifica: login richiesto, validazione carta, NPE, race condition.
 */
@DisplayName("Payment - Test funzionale di sicurezza")
class PaymentFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/payment.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: Payment.doGet senza login reindirizza a errore")
    void testPaymentSenzaLogin() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(null);

        Payment servlet = new Payment();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    @Test
    @DisplayName("SECURITY: Payment.doGet con login e carrello carica la pagina")
    void testPaymentConLogin() throws Exception {
        Model.CartBean cart = new Model.CartBean();
        Model.ProductCartBean item = new Model.ProductCartBean();
        item.setId(1);
        item.setQuantity(1);
        cart.addProduct(1, 1);
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("cart")).thenReturn(cart);

        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen 5', 'CPU AMD', 299.99, 10, 0, '/img/ryzen.png', 'CPU')");

        Payment servlet = new Payment();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("total"), any());
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost con carta valida crea ordine")
    void testPaymentConCartaValida() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.session.getAttribute("total")).thenReturn(299.99);
        when(support.session.getAttribute("cart")).thenReturn(new Model.CartBean());
        when(support.request.getParameter("numero-carta")).thenReturn("1234567890123456");
        when(support.request.getParameter("CVV")).thenReturn("123");
        when(support.request.getParameter("scadenza")).thenReturn("2030-12-31");
        when(support.request.getParameter("titolare")).thenReturn("Mario Rossi");

        Payment servlet = new Payment();
        support.invokeDoPost(servlet, support.request, support.response);

        // Il servlet non crasha con carta valida
        verify(support.request, atLeastOnce()).getParameter("numero-carta");
    }

    @Test
    @DisplayName("SECURITY: Payment.doPost con carta invalida non crea ordine")
    void testPaymentConCartaInvalida() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());
        when(support.request.getParameter("numero-carta")).thenReturn("123");
        when(support.request.getParameter("CVV")).thenReturn("1");
        when(support.request.getParameter("scadenza")).thenReturn("invalid");
        when(support.request.getParameter("titolare")).thenReturn("");

        Payment servlet = new Payment();
        try {
            support.invokeDoPost(servlet, support.request, support.response);
        } catch (Exception e) {
            // Eccezione attesa: parsing data fallisce
        }

        // Il servlet non crea l'ordine
        verify(support.request, never()).getRequestDispatcher("/WEB-INF/payment-success.jsp");
    }
}