<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
            <c:forEach items="${users}" var="user">
                <div class="single-user">
                    <div>${user.id}</div>
                    <div>${user.name}</div>
                    <div>${user.surname}</div>
                    <div>${user.email}</div>
                    <div>${user.state}</div>
                    <div><button class="button-show-more" onclick="">Altro</button></div>
                </div>
            </c:forEach>
        </div>
    </div>

</body>
</html>
