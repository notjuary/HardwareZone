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

    // FIX S1192: Estrazione delle costanti
    private static final String ERROR_JSP = "/WEB-INF/error.jsp";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_MSG = "msg";
    private static final String ATTR_REDIRECT = "redirect";
    private static final String TYPE_SUCCESS = "success";
    private static final String TYPE_ALERT = "alert";

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
        if (matcher.find()) level++;

        String surname = request.getParameter("surname");
        matcher = info_string.matcher(surname);
        if (matcher.find()) level++;

        String email = request.getParameter("email");
        matcher = email_string.matcher(email);
        if (matcher.find()) level++;

        String password = request.getParameter("password");
        matcher = password_string.matcher(password);
        if (matcher.find()) level++;

        String phone = request.getParameter("phone");
        matcher = phone_string.matcher(phone);
        if (matcher.find()) level++;

        String city = request.getParameter("city");
        matcher = info_string.matcher(city);
        if (matcher.find()) level++;

        String province = request.getParameter("province");
        matcher = province_string.matcher(province);
        if (matcher.find()) level++;

        String postalCode = request.getParameter("postalCode");
        matcher = postalcode_string.matcher(postalCode);
        if (matcher.find()) level++;

        String address = request.getParameter("address");
        matcher = address_string.matcher(address);
        if (matcher.find()) level++;

        String date = request.getParameter("birthday");
        if (date != null) level++;

        // Rimosso System.out.println(level) per evitare leak nei log

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

            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute(ATTR_TYPE, TYPE_SUCCESS);
            request.setAttribute(ATTR_MSG, "Registrazione effettuata");
            request.setAttribute(ATTR_REDIRECT, "index.jsp");
            dispatcher.include(request, response);
        } else if (level != 10) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute(ATTR_TYPE, TYPE_ALERT);
            request.setAttribute(ATTR_MSG, "Errore registrazione");
            request.setAttribute(ATTR_REDIRECT, "register.jsp");
            dispatcher.include(request, response);
        } else {
            RequestDispatcher dispatcher = request.getRequestDispatcher(ERROR_JSP);
            request.setAttribute(ATTR_TYPE, TYPE_ALERT);
            request.setAttribute(ATTR_MSG, "Email già in uso");
            request.setAttribute(ATTR_REDIRECT, "login.jsp");
            dispatcher.include(request, response);
        }
    }
}