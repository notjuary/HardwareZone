package Controller;

import Model.CategoryBean;
import Model.CategoryDAO;
import Model.ProductBean;
import Model.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet(name = "addProductServlet", value = "/add-product-servlet")
public class AddProduct extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        CategoryDAO service = new CategoryDAO();
        ArrayList<CategoryBean> listCategories = service.doRetrieveAll();

        request.setAttribute("categories", listCategories);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp");
        dispatcher.include(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String price = request.getParameter("price");
        String quantity = request.getParameter("quantity");
        String sales = request.getParameter("sales");
        String category = request.getParameter("category");

        Part part = request.getPart("image");
        String uploadPath = getServletContext().getRealPath("") + "\\img\\products";
        String imagepath = uploadPath + File.separator + part.getSubmittedFileName();
        part.write(imagepath);
        String subpath = "./img/products/" + part.getSubmittedFileName();

        ProductDAO service = new ProductDAO();
        ProductBean product = new ProductBean();

        CategoryDAO serviceCategory = new CategoryDAO();
        ArrayList<CategoryBean> categoryList = serviceCategory.doRetrieveAll();

        boolean categoryExist = false;
        for (CategoryBean categorySaved: categoryList) {

            if (categorySaved.getNome().equalsIgnoreCase(category)) {
                categoryExist = true;
                break;
            }
        }

        if (!categoryExist)
            serviceCategory.doSave(category);

        product.setName(name);
        product.setDescription(description);
        product.setPrice(Double.parseDouble(price));
        product.setQuantity(Integer.parseInt(quantity));
        product.setSales(Integer.parseInt(sales));
        product.setCategory(category);
        product.setImage(subpath);

        service.doSave(product);

        RequestDispatcher dispatcher = request.getRequestDispatcher("${pageContext.request.contextPath}/add-product-servlet");
        dispatcher.include(request, response);
    }
}