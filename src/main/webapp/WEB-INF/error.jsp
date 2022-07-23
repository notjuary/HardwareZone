<%@ page isErrorPage="true" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Errore</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

</head>
<body onload="updateCount()">

    <%@ include file="/menu.jsp"%>

    <script>
        let count = 11;

        function updateCount() {
            count = count - 1;
            document.getElementById("timer").innerHTML = count;
            setTimeout(updateCount, 1000);
        }

        setTimeout("window.location.href='<%= (String) request.getAttribute("redirect") %>'", 5000)
    </script>

    <div class="<%= (String) request.getAttribute("type") %>">
        <h1><%= (String) request.getAttribute("msg") %></h1>
        <h2><a href="<%= (String) request.getAttribute("redirect") %>" class="error">Procedi</a></h2>
        <span id="timer" class="error"></span>
    </div>

</body>
</html>
