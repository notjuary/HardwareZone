package Controller;

import Model.ProductBean;
import Model.ProductDAO;
import Model.UserBean;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "showProductServlet", value = "/show-product-servlet")
public class ShowProduct extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean userAdmin = (UserBean) session.getAttribute("user");

        if (userAdmin.isAdmin().equalsIgnoreCase("true")) {

            int productId = Integer.parseInt(request.getParameter("productId"));

            ProductDAO service = new ProductDAO();
            ProductBean product = service.doRetrieveById(productId);

            request.setAttribute("product", product);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/product.jsp");
            dispatcher.include(request, response);
        }

        else {

            RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
            dispatcher.include(request, response);
        }
    }
}