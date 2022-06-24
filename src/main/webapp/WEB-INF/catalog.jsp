<%@ page import="Model.ProductBean" %>
<%@ page import="java.util.ArrayList" %>
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
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <% ArrayList<ProductBean> productsList = (ArrayList<ProductBean>) request.getAttribute("products"); %>

    <div class="container">
        <% for (ProductBean product: productsList) { %>
            <div class="productCard">
                <div class="image"><img src="./img/<%= product.getImage() %>" alt="<%= product.getName() %>"></div>
                <div class="name"><%= product.getName() %></div>
                <div class="price">€<%= product.getPrice() %></div>
            </div>
        <% } %>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
