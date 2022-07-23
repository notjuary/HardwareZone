package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "orderUserInfoServlet", value = "/order-user-info-servlet")
public class OrderInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int id = Integer.parseInt(request.getParameter("id"));

        OrderProductDAO service = new OrderProductDAO();
        ArrayList<OrderProductBean> products = service.doRetrieveById(id);

        request.setAttribute("products", products);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/user/orderinfo.jsp");
        dispatcher.include(request, response);
    }
}
