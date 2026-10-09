package Controller;

import Model.CartBean;
import Model.CartDAO;
import Model.ProductCartBean;
import Model.UserBean;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "logoutServlet", value = "/logout-servlet")
public class Logout extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        // FIX: non creare una nuova sessione se non esiste
        HttpSession session = request.getSession(false);

        if (session != null) {
            UserBean user = (UserBean) session.getAttribute("user");
            CartBean cartBean = (CartBean) session.getAttribute("cart");

            // FIX: null check su user e cartBean
            if (user != null && cartBean != null) {
                CartDAO serviceCart = new CartDAO();
                serviceCart.doDelete(user.getId());
                for (ProductCartBean product : cartBean.getCartList()) {
                    serviceCart.doSave(user.getId(), product.getId(), product.getQuantity());
                }
            }

            session.invalidate();
        }

        // Redirect alla home
        RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
        dispatcher.include(request, response);
    }
}