package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "showOrderUserServlet", value = "/show-order-user-servlet")
public class ShowOrderUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");

        if (user.isAdmin().equalsIgnoreCase("false")) {

            OrderDAO serviceOrder = new OrderDAO();
            ArrayList<OrderBean> orders = serviceOrder.doRetrieveById(user.getId());

            request.setAttribute("orders", orders);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/user/orders.jsp");
            dispatcher.include(request, response);
        }

        else if (user.isAdmin().equalsIgnoreCase("true")) {
            OrderDAO serviceOrder = new OrderDAO();
            ArrayList<OrderBean> orders = serviceOrder.doRetrieveAll();

            request.setAttribute("orders", orders);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/orders.jsp");
            dispatcher.include(request, response);
        }


    }
}