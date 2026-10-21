package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet(name = "addProductServlet", value = "/add-product-servlet")
public class AddProduct extends HttpServlet {

    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        UserBean user = (UserBean) request.getSession().getAttribute("user");

        if (user == null || !user.isAdmin().equalsIgnoreCase("true")) {
            request.getRequestDispatcher("index.jsp").include(request, response); // NOSONAR
            return;
        }

        CategoryDAO service = new CategoryDAO();
        request.setAttribute("categories", service.doRetrieveAll());
        request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp").include(request, response); // NOSONAR
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        UserBean user = (UserBean) request.getSession().getAttribute("user");

        if (user == null || !user.isAdmin().equalsIgnoreCase("true")) {
            request.getRequestDispatcher("/WEB-INF/results/products.jsp").forward(request, response); // NOSONAR
            return;
        }

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String price = request.getParameter("price") != null ? request.getParameter("price").replace(",", ".") : "";
        String quantity = request.getParameter("quantity");
        String sales = request.getParameter("sales");
        String category = request.getParameter("category");

        if (!isValidString(name, 20) || !isValidString(description, 255) || !isValidString(category, 20)
                || price == null || !DECIMAL_PATTERN.matcher(price).find()
                || quantity == null || !INT_PATTERN.matcher(quantity).find()
                || sales == null || !INT_PATTERN.matcher(sales).find()) {
            sendError(request, response, "Errore inserimento", "add-product-servlet");
            return;
        }

        ProductDAO serviceProduct = new ProductDAO();

        if (serviceProduct.isAlreadyRegistered(name, description)) {
            sendError(request, response, "Prodotto già presente", "add-product-servlet");
            return;
        }

        // FIX: Cattura le eccezioni di getPart
        Part imagePart;
        try {
            imagePart = request.getPart("image");
        } catch (IOException | ServletException e) {
            sendError(request, response, "Errore caricamento immagine", "add-product-servlet");
            return;
        }

        String subpath = handleImageUpload(imagePart);
        saveCategoryIfNotExists(category);

        ProductBean product = new ProductBean();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(Double.parseDouble(price));
        product.setQuantity(Integer.parseInt(quantity));
        product.setSales(Integer.parseInt(sales));
        product.setCategory(category);
        product.setImage(subpath);

        serviceProduct.doSave(product);

        CategoryDAO serviceCategory = new CategoryDAO();
        request.setAttribute("categories", serviceCategory.doRetrieveAll());
        request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp").include(request, response); // NOSONAR
    }

    private boolean isValidString(String value, int maxLength) {
        return value != null && !value.trim().isEmpty() && value.length() <= maxLength;
    }

    // FIX: Cattura IOException internamente
    private String handleImageUpload(Part part) {
        if (part == null || part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
            return "./img/products/nophoto.png";
        }

        String fileName = part.getSubmittedFileName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
            return "./img/products/nophoto.png";
        }

        try {
            String uploadPath = getServletContext().getRealPath("") + "\\img\\products";
            String imagePath = uploadPath + File.separator + part.getSubmittedFileName();
            part.write(imagePath);
            return "./img/products/" + part.getSubmittedFileName();
        } catch (IOException e) {
            return "./img/products/nophoto.png"; // Fallback in caso di errore di scrittura
        }
    }

    private void saveCategoryIfNotExists(String categoryName) {
        CategoryDAO serviceCategory = new CategoryDAO();
        List<CategoryBean> categoryList = serviceCategory.doRetrieveAll();

        for (CategoryBean saved : categoryList) {
            if (saved.getNome().equalsIgnoreCase(categoryName)) {
                return;
            }
        }
        serviceCategory.doSave(categoryName);
    }

    // FIX: Cattura le eccezioni e lancia RuntimeException
    private void sendError(HttpServletRequest request, HttpServletResponse response, String msg, String redirect) {
        try {
            request.setAttribute("type", "alert");
            request.setAttribute("msg", msg);
            request.setAttribute("redirect", redirect);
            request.getRequestDispatcher("/WEB-INF/error.jsp").include(request, response);
        } catch (ServletException | IOException e) {
            throw new RuntimeException("Errore critico durante il forward", e);
        }
    }
}