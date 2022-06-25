package Controller;

import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@MultipartConfig
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
        String image;

        Part part = request.getPart("image");
        String fileName = part.getSubmittedFileName();
        String path = getServletContext().getRealPath("/img/products" + File.separator + fileName);

        InputStream is = part.getInputStream();
        if (uploadFile(is, path))
            image = path;
        else
            image = "./img/products/nophoto.png";

        ProductDAO service = new ProductDAO();
        ProductBean product = new ProductBean();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(Double.parseDouble(price));
        product.setQuantity(Integer.parseInt(quantity));
        product.setSales(Integer.parseInt(sales));
        product.setCategory(category);
        product.setImage(image);

        service.doSave(product);
    }

    public boolean uploadFile(InputStream is, String path) {

        try {
            byte[] byt = new byte[is.available()];
            is.read();
            FileOutputStream fops = new FileOutputStream(path);
            fops.write(byt);
            fops.flush();
            fops.close();

            return true;
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}