<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Login</title>

    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="style/login.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/validateForm.js"></script>
    <script type="text/javascript" src="script/eventManager.js"></script>

</head>
<body>

    <%@ include file="menu.jsp"%>

    <%
        if (myProfile != null) {
            response.sendRedirect("index.jsp");
        }
    %>

    <div class="login-form">
        <form action="login-servlet" method="post" id="loginForm">
            <div class="user-email">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" placeholder="Email">
            </div>

            <div class="user-password">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Password">
            </div>

            <div class="login-button">
                <input type="button" value="Login" onclick="validateLogin()">
            </div>

            <div class="register-button">
                <span>Nuovo utente? <a href="register.jsp"><br/>Crea un account</a></span>
            </div>
        </form>
    </div>

</body>
</html>