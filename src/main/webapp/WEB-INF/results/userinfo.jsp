<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Model.UserBean" %>
<%--@elvariable id="profileJSP" type="model.UtenteBean"--%>
<html>
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">

    <% UserBean user = (UserBean) request.getAttribute("profileJSP"); %>

    <title><%= user.getId() + " - " + user.getName() + " " + user.getSurname() %></title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/profile.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

</head>
<body>

    <%@ include file="/WEB-INF/admin/menu-admin.jsp"%>

    <div class="edit-form">

        <div class="back">
            <a href="${pageContext.request.contextPath}/users-servlet">Indietro</a>
        </div>

        <div class="setAdmin">
            <a href="${pageContext.request.contextPath}/set-admin-servlet?id=<%= user.getId() %>">Rendi amministratore</a>
        </div>

        <div class="container">

            <div class="personal-information">

                <label class="text-section">Dati personali</label>

                <label for="name" class="login-text">Nome</label>
                <input type="text" class="register" id="name" name="name" placeholder="${profileJSP.getName()}" readonly>

                <label for="surname" class="login-text">Cognome</label>
                <input type="text" class="register" id="surname" name="surname" placeholder="${profileJSP.getSurname()}" readonly>

                <label for="birthday" class="login-text">Data di nascita</label>
                <input type="text" class="register" id="birthday" name="birthday" placeholder="${profileJSP.getBirthday()}" readonly>

                <label for="admin" class="login-text">Admin</label>
                <input type="text" class="register" id="admin" name="admin" placeholder="${profileJSP.isAdmin()}" readonly>

            </div>

            <div class="login-information">

                <label class="text-section">Dati accesso</label>

                <label for="email" class="login-text">Email</label>
                <input type="email" class="register" id="email" name="email" placeholder="${profileJSP.getEmail()}" readonly>

                <label for="password" class="login-text">Password</label>
                <input type="password" class="register" id="password" name="password" placeholder="Password" readonly>

                <label for="phone" class="login-text">Telefono</label>
                <input type="tel" class="register" id="phone" name="phone" placeholder="${profileJSP.getPhone()}" readonly>

                <label for="state" class="login-text">Stato</label>
                <input type="text" class="register" id="state" name="state" placeholder="${profileJSP.isActive()}" readonly>

            </div>

            <div class="address-information">

                <label class="text-section">Indirizzo</label>

                <label for="city" class="login-text">Città</label>
                <input type="text" class="register" id="city" name="city" placeholder="${profileJSP.getCity()}" readonly>

                <label for="province" class="login-text">Provincia</label>
                <input type="text" class="register" id="province" name="province" placeholder="${profileJSP.getProvince()}" readonly>

                <label for="postalCode" class="login-text">Codice Postale</label>
                <input type="text" class="register" id="postalCode" name="postalCode" placeholder="${profileJSP.getPostalCode()}" readonly>

                <label for="address" class="login-text">Indirizzo</label>
                <input type="text" class="register" id="address" name="address" placeholder="${profileJSP.getAddress()}" readonly>

            </div>

        </div>
    </div>

</body>
</html>
