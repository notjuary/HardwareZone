package Controller;

import Model.CartBean;
import Model.ProductBean;
import Model.ProductCartBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "showCart", value = "/show-cart-servlet")
public class ShowCart extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        CartBean cartBean = (CartBean) session.getAttribute("cart");
        ArrayList<ProductCartBean> cartList = cartBean.getList();
        ProductDAO service = new ProductDAO();
        JSONArray ja = new JSONArray();

        for (ProductCartBean product: cartList) {

            ProductBean productCatalog = service.doRetrieveById(product.getId());
            JSONObject jo = new JSONObject();

            jo.put("id", productCatalog.getId());
            jo.put("name", productCatalog.getName());
            jo.put("image", productCatalog.getImage());
            jo.put("sales", productCatalog.getSales());
            jo.put("price", productCatalog.getPrice());
            jo.put("quantity", product.getQuantity());

            ja.put(jo);
        }

        PrintWriter out = response.getWriter();
        out.write(String.valueOf(ja));
        out.flush();
    }
}