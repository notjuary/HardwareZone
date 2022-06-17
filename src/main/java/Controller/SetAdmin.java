package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

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

                PrintWriter out = response.getWriter();
                out.println("<div class=\"warning\">\n" +
                        "    <span class=\"closebtn\" onclick=\"clearDiv();\">&times;</span> \n" +
                        "    L'utente selezionato è già un amministratore\n" +
                        "    </div>");

                RequestDispatcher dispatcher = request.getRequestDispatcher("/users-servlet");
                dispatcher.include(request, response);
                out.close();
            }

            else {

                service.doUpdateAdmin(user);

                PrintWriter out = response.getWriter();
                out.println("<div class=\"success\">\n" +
                        "    <span class=\"closebtn\" onclick=\"clearDiv();\">&times;</span> \n" +
                        "    L'utente ora è amministratore\n" +
                        "    </div>");

                RequestDispatcher dispatcher = request.getRequestDispatcher("/users-servlet");
                dispatcher.include(request, response);
                out.close();
            }
        }

        else {

            PrintWriter out = response.getWriter();
            out.println("<div class=\"alert\">\n" +
                    "    <span class=\"closebtn\" onclick=\"clearDiv();\">&times;</span> \n" +
                    "    Utente non attivo\n" +
                    "    </div>");

            RequestDispatcher dispatcher = request.getRequestDispatcher("/users-servlet");
            dispatcher.include(request, response);
            out.close();
        }
    }
}