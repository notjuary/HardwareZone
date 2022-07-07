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
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");

        request.setAttribute("profileJSP", user);

        if (user.isAdmin().equalsIgnoreCase("true")) {

            RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/admin/profile-admin.jsp");
            dispatcher.include(request, response);
        }

        else {

            RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/user/profile-user.jsp");
            dispatcher.include(request, response);
        }
    }
}
