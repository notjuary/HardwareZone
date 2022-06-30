<%@ page import="Model.ProductBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.CategoryBean" %>
<%@ page import="Model.CategoryDAO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Catalogo</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/catalog.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

    <% ArrayList<CategoryBean> listCategories = (ArrayList<CategoryBean>) request.getAttribute("categories"); %>
    <% ArrayList<ProductBean> productsList = (ArrayList<ProductBean>) request.getAttribute("products"); %>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <div class="bodyCatalog">
        <div class="filterBar">
            <div class="filterPrice">
                <span>Prezzo</span>
            </div>
            <div class="filterCategory">
                <span>Categoria</span>
                <% for(CategoryBean category: listCategories) { %>
                <div class="categoryName">
                    <input type="checkbox" id="<%= category.getNome() %>">
                    <label for="<%= category.getNome() %>"><%= category.getNome() %></label>
                </div>
                <% } %>
            </div>
            <div class="filterSearch">
                <input type="button" value="Filtra" onclick="">
            </div>
        </div>

        <div class="containerProduct" id="containerProduct">
            <% for (ProductBean product: productsList) { %>
                <div class="productCard">
                    <div class="image"><img src="<%= product.getImage() %>" alt="<%= product.getName() %>"></div>
                    <div class="name"><%= product.getName() %></div>
                    <div class="price">€<%= product.getPrice() %></div>
                    <div><i class="fa-solid fa-cart-plus"></i></div>
                </div>
            <% } %>

            <div class="arrow-container">
                <div class="previous"><a href="${pageContext.request.contextPath}/show-catalog-servlet?first=<%= productsList.get(0).getId() + 9 %>&last=<%= productsList.get(productsList.size() - 1).getId() + 9 %>"><i class="fa-solid fa-circle-arrow-left"></i></a></div>
                <div class="next"><a href="${pageContext.request.contextPath}/show-catalog-servlet?first=<%= productsList.get(0).getId() + 9 %>&last=<%= productsList.get(productsList.size() - 1).getId() + 9 %>"><i class="fa-solid fa-circle-arrow-right"></i></a></div>
            </div>
        </div>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
