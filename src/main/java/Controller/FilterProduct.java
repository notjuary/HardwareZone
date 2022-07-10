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

@WebServlet(name = "filterProductServlet", value = "/filter-product-servlet")
public class FilterProduct extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        int minPrice = Integer.parseInt(request.getParameter("min"));
        int maxPrice = Integer.parseInt(request.getParameter("max"));
        String category = request.getParameter("category");

        if (maxPrice < minPrice)
            maxPrice = minPrice;

        ProductDAO service = new ProductDAO();
        ArrayList<ProductBean> listProducts = service.doRetrieveByFilter(minPrice, maxPrice, category);

        JSONArray ja = new JSONArray();

        for (ProductBean product: listProducts) {
            JSONObject jo = new JSONObject();
            jo.put("id", product.getId());
            jo.put("name", product.getName());
            jo.put("image", product.getImage());
            jo.put("sales", product.getSales());
            jo.put("price", product.getPrice());
            jo.put("quantity", product.getQuantity());

            ja.put(jo);
        }

        PrintWriter out = response.getWriter();
        out.write(String.valueOf(ja));
        out.flush();
    }
}