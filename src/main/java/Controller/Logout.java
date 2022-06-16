package Controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "logoutServlet", value = "/logout-servlet")
public class Logout extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        session.invalidate();

        PrintWriter out = response.getWriter();
        out.println("<div class=\"success\">\n" +
                "    <span class=\"closebtn\" onclick=\"clearDiv();\">&times;</span> \n" +
                "    <strong>Logout eseguito</strong>\n" +
                "    </div>");

        RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
        dispatcher.include(request, response);
        out.close();
    }
}