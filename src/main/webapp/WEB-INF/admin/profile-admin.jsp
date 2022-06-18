<%--@elvariable id="profileJSP" type="model.UtenteBean"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Profilo</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/profile.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>

</head>
<body>


<%@ include file="/menu.jsp"%>

<div class="edit-form">
    <form action="edit-profile-servlet" method="post">
        <div class="container">
            <div class="personal-information">
                <label class="text-section">Dati personali</label>

                <label for="name" class="login-text">Nome</label>
                <input type="text" class="register" id="name" name="name" placeholder="${profileJSP.getName()}" value="${profileJSP.getName()}">

                <label for="surname" class="login-text">Cognome</label>
                <input type="text" class="register" id="surname" name="surname" placeholder="${profileJSP.getSurname()}" value="${profileJSP.getSurname()}">

                <label for="birthday" class="login-text">Data di nascita</label>
                <input type="text" class="register" id="birthday" name="birthday" placeholder="${profileJSP.getBirthday()}" onfocus="(this.type='date')" value="${profileJSP.getBirthday()}">
            </div>

            <div class="login-information">
                <label class="text-section">Dati accesso</label>

                <label for="email" class="login-text">Email</label>
                <input type="email" class="register" id="email" name="email" placeholder="${profileJSP.getEmail()}" value="${profileJSP.getEmail()}">

                <label for="password" class="login-text">Password</label>
                <input type="password" class="register" id="password" name="password" placeholder="Password" required>

                <label for="phone" class="login-text">Telefono</label>
                <input type="tel" class="register" id="phone" name="phone" placeholder="${profileJSP.getPhone()}" value="${profileJSP.getPhone()}">
            </div>

            <div class="address-information">
                <label class="text-section">Indirizzo</label>

                <label for="city" class="login-text">Città</label>
                <input type="text" class="register" id="city" name="city" placeholder="${profileJSP.getCity()}" value="${profileJSP.getCity()}">

                <label for="province" class="login-text">Provincia</label>
                <input type="text" class="register" id="province" name="province" placeholder="${profileJSP.getProvince()}" value="${profileJSP.getProvince()}">

                <label for="postalCode" class="login-text">Codice Postale</label>
                <input type="text" class="register" id="postalCode" name="postalCode" placeholder="${profileJSP.getPostalCode()}" value="${profileJSP.getPostalCode()}">

                <label for="address" class="login-text">Indirizzo</label>
                <input type="text" class="register" id="address" name="address" placeholder="${profileJSP.getAddress()}" value="${profileJSP.getAddress()}">
            </div>
        </div>

        <div class="register-button"><input type="submit" value="Salva"></div>
    </form>
</div>

</body>
</html>