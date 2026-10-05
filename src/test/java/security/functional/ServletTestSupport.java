package security.functional;

import Model.UserBean;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Classe base per i test funzionali.
 * Fornisce mock preconfigurati di request/response/session/dispatcher.
 */
public abstract class ServletTestSupport {

    public HttpServletRequest request;
    public HttpServletResponse response;
    public HttpSession session;
    public RequestDispatcher dispatcher;


    public void setUpBase() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    public UserBean createAdminUser() {
        UserBean user = new UserBean();
        user.setId(1);
        user.setName("Admin");
        user.setEmail("admin@test.com");
        user.setAdmin("true");
        user.setState("true");
        return user;
    }

    public UserBean createNormalUser() {
        UserBean user = new UserBean();
        user.setId(2);
        user.setName("User");
        user.setEmail("user@test.com");
        user.setAdmin("false");
        user.setState("true");
        return user;
    }

    public void invokeDoGet(Object servlet,
                               HttpServletRequest req,
                               HttpServletResponse resp) throws Exception {
        java.lang.reflect.Method method = servlet.getClass().getDeclaredMethod(
                "doGet", HttpServletRequest.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(servlet, req, resp);
    }

    public void invokeDoPost(Object servlet,
                                HttpServletRequest req,
                                HttpServletResponse resp) throws Exception {
        java.lang.reflect.Method method = servlet.getClass().getDeclaredMethod(
                "doPost", HttpServletRequest.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(servlet, req, resp);
    }
}