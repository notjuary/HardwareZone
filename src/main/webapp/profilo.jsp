<%--@elvariable id="profiloJSP" type="model.UtenteBean"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Profilo</title>

    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="style/login.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <div class="login-form">
        <form action="#" method="post">

            <label for="nome" class="login-text">Nome</label>
            <input type="text" class="register" id="nome" name="nome" placeholder="${profiloJSP.getNome()}">

            <label for="cognome" class="login-text">Cognome</label>
            <input type="text" class="register" id="cognome" name="cognome" placeholder="${profiloJSP.getCognome()}">

            <label for="data-di-nascita" class="login-text">Data di nascita</label>
            <input type="text" class="register" id="data-di-nascita" name="data-di-nascita" placeholder="${profiloJSP.getDataNascita()}" onfocus="(this.type='date')">

            <label for="email" class="login-text">Email</label>
            <input type="email" class="register" id="email" name="email" placeholder="${profiloJSP.getEmail()}">

            <label for="password" class="login-text">Password</label>
            <input type="password" class="register" id="password" name="password" placeholder="Password">

            <label for="telefono" class="login-text">Telefono</label>
            <input type="tel" class="register" id="telefono" name="telefono" placeholder="${profiloJSP.getTelefono()}">

            <label for="citta" class="login-text">Città</label>
            <input type="text" class="register" id="citta" name="citta" placeholder="${profiloJSP.getCitta()}">

            <label for="provincia" class="login-text">Provincia</label>
            <input type="text" class="register" id="provincia" name="provincia" placeholder="${profiloJSP.getProvincia()}">

            <label for="codice-postale" class="login-text">Codice Postale</label>
            <input type="text" class="register" id="codice-postale" name="codice-postale" placeholder="${profiloJSP.getCodicePostale()}">

            <label for="indirizzo" class="login-text">Indirizzo</label>
            <input type="text" class="register" id="indirizzo" name="indirizzo" placeholder="${profiloJSP.getIndirizzo()}">

            <br/>
            <input type="button" value="Annulla" class="show-tab-login-info" onclick="location.href='index.jsp'">
            <br/>
            <input type="submit" value="Salva modifiche" class="submit-register" style="margin-top: 16px !important;">

        </form>
    </div>

</body>
</html>
