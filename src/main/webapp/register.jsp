<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Registrazione</title>

    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="style/login.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/validateForm.js"></script>
    <script type="text/javascript" src="script/eventManager.js"></script>

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

    <%@ include file="/menu.jsp"%>

    <div class="login-form">
        <form action="registrazione-servlet" method="post">

            <div class="tab-personal-information">

                <div class="nome-utente">
                    <label for="nome" class="login-text">Nome</label>
                    <input type="text" class="register" id="nome" name="nome" placeholder="Nome">
                </div>

                <div class="cognome-utente">
                    <label for="cognome" class="login-text">Cognome</label>
                    <input type="text" class="register" id="cognome" name="cognome" placeholder="Cognome">
                </div>

                <div class="data-nascita">
                    <label for="data-di-nascita" class="login-text">Data di nascita</label>
                    <input type="date" class="register" id="data-di-nascita" name="data-di-nascita" placeholder="Data di nascita">
                </div>

                <div class="login-button">
                    <input type="button" value="Avanti" class="show-tab-login-info" onclick="validatePersonalInformation()">
                </div>

            </div>

            <div class="tab-login-information">

                <div class="email-utente">
                    <label for="email" class="login-text">Email</label>
                    <input type="email" class="register" id="email" name="email" placeholder="Email">
                </div>

                <div class="password-utente">
                    <label for="password" class="login-text">Password</label>
                    <input type="password" class="register" id="password" name="password" placeholder="Password">
                </div>

                <div class="telefono-utente">
                    <label for="telefono" class="login-text">Telefono</label>
                    <input type="tel" class="register" id="telefono" name="telefono" placeholder="Telefono">
                </div>

                <div class="login-button">
                    <input type="button" value="Indietro" class="show-tab-personal-information" onclick="showTabPersonalInformation()">
                </div>

                <div class="login-button">
                    <input type="button" value="Avanti" class="show-tab-address" onclick="validateLoginInformation()">
                </div>

            </div>

            <div class="tab-address-information">

                <div class="citta-utente">
                    <label for="citta" class="login-text">Città</label>
                    <input type="text" class="register" id="citta" name="citta" placeholder="Città">
                </div>

                <div class="provincia-utente">
                    <label for="provincia" class="login-text">Provincia</label>
                    <input type="text" class="register" id="provincia" name="provincia" placeholder="Provincia">
                </div>

                <div class="codice-postale-utente">
                    <label for="codice-postale" class="login-text">Codice Postale</label>
                    <input type="text" class="register" id="codice-postale" name="codice-postale" placeholder="Codice Postale">
                </div>

                <div class="indirizzo-utente">
                    <label for="indirizzo" class="login-text">Indirizzo</label>
                    <input type="text" class="register" id="indirizzo" name="indirizzo" placeholder="Indirizzo">
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