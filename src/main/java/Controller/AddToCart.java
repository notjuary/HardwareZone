package Controller;

import Model.Cart;
import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "addToCartServlet", value = "/add-to-cart-servlet")
public class AddToCart extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int productId = Integer.parseInt(request.getParameter("productId"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        ProductDAO service = new ProductDAO();
        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null)
            cart = new Cart();

        ProductBean productBean = service.doRetrieveById(productId);

        if (productBean.getQuantity() >= quantity) {
            cart.addProduct(productId, quantity);
            productBean.setQuantity(productBean.getQuantity() - quantity);
            service.doUpdate(productBean);
        }

        else {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Quantità non disponibile");
            request.setAttribute("redirect", "${pageContext.request.contextPath}/show-product-servlet?productId=" + productId);
            dispatcher.include(request, response);
        }

        session.setAttribute("cart", cart);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        System.out.println(" we ");
    }
}