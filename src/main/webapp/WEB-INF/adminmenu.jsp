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
                url = "showSubMenu(this)";
            }
        %>

        window.addEventListener("resize", browserSize);
        function browserSize() {
            if (window.outerWidth <= 1160)
                $("div.menu").hide();
            else
                $("div.menu").show();
        }

        $(document).ready(function() {
            $("div.sub-menu").hide();
            currentPage();
        });

        function showMenu() {
            $("div.menu").slideToggle();
        }

        function showSubMenu(element) {
            let elementAttribute = element.getAttribute("data-name");

            $(".sub-menu").each(function() {


                if (elementAttribute === (this.getAttribute("data-name")))
                    $(this).slideToggle();
                else
                    $(this).hide();
            });
        }

        function currentPage() {

            let currentPage = document.title;

            $(".admin-item-menu").each(function() {

                if (currentPage === (this.getAttribute("data-name"))) {
                    $(this).closest("div").addClass("active");
                }
            });
        }
    </script>

</head>
<body>

<div class="navigation-bar">
    <div class="show-menu" onclick="showMenu()"><i class="fa-solid fa-bars"></i></div>
    <div class="menu">
        <div class="admin-item-menu item-logo" onclick="location.href='#'"><img src="${pageContext.request.contextPath}/img/Logo_NoScritta.png" class="logo" alt="logo"></div>
        <div class="admin-item-menu" onclick="location.href='utenti-servlet'" data-name="HomePage"><i class="fa-solid fa-house iconMenu"></i>Utenti</div>
        <div class="admin-item-menu" onclick="location.href='#'" data-name="Ordini"><i class="fa-solid fa-book-open iconMenu"></i>Ordini</div>
        <div class="admin-item-menu" onclick="showSubMenu(this)" style="position: relative" data-name="Prodotti"><i class="fa-solid fa-dollar-sign iconMenu"></i>Prodotti
            <div class="sub-menu" data-name="Prodotti">
                <div class="item-submenu" onclick="location.href='#'">Aggiungi</div>
                <div class="item-submenu" onclick="location.href='logout-servlet'">Gestione</div>
            </div>
        </div>
        <div class="admin-item-menu" onclick="<%= url %>" style="position: relative" data-name="Login"><i class="fa-solid fa-user iconMenu"></i><%= utente %>
            <div class="sub-menu" data-name="Login">
                <div class="item-submenu" onclick="location.href='#'">Profilo</div>
                <div class="item-submenu" onclick="location.href='logout-servlet'">Logout</div>
            </div>
        </div>
    </div>
</div>

</body>