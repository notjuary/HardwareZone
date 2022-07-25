<%@ page import="Model.OrderBean" %>
<%@ page import="java.util.ArrayList" %>
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

</head>
<body>

    <%@ include file="/WEB-INF/admin/menu-admin.jsp"%>

    <%  @SuppressWarnings("unchecked")
    ArrayList<OrderBean> orderBean = (ArrayList<OrderBean>) request.getAttribute("orders"); %>

    <div class="containerListDiv">
      <div class="headerTableAdmin">
        <div>ID</div>
        <div>Totale</div>
      </div>

      <div class="bodyTable">
        <% for (OrderBean order: orderBean) { %>
        <div class="single-item-admin">
          <div><%= order.getId() %></div>
          <div><%= String.format("%.2f", order.getTotal()) %></div>
          <div class="show-more"><a href="${pageContext.request.contextPath}/order-user-info-servlet?id=<%= order.getId() %>">Altro <i class="fas fa-info-circle"></i></a></div>
        </div>
        <% } %>
      </div>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
