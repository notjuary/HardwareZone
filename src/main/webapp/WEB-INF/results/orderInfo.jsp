<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Ordine</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/order.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

</head>
<body>

<%@ include file="/WEB-INF/admin/menu-admin.jsp"%>

<%  @SuppressWarnings("unchecked")
ArrayList<OrderProductBean> products = (ArrayList<OrderProductBean>) request.getAttribute("products"); %>

<%  @SuppressWarnings("unchecked")
ArrayList<ProductBean> catalog = (ArrayList<ProductBean>) request.getAttribute("catalog"); %>

<div class="containerListDiv">
    <div class="return-orders"><a href="/ecommerce_war_exploded/show-order-user-servlet">Indietro</a></div>
    <div class="headerTable">
        <div>Nome</div>
        <div>Quantità</div>
    </div>

    <div class="bodyTable">
        <% for (OrderProductBean productOrder: products) { %>
        <div class="single-item">
            <% for (ProductBean productBean: catalog) { %>
            <% if (productBean.getId() == productOrder.getProduct()) { %>
            <div><%= productBean.getName() %></div>
            <div><%= productOrder.getQuantity() %></div>
            <% } %>
            <% } %>
        </div>
        <% } %>
    </div>
</div>

<%@ include file="/footer.jsp"%>

</body>
</html>