<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.UserBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Utenti</title>

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
            <div>Cognome</div>
            <div>Email</div>
            <div>Stato</div>
        </div>

        <div class="bodyTable">

            <%  @SuppressWarnings("unchecked")
                ArrayList<UserBean> usersList = (ArrayList<UserBean>) request.getAttribute("users");

            for (UserBean user: usersList) { %>
                <div class="single-item">

                    <%
                        String active, activeText;
                        if (user.isActive().equalsIgnoreCase("true")) {
                            active = "Disattiva";
                            activeText = "Attivo";
                        }
                        else {
                            active = "Attiva";
                            activeText = "Disattivo";
                        }
                    %>

                    <div><%= user.getId() %></div>
                    <div><%= user.getName() %></div>
                    <div><%= user.getSurname() %></div>
                    <div><%= user.getEmail() %></div>
                    <div><%= activeText %></div>

                    <div class="set-state"><a href="${pageContext.request.contextPath}/set-state-user-servlet?id=<%= user.getId() %>&active=<%= user.isActive()%>"><%= active %></a></div>
                    <div class="show-more"><a href="${pageContext.request.contextPath}/user-info-servlet?id=<%= user.getId() %>">Altro <i class="fas fa-info-circle"></i></a></div>

                </div>
            <% } %>
        </div>
    </div>

</body>
</html>
