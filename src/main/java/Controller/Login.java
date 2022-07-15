package Controller;

import Model.Cart;
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
        UserBean user = service.doRetrieveByEmailAndPassword(email, password);

        if (user != null && user.isActive().equalsIgnoreCase("true")) {

            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            RequestDispatcher dispatcher;

            if (user.isAdmin().equalsIgnoreCase("true")) {
                dispatcher = request.getRequestDispatcher("WEB-INF/admin/admin.jsp");
            }

            else {

                Cart cart = new Cart();
                session.setAttribute("cart", cart);
                dispatcher = request.getRequestDispatcher("index.jsp");
            }

            PrintWriter out = response.getWriter();
            out.println("<div class=\"success\">\n" +
                    "    <span class=\"closebtn\" onclick=\"clearDiv();\">&times;</span> \n" +
                    "    <strong>Login eseguito</strong> \n" +
                    "    </div>");

            dispatcher.include(request, response);
            out.close();
        }

        else if (user == null) {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Email o password errati");
            request.setAttribute("redirect", "login.jsp");
            dispatcher.include(request, response);
        }

        else if (user.isActive().equalsIgnoreCase("false")) {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Account disabilitato");
            request.setAttribute("redirect", "login.jsp");
            dispatcher.include(request, response);
        }

        else {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Errore imprevisto");
            request.setAttribute("redirect", "login.jsp");
            dispatcher.include(request, response);
        }
    }
}
