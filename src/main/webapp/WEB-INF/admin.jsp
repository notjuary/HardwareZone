<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Model.UserBean" %>
<%@ page import="java.io.PrintWriter" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Admin Page</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

</head>
<body>

    <%@ include file="/WEB-INF/menu-admin.jsp"%>

</body>
</html>
