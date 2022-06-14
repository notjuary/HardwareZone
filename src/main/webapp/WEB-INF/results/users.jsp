<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.UserBean" %>
<%--@elvariable id="users" type="model.UserBean"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Utenti</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/users.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

</head>
<body>

    <%@ include file="/WEB-INF/menu-admin.jsp"%>

    <div class="container">
        <div class="headerTable">
            <div>ID</div>
            <div>Nome</div>
            <div>Cognome</div>
            <div>Email</div>
            <div>Stato</div>
        </div>

        <div class="bodyTable">
            <%
                ArrayList<UserBean> usersList = (ArrayList<UserBean>) request.getAttribute("users");

                for (UserBean user: usersList) { %>
                    <div class="single-user">
                    <div><%= user.getId() %></div>
                    <div><%= user.getName() %></div>
                    <div><%= user.getSurname() %></div>
                    <div><%= user.getEmail() %></div>
                    <div><%= user.isActive() %></div>
                    <div><button class="button-show-more" onclick="">Altro</button></div>
                    </div>
             <% } %>
        </div>
    </div>

</body>
</html>
