package Controller;

import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "loginServlet", value = "/login-servlet")
public class Login extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO service = new UserDAO();
        int login = service.doRetrieveByEmailAndPassword(email, password);

        if (login != -1) {

            UserBean user = service.doRetrieveById(login);

            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            RequestDispatcher dispatcher;

            if (user.isAdmin().equalsIgnoreCase("true")) {
                dispatcher = request.getRequestDispatcher("WEB-INF/admin.jsp");
            } else {
                dispatcher = request.getRequestDispatcher("index.jsp");
            }

            PrintWriter out = response.getWriter();
            out.println("<div class=\"success\">\n" +
                    "    <span class=\"closebtn\" onclick=\"this.parentElement.style.display='none';\">&times;</span> \n" +
                    "    <strong>Login eseguito</strong> \n" +
                    "    </div>");

            dispatcher.include(request, response);

        } else {

            PrintWriter out = response.getWriter();
            out.println("<div class=\"alert\">\n" +
                    "    <span class=\"closebtn\" onclick=\"this.parentElement.style.display='none';\">&times;</span> \n" +
                    "    <strong>Attenzione!</strong> Email o password errate\n" +
                    "    </div>");

            RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
            dispatcher.include(request, response);
        }
    }

    public void destroy() {
    }
}
