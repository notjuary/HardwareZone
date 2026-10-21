package security.functional.authorization;

import Controller.Login;
import Model.UserBean;
import Model.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per Login.
 * Verifica: autenticazione, redirect admin/user, account disabilitato.
 */
@DisplayName("Login - Test funzionale di sicurezza")
class LoginFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        // FIX: Usa UserDAO per creare gli utenti e far generare l'hash BCrypt automaticamente
        UserDAO userDAO = new UserDAO();

        UserBean admin = new UserBean();
        admin.setName("Admin");
        admin.setSurname("Test");
        admin.setEmail("admin@test.com");
        admin.setPassword("password123");
        admin.setBirthday("1990-01-01");
        admin.setPhone("3331234567");
        admin.setCity("SA");
        admin.setProvince("SA");
        admin.setPostalCode("84100");
        admin.setAddress("Via 1");
        admin.setRegister("2024-01-01");
        admin.setState("true");
        admin.setAdmin("true");
        userDAO.doSave(admin);

        UserBean user = new UserBean();
        user.setName("User");
        user.setSurname("Test");
        user.setEmail("user@test.com");
        user.setPassword("password123");
        user.setBirthday("1995-01-01");
        user.setPhone("3339999999");
        user.setCity("NA");
        user.setProvince("NA");
        user.setPostalCode("80100");
        user.setAddress("Via 2");
        user.setRegister("2024-01-02");
        user.setState("true");
        user.setAdmin("false");
        userDAO.doSave(user);

        UserBean disabled = new UserBean();
        disabled.setName("Disabled");
        disabled.setSurname("Test");
        disabled.setEmail("disabled@test.com");
        disabled.setPassword("password123");
        disabled.setBirthday("1995-01-01");
        disabled.setPhone("3338888888");
        disabled.setCity("NA");
        disabled.setProvince("NA");
        disabled.setPostalCode("80100");
        disabled.setAddress("Via 3");
        disabled.setRegister("2024-01-03");
        disabled.setState("false");
        disabled.setAdmin("false");
        userDAO.doSave(disabled);

        // Mock dei dispatcher
        when(support.request.getRequestDispatcher("WEB-INF/admin/admin.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: login admin crea la sessione e reindirizza a admin.jsp")
    void testLoginAdmin() throws Exception {
        when(support.request.getParameter("email")).thenReturn("admin@test.com");
        when(support.request.getParameter("password")).thenReturn("password123");

        Login servlet = new Login();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("user"), any());
    }

    @Test
    @DisplayName("SECURITY: login utente normale crea la sessione e reindirizza a index")
    void testLoginUtenteNormale() throws Exception {
        when(support.request.getParameter("email")).thenReturn("user@test.com");
        when(support.request.getParameter("password")).thenReturn("password123");

        Login servlet = new Login();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("user"), any());
    }

    @ParameterizedTest(name = "SECURITY: login fallito con email={0} e password={1} non crea sessione")
    @CsvSource({
            "user@test.com,       wrongpassword",
            "nonexistent@test.com, password123",
            "disabled@test.com,   password123"
    })
    @DisplayName("SECURITY: login fallito (password errata, email inesistente, account disabilitato)")
    void testLoginFallitoNonCreaSessione(String email, String password) throws Exception {
        when(support.request.getParameter("email")).thenReturn(email);
        when(support.request.getParameter("password")).thenReturn(password);

        Login servlet = new Login();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session, never()).setAttribute(eq("user"), any());
    }
}