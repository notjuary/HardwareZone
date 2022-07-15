<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.CategoryBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Aggiungi Prodotto</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/add-product.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/validateForm.js"></script>

    <script>
        function imageUpload(event) {
            $(".fa-file-circle-xmark").addClass("fa-solid fa-file-circle-check").removeClass("fa-file-circle-xmark");
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
        <form action="${pageContext.request.contextPath}/add-product-servlet" method="post" enctype="multipart/form-data" id="add-product-form">
            <div class="containerProduct">
                <div class="product-name">
                    <label for="name">Nome</label>
                    <input type="text" id="name" name="name" class="product" placeholder="Nome" maxlength="20" required>
                </div>

                <div class="product-description">
                    <label for="description">Descrizione</label>
                    <textarea id="description" name="description" class="product" placeholder="Descrizione" rows="6" maxlength="255" required></textarea>
                </div>

                <div class="product-price">
                    <label for="price">Prezzo</label>
                    <input type="text" id="price" name="price" class="product" placeholder="Prezzo" pattern="^[1-9]\d*(\.\d+)?$" required>
                </div>

                <div class="product-quantity">
                    <label for="quantity">Quantità</label>
                    <input type="text" id="quantity" name="quantity" class="product" placeholder="Quantità" pattern="[0-9]+" required>
                </div>

                <div class="product-sales">
                    <label for="sales">Sconto</label>
                    <input type="text" id="sales" name="sales" class="product" placeholder="Sconto" pattern="[0-9]+" required>
                </div>

                <div class="product-image">
                    <label for="image" id="imageUpload">Immagine
                    <i class="fa-solid fa-file-circle-xmark"></i>
                    <input type="file" id="image" name="image" class="product" onchange="imageUpload(event)" accept="image/*" required></label>
                    <img src="" alt="" class="preview">
                </div>

                <div class="product-category">
                    <label for="category">Categoria</label>
                    <input list="category_name" id="category" name="category" class="product" placeholder="Categoria" maxlength="20" required>
                    <datalist id="category_name">
                        <% for (CategoryBean category: listCategories) { %>
                            <option value="<%= category.getNome() %>">
                        <% } %>
                    </datalist>
                </div>

                <div class="add-product">
                    <input type="button" onclick="validateAddProduct()" value="Inserisci">
                </div>
            </div>
        </form>
    </div>

</body>
</html>
