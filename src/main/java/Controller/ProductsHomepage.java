package Controller;

import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@WebServlet(name = "productsHomepageServlet", value = "/products-homepage-servlet")
public class ProductsHomepage extends HttpServlet {

    // FIX: Random come campo static final, riutilizzato tra le chiamate
    private static final Random RANDOM = new Random();
    private static final int HOMEPAGE_SIZE = 12;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");

        ProductDAO service = new ProductDAO();
        ArrayList<ProductBean> allProducts = service.doRetrieveAll();

        // FIX: shuffle + subList evita loop infinito e IllegalArgumentException su DB vuoto
        List<ProductBean> listProduct;
        if (allProducts.isEmpty()) {
            listProduct = Collections.emptyList();
        } else {
            Collections.shuffle(allProducts, RANDOM);
            int target = Math.min(HOMEPAGE_SIZE, allProducts.size());
            listProduct = allProducts.subList(0, target);
        }

        JSONArray ja = new JSONArray();
        for (ProductBean product : listProduct) {
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