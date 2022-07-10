<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.CategoryBean" %>
<%@ page import="Model.ProductBean" %>
<%@ page import="java.util.Locale" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <%  @SuppressWarnings("unchecked")
        ProductBean product = (ProductBean) request.getAttribute("productJSP"); %>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title><%= product.getId() %> <%= product.getName() %></title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/add-product.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

    <script>
        function imageUpload(event) {
            let src = URL.createObjectURL(event.target.files[0]);
            $(".preview").attr("src", src);
        }
    </script>

    <%  @SuppressWarnings("unchecked")
        ArrayList<CategoryBean> listCategories = (ArrayList<CategoryBean>) request.getAttribute("categories"); %>

</head>
<body>

    <%@ include file="/WEB-INF/admin/menu-admin.jsp"%>

    <div class="containerProductDiv">
        <form action="${pageContext.request.contextPath}/edit-product-servlet" method="post" enctype="multipart/form-data">
            <div class="containerProduct">
                <label>
                    <input type="text" value="<%= product.getId() %>" name="id" hidden>
                </label>
                <div class="product-name">
                    <label for="name">Nome</label>
                    <input type="text" id="name" name="name" class="product" placeholder="<%= product.getName() %>" value="<%= product.getName() %>" maxlength="20" required>
                </div>

                <div class="product-description">
                    <label for="description">Descrizione</label>
                    <textarea id="description" name="description" class="product" placeholder="<%= product.getDescription() %>" rows="6" maxlength="255" required><%= product.getDescription() %></textarea>
                </div>

                <div class="product-price">
                    <label for="price">Prezzo</label>
                    <input type="text" id="price" name="price" class="product" placeholder="<%= String.format("%.2f", product.getPrice()) %>" value="<%= String.format(Locale.US, "%.2f", product.getPrice()) %>" pattern="^[1-9]\d*(\.\d+)?$" required>
                </div>

                <div class="product-quantity">
                    <label for="quantity">Quantità</label>
                    <input type="text" id="quantity" name="quantity" class="product" placeholder="<%= product.getQuantity() %>" value="<%= product.getQuantity() %>" pattern="[0-9]+" required>
                </div>

                <div class="product-sales">
                    <label for="sales">Sconto</label>
                    <input type="text" id="sales" name="sales" class="product" placeholder="<%= product.getSales() %>" value="<%= product.getSales() %>" pattern="[0-9]+" required>
                </div>

                <div class="product-image">
                    <label for="image" id="imageUpload">Immagine
                        <i class="fa-solid fa-file-arrow-up"></i>
                        <input type="file" id="image" name="image" class="product" accept="image/*" onchange="imageUpload(event)"></label>
                        <img src="<%= product.getImage() %>" alt="<%= product.getName() %>" class="preview">
                </div>

                <div class="product-category">
                    <label for="category">Categoria</label>
                    <input list="category_name" id="category" name="category" class="product" placeholder="<%= product.getCategory() %>" value="<%= product.getCategory() %>" maxlength="20" required>
                    <datalist id="category_name">
                        <% for (CategoryBean category: listCategories) { %>
                        <option value="<%= category.getNome() %>">
                                <% } %>
                    </datalist>
                </div>

                <div class="add-product">
                    <input type="submit" value="Modifica">
                </div>
            </div>
        </form>
    </div>

</body>
</html>
