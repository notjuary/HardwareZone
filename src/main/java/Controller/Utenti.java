package Controller;

import Model.UtenteBean;
import Model.UtenteDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "utentiServlet", value = "/utenti-servlet")
public class Utenti extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        UtenteDAO service = new UtenteDAO();

        List<UtenteBean> listaUtenti = service.doRetrieveAll();

        request.setAttribute("utenti", listaUtenti);

        String address = "/WEB-INF/results/utenti.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request,response);
    }
}