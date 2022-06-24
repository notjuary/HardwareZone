package Controller;

import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "addProductServlet", value = "/add-product-servlet")
public class AddProduct extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String price = request.getParameter("price");
        String quantity = request.getParameter("quantity");
        String sales = request.getParameter("sales");
        String category = request.getParameter("category");

        ProductDAO service = new ProductDAO();
        ProductBean product = new ProductBean();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(Double.parseDouble(price));
        product.setQuantity(Integer.parseInt(quantity));
        product.setSales(Integer.parseInt(sales));
        product.setCategory(category);
        product.setImage("nophoto.png");

        service.doSave(product);
    }
}