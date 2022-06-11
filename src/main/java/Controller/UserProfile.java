package Controller;

import Model.UserBean;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "userProfileServlet", value = "/user-profile-servlet")
public class UserProfile extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");

        request.setAttribute("profileJSP", user);

        RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/profile-user.jsp");
        dispatcher.include(request, response);
    }
}
