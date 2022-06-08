<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/nav-menu.css">
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>

    <script>
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

        /*
        window.addEventListener("resize", browserSize);
        function browserSize() {
            if (window.outerWidth > 1150) {
                $("div.menu").css({"visibility": "visible", "display": "block"});
                $("div.sub-menu").css({"visibility": "hidden", "display": "none"});
            }
            else
                $("div.menu").css({"visibility": "hidden", "display": "none"});

        }*/

        function showMenu() {
            $("div.menu").slideToggle();
        }

        function showSubMenu() {
            $("div.sub-menu").slideToggle();
        }
    </script>

</head>
<body>

    <div class="navigation-bar">
        <div class="show-menu" onclick="showMenu()"><i class="fa-solid fa-bars"></i></div>
        <div class="menu">
            <div class="item-menu item-logo"><img src="img/Logo_Scritta.png" class="logo" alt="logo"></div>
            <div class="item-menu" onclick="location.href='index.jsp'"><i class="fa-solid fa-house iconMenu"></i>HomePage</div>
            <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-book-open iconMenu"></i>Catalogo</div>
            <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-dollar-sign iconMenu"></i>Offerte</div>
            <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-cart-shopping iconMenu"></i>Carrello</div>
            <div class="item-menu" onclick="location.href='#'"><i class="fa-solid fa-circle-info iconMenu"></i>Chi siamo</div>
            <div class="item-menu" onclick="<%= url %>" style="position: relative"><i class="fa-solid fa-user iconMenu"></i><%= utente %>
                <div class="sub-menu">
                    <div class="item-submenu" onclick="location.href='#'">Profilo</div>
                    <div class="item-submenu" onclick="location.href='#'">Ordini</div>
                    <div class="item-submenu" onclick="location.href='logout-servlet'">Logout</div>
                </div>
            </div>
        </div>
    </div>

</body>