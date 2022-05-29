<%--@elvariable id="profiloJSP" type="model.UtenteBean"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Profilo</title>
    <link rel="stylesheet" type="text/css" href="style/general.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/menu.js"></script>

</head>
<body>

    <%@ include file="/resources/menu.jsp"%>

    <div>
        <p class="login-text">Utente connesso: ${profiloJSP.nome} ${profiloJSP.cognome}</p>
    </div>

</body>
</html>
