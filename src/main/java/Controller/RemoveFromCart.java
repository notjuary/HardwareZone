package Controller;

import Model.CartBean;
import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "removeFromCartServlet", value = "/remove-from-cart-servlet")
public class RemoveFromCart extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int id = Integer.parseInt(request.getParameter("productId"));
        HttpSession session = request.getSession();
        CartBean cart = (CartBean) session.getAttribute("cart");

        cart.removeProduct(id);
        session.setAttribute("cart", cart);
    }
}