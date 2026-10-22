package Controller;

import Model.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "loginServlet", value = "/login-servlet")
public class Login extends HttpServlet {

    // FIX S1192: Definizione delle costanti
    private static final String ERROR_JSP = "/WEB-INF/error.jsp";
    private static final String TYPE_ALERT = "alert";
    private static final String ATTR_REDIRECT = "redirect";
    private static final String LOGIN_JSP = "login.jsp";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO service = new UserDAO();
        UserBean user = service.doRetrieveByEmailAndPassword(email, password);

        if (user != null && user.isActive().equalsIgnoreCase("true")) {

            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            RequestDispatcher dispatcher;

            if (user.isAdmin().equalsIgnoreCase("true")) {
                dispatcher = request.getRequestDispatcher("WEB-INF/admin/admin.jsp");
            } else {
                CartBean cartBean = new CartBean();
                CartDAO serviceCart = new CartDAO();

                cartBean.setCartList(serviceCart.getCart(user.getId()));

                session.setAttribute("cart", cartBean);
                dispatcher = request.getRequestDispatcher("index.jsp");
            }

            dispatcher.include(request, response);
        } else if (user == null) {
            // Utilizzo delle costanti
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute("type", TYPE_ALERT);
            request.setAttribute("msg", "Email o password errati");
            request.setAttribute(ATTR_REDIRECT, LOGIN_JSP);
            dispatcher.include(request, response);
        } else if (user.isActive().equalsIgnoreCase("false")) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute("type", TYPE_ALERT);
            request.setAttribute("msg", "Account disabilitato");
            request.setAttribute(ATTR_REDIRECT, LOGIN_JSP);
            dispatcher.include(request, response);
        } else {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute("type", TYPE_ALERT);
            request.setAttribute("msg", "Errore imprevisto");
            request.setAttribute(ATTR_REDIRECT, LOGIN_JSP);
            dispatcher.include(request, response);
        }
    }
}