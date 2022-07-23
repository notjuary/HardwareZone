<%@ page import="Model.OrderBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.OrderProductBean" %>
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

    <%@ include file="/menu.jsp"%>

    <%  @SuppressWarnings("unchecked")
        ArrayList<OrderProductBean> products = (ArrayList<OrderProductBean>) request.getAttribute("products"); %>

    <div class="containerListDiv">
        <div class="headerTable">
            <div>ID</div>
            <div>Totale</div>
        </div>

        <div class="bodyTable">
            <% for (OrderProductBean order: products) { %>
            <div class="single-item">
                <div><%= order.getProduct() %></div>
                <div><%= order.getQuantity() %></div>
            </div>
            <% } %>
        </div>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
