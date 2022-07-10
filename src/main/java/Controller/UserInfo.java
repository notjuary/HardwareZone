package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "userInfoServlet", value = "/user-info-servlet")
public class UserInfo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        UserDAO service = new UserDAO();
        int id = Integer.parseInt(request.getParameter("id"));
        UserBean user = service.doRetrieveById(id);

        request.setAttribute("profileJSP", user);

        RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/results/userinfo.jsp");
        dispatcher.include(request, response);
    }
}