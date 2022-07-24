package Controller;

import Model.*;
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
        UserBean user = (UserBean) session.getAttribute("user");
        CartDAO serviceCart = new CartDAO();

        cart.removeProduct(id);

        if (user != null) {
            serviceCart.doDelete(user.getId());
            for (ProductCartBean product : cart.getCartList()) {
                serviceCart.doSave(user.getId(), product.getId(), product.getQuantity());
            }
        }

        session.setAttribute("cart", cart);
    }
}