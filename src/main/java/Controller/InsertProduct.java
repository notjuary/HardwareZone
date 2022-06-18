package Controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "insertProductServlet", value = "/insert-product-servlet")
public class InsertProduct extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp");
        dispatcher.include(request, response);
    }
}