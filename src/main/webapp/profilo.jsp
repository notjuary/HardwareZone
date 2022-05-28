<%--@elvariable id="profiloJSP" type="model.UtenteBean"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

    <%@ include file="/resources/menu.jsp"%>
    <p>Utente connesso: ${profiloJSP.nome} ${profiloJSP.cognome}</p>

</body>
</html>
