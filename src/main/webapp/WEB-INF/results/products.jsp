<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.ProductBean" %>
<%@ page import="Model.CategoryBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Prodotti</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/list.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>

</head>
<body>

    <%@ include file="/WEB-INF/admin/menu-admin.jsp"%>

    <div class="containerListDiv">
        <div class="headerTable">
            <div>ID</div>
            <div>Nome</div>
            <div>Prezzo</div>
            <div>Sconto</div>
            <div>Prezzo Finale</div>
            <div>Quantità</div>
        </div>

        <div class="bodyTable">

            <%  @SuppressWarnings("unchecked")
                ArrayList<ProductBean> productList = (ArrayList<ProductBean>) request.getAttribute("products");

            for (ProductBean product: productList) {
                double sale = product.getPrice() - ((product.getPrice() * product.getSales() / 100)); %>

                <div class="single-item">
                    <div><%= product.getId() %></div>
                    <div><%= product.getName() %></div>
                    <div>€<%= String.format("%.2f", product.getPrice()) %></div>
                    <div><%= product.getSales() %></div>
                    <div>€<%= String.format("%.2f", sale) %></div>

                    <% if (product.getQuantity() < 5) { %>
                        <div class="alertQuantity"><%= product.getQuantity() %></div>
                    <% } else { %>
                        <div><%= product.getQuantity() %></div>
                    <% } %>

                    <div class="show-more"><a href="${pageContext.request.contextPath}/product-info-servlet?id=<%= product.getId() %>">Altro <i class="fas fa-info-circle"></i></a></div>

                </div>
            <% } %>
        </div>
    </div>

</body>
</html>
