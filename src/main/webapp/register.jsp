<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Register</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="style/login.css">
    <link rel="stylesheet" type="text/css" href="style/nav-menu.css">
    <script type="text/javascript" src="script/script.js"></script>

    <script>
        $(document).ready(function() {
            $(".tab-personal-information").show();
            $(".tab-login-information").hide();
            $(".tab-address-information").hide();
            $(".tab-review-information").hide();
        });
    </script>

</head>
<body>

    <%@ include file="/resources/menu.jsp"%>

    <div class="login-form">
        <form action="registrazione-servlet" method="post">

            <div class="tab-personal-information">
                <label for="nome" class="login-text">Nome</label>
                <input type="text" class="register" id="nome" name="nome" placeholder="Nome">
                <label for="cognome" class="login-text">Cognome</label>
                <input type="text" class="register" id="cognome" name="cognome" placeholder="Cognome">
                <label for="data-di-nascita" class="login-text">Data di nascita</label>
                <input type="date" class="register" id="data-di-nascita" name="data-di-nascita" placeholder="Data di nascita">

                <br/>
                <input type="button" value="Avanti" class="show-tab-login-info" onclick="validatePersonalInformation()"><br/>
            </div>

            <div class="tab-login-information">
                <label for="email" class="login-text">Email</label>
                <input type="email" class="register" id="email" name="email" placeholder="Email">
                <label for="password" class="login-text">Password</label>
                <input type="password" class="register" id="password" name="password" placeholder="Password">
                <label for="telefono" class="login-text">Telefono</label>
                <input type="tel" class="register" id="telefono" name="telefono" placeholder="Telefono">

                <br/>
                <input type="button" value="Indietro" class="show-tab-personal-information" onclick="showTabPersonalInformation()"><br/>
                <input type="button" value="Avanti" class="show-tab-address" onclick="validateLoginInformation()"><br/>
            </div>

            <div class="tab-address-information">
                <label for="citta" class="login-text">Città</label>
                <input type="text" class="register" id="citta" name="citta" placeholder="Città">
                <label for="provincia" class="login-text">Provincia</label>
                <input type="text" class="register" id="provincia" name="provincia" placeholder="Provincia">
                <label for="codice-postale" class="login-text">Codice Postale</label>
                <input type="text" class="register" id="codice-postale" name="codice-postale" placeholder="Codice Postale">
                <label for="indirizzo" class="login-text">Indirizzo</label>
                <input type="text" class="register" id="indirizzo" name="indirizzo" placeholder="Indirizzo">

                <br/>
                <input type="button" value="Indietro" class="show-tab-login-info" onclick="showTabLoginInformation()"><br/>
                <input type="button" value="Avanti" class="show-review-login-info" onclick="validateAddressInformation()"><br/>
            </div>

            <div class="tab-review-information">
                <p class="login-text">Conferma registrazione</p>
                <input type="button" value="Annulla" class="show-tab-login-info" onclick="showTabPersonalInformation()"><br/>
                <input type="submit" value="Crea un account" class="submit-register" style="margin-top: 16px !important;">
            </div>

        </form>

        <p class="login-text">Hai già un account? <a href="login.jsp">Accedi</a></p>
    </div>

</body>
</html>