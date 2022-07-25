package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "setStateUserServlet", value = "/set-state-user-servlet")
public class SetStateUser extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean userAdmin = (UserBean) session.getAttribute("user");

        if (userAdmin.isAdmin().equalsIgnoreCase("true")) {

            String state = request.getParameter("active");
            int id = Integer.parseInt(request.getParameter("id"));

            UserDAO service = new UserDAO();
            UserBean user = service.doRetrieveById(id);

            if (state.equalsIgnoreCase("true"))
                user.setState("false");
            else
                user.setState("true");

            service.doUpdateState(user);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/users-servlet");
            dispatcher.include(request, response);
        }

        else {

            RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
            dispatcher.include(request, response);
        }
    }
}