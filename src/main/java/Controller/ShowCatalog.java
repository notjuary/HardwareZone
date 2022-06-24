package Controller;

import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "showCatalogServlet", value = "/show-catalog-servlet")
public class ShowCatalog extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        ProductDAO service = new ProductDAO();
        ArrayList<ProductBean> listProducts = service.doRetrieveAll();

        request.setAttribute("products", listProducts);

        String address = "/WEB-INF/catalog.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request,response);
    }
}