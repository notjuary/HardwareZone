package Controller;

import Model.UtenteBean;
import Model.UtenteDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "profiloServlet", value = "/profilo-servlet")
public class Profilo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        int id = (int) session.getAttribute("id-utente");

        UtenteDAO service = new UtenteDAO();
        UtenteBean utente = service.doRetrieveById(id);

        request.setAttribute("profiloJSP", utente);

        RequestDispatcher dispatcher = request.getRequestDispatcher("profilo.jsp");
        dispatcher.include(request, response);
    }
}
