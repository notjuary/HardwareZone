package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.GregorianCalendar;
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

            double total = 0;
            for (ProductCartBean productCartBean : cartBean.getCartList()) {

                ProductBean catalogProduct = serviceProduct.doRetrieveById(productCartBean.getId());

                if (catalogProduct.getPrice() == 0)
                    total = total + (catalogProduct.getPrice() * productCartBean.getQuantity());
                else
                    total = total + ((catalogProduct.getPrice() - (catalogProduct.getPrice() * catalogProduct.getSales() / 100)) * productCartBean.getQuantity());

                if (productCartBean.getQuantity() > catalogProduct.getQuantity()) {
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
                    request.setAttribute("type", "alert");
                    request.setAttribute("msg", "Quantità richiesta di " + catalogProduct.getName() + " non disponibile. Quantità massima: " + catalogProduct.getQuantity());
                    request.setAttribute("redirect", request.getContextPath() + "/Cart.jsp");
                    dispatcher.include(request, response);
                    break;
                }
            }

            request.setAttribute("total", String.format("%.2f", total));
            session.setAttribute("total", total);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/payment.jsp");
            dispatcher.include(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        final Pattern number_string =  Pattern.compile("^\\d+$");
        final Pattern holder_string =  Pattern.compile("^([a-zA-Z\\\\xE0\\xE8\\xE9\\xF9\\xF2\\xEC\\x27]\\s?){2,255}$");

        int level = 0;

        HttpSession session = request.getSession();

        String creditCardNumber = request.getParameter("numero-carta");
        Matcher matcher = number_string.matcher(creditCardNumber);
        boolean matchFound = matcher.find();
        if (creditCardNumber.length() == 16  && matchFound)
            level++;

        String cvv = request.getParameter("CVV");
        matcher = number_string.matcher(cvv);
        matchFound = matcher.find();
        if (cvv.length() == 3 && matchFound)
            level++;

        String deadline = request.getParameter("scadenza");
        GregorianCalendar today = new GregorianCalendar();
        String[] dateSplit = deadline.split("-");
        int year = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int day = Integer.parseInt(dateSplit[2]);
        month--;
        GregorianCalendar deadlineCalendar = new GregorianCalendar(year, month, day);
        if (deadlineCalendar.after(today))
            level++;

        String holder = request.getParameter("titolare");
        matcher = holder_string.matcher(holder);
        matchFound = matcher.find();
        if (matchFound)
            level++;

        if (level == 4) {
            UserBean user = new UserBean();
            user = (UserBean) session.getAttribute("user");

            OrderBean order = new OrderBean();
            order.setUser(user.getId());
            order.setTotal((Double) session.getAttribute("total"));

            OrderDAO serviceOrder = new OrderDAO();
            int orderId = serviceOrder.doSave(order);

            CartBean cart = (CartBean) session.getAttribute("cart");

            ProductDAO serviceProduct = new ProductDAO();

            for (ProductCartBean product : cart.getCartList()) {

                OrderProductBean orderProductBean = new OrderProductBean();
                orderProductBean.setProduct(product.getId());
                orderProductBean.setQuantity(product.getQuantity());
                orderProductBean.setPrice(serviceProduct.doRetrieveById(product.getId()).getPrice());
                orderProductBean.setOrder(orderId);

                OrderProductDAO serviceOrderProduct = new OrderProductDAO();
                serviceOrderProduct.doSave(orderProductBean);

                ProductBean productBean = serviceProduct.doRetrieveById(product.getId());
                productBean.setQuantity(productBean.getQuantity() - product.getQuantity());
                serviceProduct.doUpdate(productBean);
            }

            PaymentBean payment = new PaymentBean();
            payment.setOrder(orderId);
            payment.setDatePayment();
            payment.setCardNumber(creditCardNumber);
            payment.setCVV(cvv);
            payment.setDeadline(deadline);
            payment.setHolder(holder);

            PaymentDAO servicePayment = new PaymentDAO();
            servicePayment.doSave(payment);

            CartDAO serviceCart = new CartDAO();
            serviceCart.doDelete(user.getId());
            cart.setCartList(serviceCart.getCart(user.getId()));
            cart.setNumberObject(0);
            session.setAttribute("cart", cart);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "success");
            request.setAttribute("msg", "Ordine confermato");
            request.setAttribute("redirect", request.getContextPath() + "/index.jsp");
            dispatcher.include(request, response);
        }

        else {

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
            request.setAttribute("type", "alert");
            request.setAttribute("msg", "Errore campi");
            request.setAttribute("redirect", request.getContextPath() + "/payment-servlet");
            dispatcher.include(request, response);
        }
    }
}