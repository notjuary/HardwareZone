package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "setAdminServlet", value = "/set-admin-servlet")
public class SetAdmin extends HttpServlet {

    // FIX S1192: Estrazione delle costanti
    private static final String ERROR_JSP = "/WEB-INF/error.jsp";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_MSG = "msg";
    private static final String ATTR_REDIRECT = "redirect";
    private static final String TYPE_SUCCESS = "success";
    private static final String TYPE_ALERT = "alert";
    private static final String TYPE_WARNING = "warning";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean userAdmin = (UserBean) session.getAttribute("user");

        // FIX S2259: Null check per evitare NPE
        if (userAdmin == null || !userAdmin.isAdmin().equalsIgnoreCase("true")) {
            request.getRequestDispatcher("index.jsp").include(request, response); // NOSONAR
            return;
        }

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute(ATTR_TYPE, TYPE_ALERT);
            request.setAttribute(ATTR_MSG, "ID utente non valido");
            request.setAttribute(ATTR_REDIRECT, "users-servlet");
            dispatcher.include(request, response);
            return;
        }

        UserDAO service = new UserDAO();
        UserBean user = service.doRetrieveById(id);

        // FIX S2259: Null check se l'utente non esiste
        if (user == null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute(ATTR_TYPE, TYPE_ALERT);
            request.setAttribute(ATTR_MSG, "Utente non trovato");
            request.setAttribute(ATTR_REDIRECT, "users-servlet");
            dispatcher.include(request, response);
            return;
        }

        if (user.isActive().equalsIgnoreCase("true")) {
            if (user.isAdmin().equalsIgnoreCase("true")) {
                RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
                request.setAttribute(ATTR_TYPE, TYPE_WARNING);
                request.setAttribute(ATTR_MSG, "L'utente selezionato è già un amministratore");
                request.setAttribute(ATTR_REDIRECT, "users-servlet");
                dispatcher.include(request, response);
            } else {
                service.doUpdateAdmin(user);

                RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
                request.setAttribute(ATTR_TYPE, TYPE_SUCCESS);
                request.setAttribute(ATTR_MSG, "Operazione effettuata");
                request.setAttribute(ATTR_REDIRECT, "users-servlet");
                dispatcher.include(request, response);
            }
        } else {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute(ATTR_TYPE, TYPE_ALERT);
            request.setAttribute(ATTR_MSG, "Utente non attivo");
            request.setAttribute(ATTR_REDIRECT, "users-servlet");
            dispatcher.include(request, response);
        }
    }
}