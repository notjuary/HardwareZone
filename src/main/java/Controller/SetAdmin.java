package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "setAdminServlet", value = "/set-admin-servlet")
public class SetAdmin extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int id = Integer.parseInt(request.getParameter("id"));

        UserDAO service = new UserDAO();
        UserBean user = service.doRetrieveById(id);

        if (user.isActive().equalsIgnoreCase("true")) {

            if (user.isAdmin().equalsIgnoreCase("true")) {

                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
                request.setAttribute("type", "warning");
                request.setAttribute("msg", " L'utente selezionato è già un amministratore");
                request.setAttribute("redirect", "users-servlet");
                dispatcher.include(request, response);
            }

            else {

                service.doUpdateAdmin(user);

                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
                request.setAttribute("type", "success");
                request.setAttribute("msg", "Operazione effettuata");
                request.setAttribute("redirect", "users-servlet");
                dispatcher.include(request, response);
            }
        }

        else {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Utente non attivo");
            request.setAttribute("redirect", "/users-servlet");
            dispatcher.include(request, response);
        }
    }
}