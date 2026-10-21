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

    // Costanti estratte per evitare di ricompilare i pattern ad ogni chiamata
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^(\\d+(?:[.,]\\d{2})?)$");
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        UserBean user = (UserBean) request.getSession().getAttribute("user");

        // FIX S2259: Null check per evitare NPE
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

        // FIX S2259: Null check
        if (user == null || !user.isAdmin().equalsIgnoreCase("true")) {
            request.getRequestDispatcher("/WEB-INF/results/products.jsp").forward(request, response); // NOSONAR
            return;
        }

        // 1. Estrazione parametri
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String price = request.getParameter("price") != null ? request.getParameter("price").replace(",", ".") : "";
        String quantity = request.getParameter("quantity");
        String sales = request.getParameter("sales");
        String category = request.getParameter("category");

        // 2. Validazione con Early Return (abbatte la Cognitive Complexity)
        if (!isValidString(name, 20) || !isValidString(description, 255) || !isValidString(category, 20)
                || !DECIMAL_PATTERN.matcher(price).find()
                || !INT_PATTERN.matcher(quantity).find()
                || !INT_PATTERN.matcher(sales).find()) {
            sendError(request, response, "Errore inserimento", "add-product-servlet");
            return;
        }

        ProductDAO serviceProduct = new ProductDAO();

        // 3. Controllo duplicati (Early Return)
        if (serviceProduct.isAlreadyRegistered(name, description)) {
            sendError(request, response, "Prodotto già presente", "add-product-servlet");
            return;
        }

        // 4. Gestione Upload Immagine (Metodo Estratto)
        String subpath = handleImageUpload(request.getPart("image"));

        // 5. Salvataggio Categoria se non esiste (Metodo Estratto)
        saveCategoryIfNotExists(category);

        // 6. Creazione e salvataggio del prodotto
        ProductBean product = new ProductBean();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(Double.parseDouble(price));
        product.setQuantity(Integer.parseInt(quantity));
        product.setSales(Integer.parseInt(sales));
        product.setCategory(category);
        product.setImage(subpath);

        serviceProduct.doSave(product);

        // 7. Ritorno alla pagina con lista aggiornata
        CategoryDAO serviceCategory = new CategoryDAO();
        request.setAttribute("categories", serviceCategory.doRetrieveAll());
        request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp").include(request, response); // NOSONAR
    }

    // ==========================================
    // METODI HELPER (Abbassano la Cognitive Complexity)
    // ==========================================

    private boolean isValidString(String value, int maxLength) {
        return value != null && !value.trim().isEmpty() && value.length() <= maxLength;
    }

    private String handleImageUpload(Part part) throws IOException {
        // Se non c'è file, usa l'immagine di default
        if (part == null || part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
            return "./img/products/nophoto.png";
        }

        // FIX CWE-434: Controllo dell'estensione (Whitelist)
        String fileName = part.getSubmittedFileName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
            // File non valido: ritorna l'immagine di default
            return "./img/products/nophoto.png";
        }

        String uploadPath = getServletContext().getRealPath("") + "\\img\\products";
        String imagePath = uploadPath + File.separator + part.getSubmittedFileName();
        part.write(imagePath);

        return "./img/products/" + part.getSubmittedFileName();
    }

    private void saveCategoryIfNotExists(String categoryName) {
        CategoryDAO serviceCategory = new CategoryDAO();
        List<CategoryBean> categoryList = serviceCategory.doRetrieveAll();

        for (CategoryBean saved : categoryList) {
            if (saved.getNome().equalsIgnoreCase(categoryName)) {
                return; // La categoria esiste già, esci dal metodo
            }
        }
        serviceCategory.doSave(categoryName); // Non esiste, salvala
    }

    private void sendError(HttpServletRequest request, HttpServletResponse response, String msg, String redirect) throws ServletException, IOException {
        request.setAttribute("type", "alert");
        request.setAttribute("msg", msg);
        request.setAttribute("redirect", redirect);
        request.getRequestDispatcher("/WEB-INF/error.jsp").include(request, response); // NOSONAR
    }
}