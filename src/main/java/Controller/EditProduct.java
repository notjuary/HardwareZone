package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet(name = "editProductServlet", value = "/edit-product-servlet")
public class EditProduct extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");

        if (user.isAdmin().equalsIgnoreCase("true")) {
            final Pattern decimal_String = Pattern.compile("^\\d*\\.?\\d*$");
            final Pattern int_String = Pattern.compile("^\\d+$");
            int level = 0;

            ProductDAO service = new ProductDAO();
            int id = Integer.parseInt(request.getParameter("id"));

            String name = request.getParameter("name");
            if (name.length() <= 20 && name.length() != 0)
                level++;

            String description = request.getParameter("description");
            if (description.length() <= 255 && description.length() != 0)
                level++;

            String price = request.getParameter("price");
            Matcher matcher = decimal_String.matcher(price);
            boolean matchFound = matcher.find();
            if (matchFound)
                level++;

            String quantity = request.getParameter("quantity");
            matcher = int_String.matcher(quantity);
            matchFound = matcher.find();
            if (matchFound)
                level++;

            String sales = request.getParameter("sales");
            matcher = int_String.matcher(sales);
            matchFound = matcher.find();
            if (matchFound)
                level++;

            String category = request.getParameter("category");
            if (category.length() <= 20 && category.length() != 0)
                level++;

            Part part = request.getPart("image");

            String subpath;
            if (!part.getSubmittedFileName().isEmpty()) {
                String uploadPath = getServletContext().getRealPath("") + "\\img\\products";
                String imagepath = uploadPath + File.separator + part.getSubmittedFileName();
                part.write(imagepath);
                subpath = "./img/products/" + part.getSubmittedFileName();
            }

            else {
                subpath = service.doRetrieveById(id).getImage();
            }

            if (level == 6) {

                ProductBean product = new ProductBean();

                product.setId(id);
                product.setName(name);
                product.setDescription(description);
                product.setPrice(Double.parseDouble(price));
                product.setQuantity(Integer.parseInt(quantity));
                product.setSales(Integer.parseInt(sales));
                product.setCategory(category);
                product.setImage(subpath);

                service.doUpdate(product);

                response.sendRedirect("products-servlet");
            }

            else {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
                request.setAttribute("type", "alert");
                request.setAttribute("msg", "Errore modifica");
                request.setAttribute("redirect", "products-servlet");
                dispatcher.include(request, response);
            }
        }

        else {
            RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
            dispatcher.include(request, response);
        }
    }
}