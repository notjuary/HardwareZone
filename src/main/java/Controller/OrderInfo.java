package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "orderUserInfoServlet", value = "/order-user-info-servlet")
public class OrderInfo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");

        int id = Integer.parseInt(request.getParameter("id"));

        if (user.isAdmin().equalsIgnoreCase("true")) {
            OrderProductDAO service = new OrderProductDAO();
            ArrayList<OrderProductBean> products = service.doRetrieveById(id);

            ProductDAO serviceProduct = new ProductDAO();
            ArrayList<ProductBean> catalog = serviceProduct.doRetrieveAll();

            OrderDAO orderDAO = new OrderDAO();
            OrderBean orderBean = orderDAO.doRetrieveByIdOrder(id);

            UserDAO userDAO = new UserDAO();
            UserBean userOrder = userDAO.doRetrieveById(orderBean.getUser());

            System.out.println(orderBean.getUser());

            request.setAttribute("products", products);
            request.setAttribute("catalog", catalog);
            request.setAttribute("userOrder", userOrder);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/orderInfo.jsp");
            dispatcher.include(request, response);
        }

        else {

            OrderDAO serviceOrder = new OrderDAO();
            ArrayList<OrderBean> orderBean = serviceOrder.doRetrieveById(user.getId());

            for (OrderBean order : orderBean) {

                if (order.getId() == id) {

                    OrderProductDAO service = new OrderProductDAO();
                    ArrayList<OrderProductBean> products = service.doRetrieveById(id);

                    ProductDAO serviceProduct = new ProductDAO();
                    ArrayList<ProductBean> catalog = serviceProduct.doRetrieveAll();

                    request.setAttribute("products", products);
                    request.setAttribute("catalog", catalog);

                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/user/orderInfo.jsp");
                    dispatcher.include(request, response);
                }

                else {

                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
                    request.setAttribute("type", "alert");
                    request.setAttribute("msg", "Ordine non disponibile");
                    request.setAttribute("redirect", "show-order-user-servlet");
                    dispatcher.include(request, response);
                }
            }
        }
    }
}
