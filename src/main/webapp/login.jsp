<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Login</title>
    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="style/login.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/menu.js"></script>
    <script type="text/javascript" src="script/validateform.js"></script>

</head>
<body>

    <%@ include file="/resources/menu.jsp"%>

    <div class="login-form">
        <form action="login-servlet" method="post">

            <label for="email" class="login-text">Email</label>
            <input type="email" class="login" id="email" name="email" placeholder="Email">
            <label for="password" class="login-text">Password</label>
            <input type="password" class="login" id="password" name="password" placeholder="Password">

            <br/>
            <input type="submit" value="Login" class="submit-login">
        </form>

        <p class="login-text">Nuovo utente?</p>
        <input type="submit" value="Crea un account" class="submit-register" onclick="location.href='register.jsp'">
    </div>

</body>
</html>