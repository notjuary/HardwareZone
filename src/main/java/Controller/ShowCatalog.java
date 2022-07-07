package Controller;

import Model.CategoryBean;
import Model.CategoryDAO;
import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "showCatalogServlet", value = "/show-catalog-servlet")
public class ShowCatalog extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int first = Integer.parseInt(request.getParameter("first"));
        int last = Integer.parseInt(request.getParameter("last"));

        CategoryDAO serviceCategory = new CategoryDAO();
        ArrayList<CategoryBean> listCategories = serviceCategory.doRetrieveAll();

        ProductDAO service = new ProductDAO();
        ArrayList<ProductBean> listProducts = service.doRetrieveAll(first, last);

        request.setAttribute("categories", listCategories);
        request.setAttribute("products", listProducts);


        String address = "/WEB-INF/catalog.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request, response);
    }

    /*
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int min = Integer.parseInt(request.getParameter("min"));
        int max = Integer.parseInt(request.getParameter("max"));
        String category = request.getParameter("category");
        String inSale = request.getParameter("inSale");

        System.out.println(min);
        System.out.println(max);
        System.out.println(category);
        System.out.println(inSale);


    }*/
}