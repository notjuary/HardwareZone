<%@ page import="Model.ProductBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.CategoryBean" %>
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

    <%  @SuppressWarnings("unchecked")
        ArrayList<CategoryBean> listCategories = (ArrayList<CategoryBean>) request.getAttribute("categories"); %>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <div class="bodyCatalog">
        <div class="filterBar">
            <div class="filterPrice">
                <span>Prezzo</span>
                <div class="range">
                    <label for="min"></label>
                    <input type="number" min="1" max="10000" id="min">
                </div>
                <div class="range">
                    <label for="max"></label>
                    <input type="number" min="0" max="10000" id="max">
                </div>
            </div>

            <div class="filterCategory">
                <span>Categoria</span>
                <% for(CategoryBean category: listCategories) { %>
                    <div class="categoryName">
                        <input type="radio" name="category" id="<%= category.getNome() %>" value="<%= category.getNome() %>">
                        <label for="<%= category.getNome() %>"><%= category.getNome() %></label>
                    </div>
                <% } %>
            </div>

            <div class="filterSearch">
                <input type="button" value="Filtra" onclick="">
            </div>
        </div>

        <div class="containerProduct" id="containerProduct">
            <%  @SuppressWarnings("unchecked")
                ArrayList<ProductBean> productsList = (ArrayList<ProductBean>) request.getAttribute("products"); %>

            <% for (ProductBean product: productsList) { %>
                <div class="productCard">
                    <div class="image"><img src="<%= product.getImage() %>" alt="<%= product.getName() %>"></div>
                    <div class="name"><%= product.getName() %></div>

                    <% if (product.getSales() > 0) {
                        double sale = product.getPrice() - ((product.getPrice() * product.getSales() / 100)); %>
                        <div class="price" style="color: red"><span style="color: black; text-decoration: line-through;">€<%= String.format("%.2f", product.getPrice()) %></span> €<%= String.format("%.2f", sale) %></div>
                    <% } else { %>
                        <div class="price">€<%= String.format("%.2f", product.getPrice()) %></div>
                    <% } %>

                    <div><i class="fa-solid fa-cart-plus"></i></div>
                </div>
            <% } %>

            <div class="arrow-container">
                <div class="previous"><a onclick="nextButton()"><i class="fa-solid fa-circle-arrow-left"></i></a></div>
                <div class="next"><i class="fa-solid fa-circle-arrow-right"></i></div>
            </div>
        </div>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
