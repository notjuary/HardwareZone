package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "addToCartServlet", value = "/add-to-cart-servlet")
public class AddToCart extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int productId = -1;
        boolean validProduct = false;

        try {
            productId = Integer.parseInt(request.getParameter("productId"));
            validProduct = true;
        } catch (NumberFormatException e) {
            // gestito sotto, fuori dal catch
        }

        if (!validProduct) {
            response.sendError(400);   // sendError ora è fuori dal catch
            return;
        }

        int quantity = Integer.parseInt(request.getParameter("quantity"));

        if (quantity <= 0) {
            response.sendError(400);
            return;
        }

        ProductDAO service = new ProductDAO();
        HttpSession session = request.getSession();
        CartBean cartBean = (CartBean) session.getAttribute("cart");
        CartDAO serviceCart = new CartDAO();
        UserBean user = (UserBean) session.getAttribute("user");

        if (cartBean == null)
            cartBean = new CartBean();

        ProductBean productBean = service.doRetrieveById(productId);

        if (productBean == null) {
            response.sendError(404);
            return;
        }

        if (productBean.getQuantity() >= quantity)
            cartBean.addProduct(productId, quantity);
        else {
            response.sendError(400);
            return;
        }

        if (user != null) {
            serviceCart.doDelete(user.getId());
            for (ProductCartBean product : cartBean.getCartList()) {
                serviceCart.doSave(user.getId(), product.getId(), product.getQuantity());
            }
        }

        session.setAttribute("cart", cartBean);
    }
}