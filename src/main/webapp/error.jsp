<%--@elvariable id="error" type=""--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Error Page</title>
</head>
<body>

    <%
        String error = (String) request.getAttribute("error");
    %>

    <div class="container-message">
        <span><%=error%></span>
    </div>

</body>
</html>
