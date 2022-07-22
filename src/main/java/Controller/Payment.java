package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "paymentServlet", value = "/payment-servlet")
public class Payment extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();

        if (session.getAttribute("user") == null) {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Accedi prima di continuare");
            request.setAttribute("redirect", "login.jsp");
            dispatcher.include(request, response);
        }

        else {

            CartBean cartBean = (CartBean) session.getAttribute("cart");
            ProductDAO serviceProduct = new ProductDAO();

            for (ProductCartBean productCartBean : cartBean.getCartList()) {

                ProductBean catalogProduct = serviceProduct.doRetrieveById(productCartBean.getId());

                if (productCartBean.getQuantity() > catalogProduct.getQuantity()) {
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
                    request.setAttribute("type", "alert");
                    request.setAttribute("msg", "Quantità richiesta di " + catalogProduct.getName() + " non disponibile. Quantità massima: " + catalogProduct.getQuantity());
                    request.setAttribute("redirect", request.getContextPath() + "/Cart.jsp");
                    dispatcher.include(request, response);
                    break;
                }
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/payment.jsp");
            dispatcher.include(request, response);
        }
    }
}