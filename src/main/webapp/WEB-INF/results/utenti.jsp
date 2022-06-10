<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.UtenteBean" %><%--@elvariable id="utenti" type="model.UtenteBean"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Utenti</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

</head>
<body>

    <%@ include file="/WEB-INF/adminmenu.jsp"%>

    <div class="container">
        <div class="headerTable">
            <div>ID</div>
            <div>Nome</div>
            <div>Cognome</div>
            <div>Email</div>
            <div>Stato</div>
        </div>

        <div class="bodyTable">
            <c:forEach items="${utenti}" var="utente">
                <div>${utente.id}</div>
                <div>${utente.nome}</div>
                <div>${utente.cognome}</div>
                <div>${utente.email}</div>
                <div>Altro</div>
            </c:forEach>
        </div>
    </div>

</body>
</html>
