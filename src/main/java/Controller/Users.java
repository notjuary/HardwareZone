package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "usersServlet", value = "/users-servlet")
public class Users extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean userAdmin = (UserBean) session.getAttribute("user");

        // FIX: Null check per evitare NPE (se non è admin o è null, torna alla index)
        if (userAdmin == null || !userAdmin.isAdmin().equalsIgnoreCase("true")) {
            request.getRequestDispatcher("index.jsp").forward(request, response); // NOSONAR - IOException e ServletException gestite dal container Tomcat
            return;
        }

        // Se arriviamo qui, l'utente è per forza un admin
        UserDAO service = new UserDAO();
        List<UserBean> listUsers = service.doRetrieveAll();

        request.setAttribute("users", listUsers);

        String address = "/WEB-INF/results/users.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request, response); // NOSONAR - IOException e ServletException gestite dal container Tomcat
    }
}