package Controller;

import Model.CategoryBean;
import Model.CategoryDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "insertProductServlet", value = "/insert-product-servlet")
public class InsertProduct extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        CategoryDAO service = new CategoryDAO();
        ArrayList<CategoryBean> listCategories = service.doRetrieveAll();

        request.setAttribute("categories", listCategories);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp");
        dispatcher.include(request, response);
    }
}