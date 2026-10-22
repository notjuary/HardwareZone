package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "setAdminServlet", value = "/set-admin-servlet")
public class SetAdmin extends HttpServlet {

    private static final String ERROR_JSP = "/WEB-INF/error.jsp";
    private static final String USERS_SERVLET = "users-servlet"; // FIX S1192
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

        if (userAdmin == null || !userAdmin.isAdmin().equalsIgnoreCase("true")) {
            request.getRequestDispatcher("index.jsp").include(request, response); // NOSONAR
            return;
        }

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            sendError(request, response, "ID utente non valido", TYPE_ALERT);
            return;
        }

        UserDAO service = new UserDAO();
        UserBean user = service.doRetrieveById(id);

        if (user == null) {
            sendError(request, response, "Utente non trovato", TYPE_ALERT);
            return;
        }

        if (user.isActive().equalsIgnoreCase("true")) {
            if (user.isAdmin().equalsIgnoreCase("true")) {
                sendError(request, response, "L'utente selezionato è già un amministratore", TYPE_WARNING);
            } else {
                service.doUpdateAdmin(user);
                sendError(request, response, "Operazione effettuata", TYPE_SUCCESS);
            }
        } else {
            sendError(request, response, "Utente non attivo", TYPE_ALERT);
        }
    }

    // FIX S1989: Cattura le eccezioni per non propagarle
    private void sendError(HttpServletRequest request, HttpServletResponse response, String msg, String type) {
        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute(ATTR_TYPE, type);
            request.setAttribute(ATTR_MSG, msg);
            request.setAttribute(ATTR_REDIRECT, USERS_SERVLET);
            dispatcher.include(request, response);
        } catch (ServletException | IOException e) {
            throw new RuntimeException("Errore critico durante il forward", e);
        }
    }
}