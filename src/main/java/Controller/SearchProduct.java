package Controller;

import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "searchProductServlet", value = "/search-product-servlet")
public class SearchProduct extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String searchQuery = request.getParameter("searchQuery");

        ProductDAO service = new ProductDAO();
        ArrayList<ProductBean> listProducts = service.doRetrieveAll();

        JSONArray ja = new JSONArray();

        for (ProductBean product : listProducts) {

            if (product.getDescription().toLowerCase().contains(searchQuery.toLowerCase()) || product.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                JSONObject jo = new JSONObject();
                jo.put("id", product.getId());
                jo.put("name", product.getName());
                jo.put("image", product.getImage());
                jo.put("sales", product.getSales());
                jo.put("price", product.getPrice());
                jo.put("quantity", product.getQuantity());

                ja.put(jo);
            }
        }

        PrintWriter out = response.getWriter();
        out.write(String.valueOf(ja));
        out.flush();
    }
}