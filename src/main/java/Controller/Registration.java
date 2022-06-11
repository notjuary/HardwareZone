package Controller;

import java.io.*;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "registrationServlet", value = "/registration-servlet")
public class Registration extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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

        if (!service.isAlreadyRegistered(email)) {

            UserBean user = new UserBean();
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

            service.doSave(user);

            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            PrintWriter out = response.getWriter();
            out.println("<div class=\"success\">\n" +
                    "    <span class=\"closebtn\" onclick=\"this.parentElement.style.display='none';\">&times;</span> \n" +
                    "    Registrazione effettuata\n" +
                    "    </div>");

            RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
            dispatcher.include(request, response);
        } else {

            PrintWriter out = response.getWriter();
            out.println("<div class=\"alert\">\n" +
                    "    <span class=\"closebtn\" onclick=\"this.parentElement.style.display='none';\">&times;</span> \n" +
                    "    <strong>Attenzione!</strong> Email già in uso\n" +
                    "    </div>");

            RequestDispatcher dispatcher = request.getRequestDispatcher("register.jsp");
            dispatcher.include(request, response);
        }
    }

    public void destroy() {
    }
}