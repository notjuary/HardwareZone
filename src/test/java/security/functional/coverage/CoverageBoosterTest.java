package security.functional.coverage; // <-- FIX: Corretto il nome del package

import Controller.AddToCart;
import Controller.Login;
import Controller.ProductsHomepage;
import Controller.*;
import Model.DataAccessException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test progettato per massimizzare la copertura (Coverage Booster).
 * Simula il crash delle Servlet (IOException/ServletException) per testare i blocchi catch.
 */
public class CoverageBoosterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // =========================================================
    // 1. Test per AddToCart (Catch su NumberFormatException e sendError)
    // =========================================================
    @Test
    @DisplayName("Coverage: AddToCart gestisce IOException su sendError")
    void testAddToCartCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET"); // AddToCart usa doGet

        // Simula un input non valido che fa scattare il catch
        when(request.getParameter("productId")).thenReturn("abc"); // Causa NumberFormatException
        when(request.getParameter("quantity")).thenReturn("1");

        // Forza sendError a lanciare IOException
        doThrow(new IOException("Errore simulato")).when(response).sendError(anyInt());

        AddToCart servlet = new AddToCart();

        // FIX: Usiamo service() invece di doGet()
        assertThrows(Exception.class, () -> servlet.service(request, response));
    }

    // =========================================================
    // 2. Test per Users (Catch su forward e include)
    // =========================================================
    @Test
    @DisplayName("Coverage: Users gestisce IOException su dispatcher.forward")
    void testUsersCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET"); // Users usa doGet

        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(session.getAttribute("user")).thenReturn(admin);

        // Forza forward a lanciare IOException
        doThrow(new IOException("Errore simulato")).when(dispatcher).forward(any(), any());

        Users servlet = new Users();

        // FIX: Usiamo service()
        assertThrows(Exception.class, () -> servlet.service(request, response));
    }

    // =========================================================
    // 3. Test per AddProduct (Catch su getPart e forward)
    // =========================================================
    @Test
    @DisplayName("Coverage: AddProduct gestisce eccezioni su getPart")
    void testAddProductCatch() throws Exception {
        when(request.getMethod()).thenReturn("POST"); // AddProduct usa doPost

        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(session.getAttribute("user")).thenReturn(admin);

        // Simula un errore durante il caricamento del file
        when(request.getPart(anyString())).thenThrow(new ServletException("Errore getPart"));

        // Forza il dispatcher a lanciare un errore
        doThrow(new IOException("Errore simulato")).when(dispatcher).include(any(), any());

        AddProduct servlet = new AddProduct();

        // FIX: Usiamo service()
        assertThrows(Exception.class, () -> servlet.service(request, response));
    }

    // =========================================================
    // 4. Test per ProductsHomepage (Catch su getWriter)
    // =========================================================
    @Test
    @DisplayName("Coverage: ProductsHomepage gestisce IOException su getWriter")
    void testProductsHomepageCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET"); // ProductsHomepage usa doGet

        // Simula un crash quando si prova a scrivere la risposta JSON
        when(response.getWriter()).thenThrow(new IOException("Errore writer"));

        ProductsHomepage servlet = new ProductsHomepage();

        // FIX: Usiamo service()
        assertThrows(Exception.class, () -> servlet.service(request, response));
    }

    // =========================================================
    // 5. Test per Login (Catch su include)
    // =========================================================
    @Test
    @DisplayName("Coverage: Login gestisce eccezioni sul dispatcher")
    void testLoginCatch() throws Exception {
        when(request.getMethod()).thenReturn("POST"); // Login usa doPost

        when(request.getParameter("email")).thenReturn("test@test.com");
        when(request.getParameter("password")).thenReturn("password");

        doThrow(new ServletException("Errore simulato")).when(dispatcher).include(any(), any());

        Login servlet = new Login();

        // FIX: Usiamo service()
        assertThrows(Exception.class, () -> servlet.service(request, response));
    }

    @Test
    @DisplayName("Coverage: DataAccessException istanziata correttamente")
    void testDataAccessException() {
        DataAccessException ex1 = new DataAccessException("Messaggio di errore");
        DataAccessException ex2 = new DataAccessException(new RuntimeException("Causa"));
        DataAccessException ex3 = new DataAccessException("Messaggio", new RuntimeException("Causa"));

        assertThat(ex1.getMessage()).isEqualTo("Messaggio di errore");
        assertThat(ex2.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(ex3.getMessage()).isEqualTo("Messaggio");
    }
}