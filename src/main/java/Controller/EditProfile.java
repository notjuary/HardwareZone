package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "editProfileServlet", value = "/edit-profile-servlet")
public class EditProfile extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String city = request.getParameter("city");
        String province = request.getParameter("province");
        String postalCode = request.getParameter("postalCode");
        String address = request.getParameter("address");
        String date = request.getParameter("birthday");

        UserDAO service = new UserDAO();

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");

        user.setId(user.getId());
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setCity(city);
        user.setProvince(province);
        user.setPostalCode(postalCode);
        user.setAddress(address);
        user.setBirthday(date);
        user.setRegister();
        user.setState("true");
        user.setAdmin("false");

        service.doUpdate(user);

        request.setAttribute("profileJSP", user);

        PrintWriter out = response.getWriter();
        out.println("<div class=\"success\">\n" +
                "    <span class=\"closebtn\" onclick=\"clearDiv();\">&times;</span> \n" +
                "    Modifica effettuata\n" +
                "    </div>");

        RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/profile-user.jsp");
        dispatcher.include(request, response);
        out.close();
    }
}