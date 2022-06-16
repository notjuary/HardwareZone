<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Registrazione</title>

    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="style/login.css">
    <link rel="stylesheet" type="text/css" href="style/footer.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/validateForm.js"></script>
    <script type="text/javascript" src="script/eventManager.js"></script>

    <%
        if (request.getSession().getAttribute("user") != null) {
            response.sendRedirect("index.jsp");
        }
    %>

</head>
<body>

    <%@ include file="menu.jsp"%>

    <div class="login-form">
        <form action="registration-servlet" method="post">

            <div class="tab-personal-information">

                <div class="user-name">
                    <label for="name" class="login-text">Nome</label>
                    <input type="text" class="register" id="name" name="name" placeholder="Nome">
                </div>

                <div class="user-surname">
                    <label for="surname" class="login-text">Cognome</label>
                    <input type="text" class="register" id="surname" name="surname" placeholder="Cognome">
                </div>

                <div class="birthday">
                    <label for="birthday" class="login-text">Data di nascita</label>
                    <input type="date" class="register" id="birthday" name="birthday" placeholder="Data di nascita">
                </div>

                <div class="login-button">
                    <input type="button" value="Avanti" class="show-tab-login-info" onclick="validatePersonalInformation()">
                </div>

            </div>

            <div class="tab-login-information">

                <div class="user-email">
                    <label for="email" class="login-text">Email</label>
                    <input type="email" class="register" id="email" name="email" placeholder="Email">
                </div>

                <div class="user-password">
                    <label for="password" class="login-text">Password</label>
                    <input type="password" class="register" id="password" name="password" placeholder="Password">
                </div>

                <div class="user-phone">
                    <label for="phone" class="login-text">Telefono</label>
                    <input type="tel" class="register" id="phone" name="phone" placeholder="Telefono">
                </div>

                <div class="login-button">
                    <input type="button" value="Indietro" class="show-tab-personal-information" onclick="showTabPersonalInformation()">
                </div>

                <div class="login-button">
                    <input type="button" value="Avanti" class="show-tab-address" onclick="validateLoginInformation()">
                </div>

            </div>

            <div class="tab-address-information">

                <div class="user-city">
                    <label for="city" class="login-text">Città</label>
                    <input type="text" class="register" id="city" name="city" placeholder="Città">
                </div>

                <div class="user-province">
                    <label for="province" class="login-text">Provincia</label>
                    <input type="text" class="register" id="province" name="province" placeholder="Provincia">
                </div>

                <div class="user-postalCode">
                    <label for="postalCode" class="login-text">Codice Postale</label>
                    <input type="text" class="register" id="postalCode" name="postalCode" placeholder="Codice Postale">
                </div>

                <div class="user-address">
                    <label for="address" class="login-text">Indirizzo</label>
                    <input type="text" class="register" id="address" name="address" placeholder="Indirizzo">
                </div>

                <div class="login-button">
                    <input type="button" value="Indietro" class="show-tab-login-info" onclick="showTabLoginInformation()">
                </div>

                <div class="login-button">
                    <input type="button" value="Avanti" class="show-review-login-info" onclick="validateAddressInformation()">
                </div>

            </div>

            <div class="tab-review-information">
                <p class="login-text">Conferma registrazione</p>

                <div class="login-button">
                    <input type="button" value="Annulla" class="show-tab-login-info" onclick="showTabPersonalInformation()">
                </div>

                <div class="register-button">
                    <input type="submit" value="Crea un account" class="submit-register">
                </div>
            </div>

        </form>

        <p class="login-text">Hai già un account? <a href="login.jsp">Accedi</a></p>
    </div>

</body>
</html>