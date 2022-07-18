<%@ page import="Model.ProductBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>

    <% ProductBean product = (ProductBean) request.getAttribute("product"); %>

    <title><%= product.getName() %></title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/catalog.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/product.js"></script>

</head>
<body>

    <%@ include file="/menu.jsp"%>

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
        <div><a class="shop" onclick="addProductCard(<%= product.getId() %>, 1)"><i class="fa-solid fa-cart-plus"></i></a></div>
        <% } else {%>
        <div><i class="fa-solid fa-ban"></i></div>
        <% } %>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
