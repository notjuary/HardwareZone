package Controller;

import java.io.*;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "registrationServlet", value = "/registration-servlet")
public class Registration extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");

        final Pattern info_string = Pattern.compile("^([a-zA-Z\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27]\\s?){2,20}$");
        final Pattern email_string = Pattern.compile("^[a-zA-Z\\d._%-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,20}$");
        final Pattern password_string = Pattern.compile("^[a-zA-Z\\d\\-\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27]{6,16}");
        final Pattern phone_string = Pattern.compile("^\\d{10}$");
        final Pattern province_string = Pattern.compile("^([a-zA-Z]{2})$");
        final Pattern postalcode_string = Pattern.compile("^\\d{5}$");
        final Pattern address_string = Pattern.compile("^([a-zA-Z\\d\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27\\x2C]\\s?){2,20}$");

        int level = 0;

        String name = request.getParameter("name");
        Matcher matcher = info_string.matcher(name);
        boolean matchFound = matcher.find();
        if (matchFound)
            level++;

        String surname = request.getParameter("surname");
        matcher = info_string.matcher(surname);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String email = request.getParameter("email");
        matcher = email_string.matcher(email);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String password = request.getParameter("password");
        matcher = password_string.matcher(password);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String phone = request.getParameter("phone");
        matcher = phone_string.matcher(phone);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String city = request.getParameter("city");
        matcher = info_string.matcher(city);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String province = request.getParameter("province");
        matcher = province_string.matcher(province);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String postalCode = request.getParameter("postalCode");
        matcher = postalcode_string.matcher(postalCode);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String address = request.getParameter("address");
        matcher = address_string.matcher(address);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        String date = request.getParameter("birthday");
        if (date != null)
            level++;

        System.out.println(level);

        UserDAO service = new UserDAO();

        if (!service.isAlreadyRegistered(email) && level == 10) {

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
                    "    <span class=\"closebtn\" onclick=\"clearDiv();\">&times;</span> \n" +
                    "    Registrazione effettuata\n" +
                    "    </div>");

            RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
            dispatcher.include(request, response);
            out.close();
        }

        else if (level != 10) {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Errore registrazione");
            request.setAttribute("redirect", "register.jsp");
            dispatcher.include(request, response);
        }

        else {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Email già in uso");
            request.setAttribute("redirect", "login.jsp");
            dispatcher.include(request, response);
        }
    }
}