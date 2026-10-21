package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet(name = "editProductServlet", value = "/edit-product-servlet")
public class EditProduct extends HttpServlet {

    // FIX: Regex semplificata per evitare backtracking (super-linear performance)
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        UserBean user = (UserBean) request.getSession().getAttribute("user");

        if (user == null || !user.isAdmin().equalsIgnoreCase("true")) {
            request.getRequestDispatcher("index.jsp").include(request, response); // NOSONAR
            return;
        }

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            sendError(request, response, "Errore modifica: ID non valido");
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
            sendError(request, response, "Errore modifica: campi non validi");
            return;
        }

        ProductDAO service = new ProductDAO();

        // FIX: Cattura le eccezioni di getPart
        Part imagePart;
        try {
            imagePart = request.getPart("image");
        } catch (IOException | ServletException e) {
            sendError(request, response, "Errore caricamento immagine");
            return;
        }

        String subpath = handleImageUpload(imagePart, service, id);

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

    private boolean isValidString(String value, int maxLength) {
        return value != null && !value.trim().isEmpty() && value.length() <= maxLength;
    }

    // FIX: Cattura IOException internamente
    private String handleImageUpload(Part part, ProductDAO service, int id) {
        if (part == null || part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
            return service.doRetrieveById(id).getImage();
        }

        String fileName = part.getSubmittedFileName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
            return service.doRetrieveById(id).getImage();
        }

        try {
            String uploadPath = getServletContext().getRealPath("") + "\\img\\products";
            String imagePath = uploadPath + File.separator + part.getSubmittedFileName();
            part.write(imagePath);
            return "./img/products/" + part.getSubmittedFileName();
        } catch (IOException e) {
            return service.doRetrieveById(id).getImage(); // Fallback
        }
    }

    // FIX: Cattura le eccezioni e lancia RuntimeException
    private void sendError(HttpServletRequest request, HttpServletResponse response, String msg) {
        try {
            request.setAttribute("type", "alert");
            request.setAttribute("msg", msg);
            request.setAttribute("redirect", "products-servlet");
            request.getRequestDispatcher("/WEB-INF/error.jsp").include(request, response);
        } catch (ServletException | IOException e) {
            throw new RuntimeException("Errore critico durante il forward", e);
        }
    }
}