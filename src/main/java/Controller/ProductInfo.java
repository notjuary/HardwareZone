package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "productInfoServlet", value = "/product-info-servlet")
public class ProductInfo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");

        if (user.isAdmin().equalsIgnoreCase("true")) {

            int id = Integer.parseInt(request.getParameter("id"));
            ProductDAO service = new ProductDAO();
            ProductBean product = service.doRetrieveById(id);

            CategoryDAO serviceCategory = new CategoryDAO();
            ArrayList<CategoryBean> listCategories = serviceCategory.doRetrieveAll();

            request.setAttribute("productJSP", product);
            request.setAttribute("categories", listCategories);

            RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/results/productInfo.jsp");
            dispatcher.include(request, response);
        }
    }
}