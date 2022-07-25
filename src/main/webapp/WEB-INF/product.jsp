<%@ page import="Model.ProductBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <% ProductBean product = (ProductBean) request.getAttribute("product"); %>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">

    <title><%= product.getName() %></title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/product.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/product.js"></script>

</head>
<body>

<%@ include file="/menu.jsp"%>

    <div style="display: flex">
        <div class="productCard">
            <div class="image"><img src="<%= product.getImage() %>" alt="<%= product.getName() %>"></div>
            <div class="name"><%= product.getName() %></div>

            <% if (product.getSales() > 0) {
                double sale = product.getPrice() - ((product.getPrice() * product.getSales() / 100)); %>
            <div class="price onSale"><span style="color: black; text-decoration: line-through;">€<%= String.format("%.2f", product.getPrice()) %></span> €<%= String.format("%.2f", sale) %> -<%= product.getSales() %>%</div>
            <% } else { %>
            <div class="price">€<%= String.format("%.2f", product.getPrice()) %></div>
            <% } %>

            <div class="name"><%= product.getDescription() %></div>

            <% if (product.getQuantity() > 0) { %>
            <form action="${pageContext.request.contextPath}/add-to-cart-servlet" method="get">
                <input type="hidden" name="productId" id="productId" value="<%= product.getId() %>">
                <label>
                    <input type="number" name="quantity" id="quantity" value="1" min="1" max="<%= product.getQuantity() %>">
                </label>

                <label>
                    <input type="button" onclick="addProductCard(document.getElementById('productId').value, document.getElementById('quantity').value)" value="Aggiungi al carrello">
                </label>
            </form>
            <% } else { %>
                <p style="text-align: center">Prodotto esaurito</p>
            <% } %>
        </div>
    </div>


<%@ include file="/footer.jsp"%>

</body>
</html>
