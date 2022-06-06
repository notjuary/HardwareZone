<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/nav-menu.css">
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/menu.js"></script>

    <script>
        function showSubMenu() {
            //noinspection JSJQueryEfficiency
            let sub_menu = $("div.sub-menu").css("visibility");
            if (sub_menu === "hidden")
                $("div.sub-menu").css({"visibility": "visible", "display": "block"});
            else
                $("div.sub-menu").css({"visibility": "hidden", "display": "none"});
        }
    </script>

</head>
<body>

    <%
        String utente = (String) session.getAttribute("nome-utente");
        String url;

        if (utente == null) {
            utente = "Accesso";
            url = "location.href='login.jsp'";
        } else {
            url = "showSubMenu()";
        }
    %>

    <div class="navigation-bar">
        <div class="item-menu" onclick="location.href='index.jsp'"><i class="fa-solid fa-house iconMenu"></i>HomePage</div>
        <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-book-open iconMenu"></i>Catalogo</div>
        <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-dollar-sign iconMenu"></i>Offerte</div>
        <div class="item-menu" onclick="<%= url %>" style="position: relative"><i class="fa-solid fa-user iconMenu"></i><%= utente %>
            <div class="sub-menu">
                <div class="item-submenu" onclick="location.href='#'">Profilo</div>
                <div class="item-submenu" onclick="location.href='#'">Ordini</div>
                <div class="item-submenu" onclick="location.href='logout-servlet'">Logout</div>
            </div>
        </div>
        <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-cart-shopping iconMenu"></i>Carrello</div>
        <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-circle-info iconMenu"></i>Chi siamo</div>
    </div>

</body>