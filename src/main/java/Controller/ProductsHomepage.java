package Controller;

import Model.ProductBean;
import Model.ProductDAO;
import Model.UserBean;
import Model.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

@WebServlet(name = "productsHomepageServlet", value = "/products-homepage-servlet")
public class ProductsHomepage extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");

        ArrayList<ProductBean> listProduct = new ArrayList<ProductBean>();
        ProductDAO service = new ProductDAO();

        int max = service.doRetrieveAll().size();
        Random rand = new Random();
        boolean in;

        while (listProduct.size() != 10) {
            in = false;
            int random = rand.nextInt(max) + 1;

            for (ProductBean product: listProduct) {

                if (product.getId() == random) {
                    in = true;
                    break;
                }
            }

            if (!in)
                listProduct.add(service.doRetrieveById(random));
        }

        JSONArray ja = new JSONArray();
        for (ProductBean product: listProduct) {
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