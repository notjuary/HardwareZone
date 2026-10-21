<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Model.UserBean" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <title>Admin Menu</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/nav-menu.css">
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>

    <script>
        <%
            UserBean myProfile = (UserBean) session.getAttribute("user");

            String name, url;

            if (myProfile == null) {
                name = "Accesso";
                url = "location.href='login.jsp'";
            } else {
                name = myProfile.getName();
                url = "showSubMenu(this)";
            }

        %>

        $(document).ready(function() {
            $("div.sub-menu").hide();
            currentPage();
        });

        window.addEventListener("resize", browserSize);
        function browserSize() {
            if (window.outerWidth <= 1160) {
                $("div.menu").hide();
                $("div.sub-menu").hide();
            }
            else
                $("div.menu").show();
        }

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

        // FIX ACCESSIBILITÀ: Permette di attivare i div interattivi con la tastiera
        function handleKeyPress(event, element) {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                element.click();
            }
        }
    </script>

</head>
<body>

<div class="navigation-bar">
    <!-- FIX: Aggiunti role, tabindex e onkeydown -->
    <div class="show-menu" onclick="showMenu()" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)"><i class="fa-solid fa-bars"></i></div>
    <div class="menu">
        <!-- FIX: Aggiunti role, tabindex e onkeydown -->
        <div class="admin-item-menu item-logo" onclick="location.href='#'" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)"><img src="${pageContext.request.contextPath}/img/Logo_NoScritta.png" class="logo" alt="logo"></div>

        <div class="admin-item-menu" onclick="location.href='${pageContext.request.contextPath}/users-servlet'" data-name="HomePage" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)"><i class="fas fa-users iconMenu"></i>Utenti</div>

        <div class="admin-item-menu" onclick="location.href='${pageContext.request.contextPath}/show-order-user-servlet'" data-name="Ordini" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)"><i class="fa-solid fa-dollar-sign iconMenu"></i>Ordini</div>

        <div class="admin-item-menu" onclick="showSubMenu(this)" style="position: relative" data-name="Prodotti" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)"><i class="fa-solid fa-book-open iconMenu"></i>Prodotti
            <div class="sub-menu" data-name="Prodotti">
                <div class="item-submenu" onclick="location.href='${pageContext.request.contextPath}/add-product-servlet'" data-name="Prodotti" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)">Aggiungi</div>
                <div class="item-submenu" onclick="location.href='${pageContext.request.contextPath}/products-servlet'" data-name="Prodotti" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)">Gestione</div>
            </div>
        </div>

        <div class="admin-item-menu" onclick="<%= url %>" style="position: relative" data-name="Login" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)"><i class="fa-solid fa-user iconMenu"></i><%= name %>
            <div class="sub-menu" data-name="Login">
                <div class="item-submenu" onclick="location.href='${pageContext.request.contextPath}/user-profile-servlet'" data-name="Login" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)">Profilo</div>
                <div class="item-submenu" onclick="location.href='${pageContext.request.contextPath}/logout-servlet'" role="button" tabindex="0" onkeydown="handleKeyPress(event, this)">Logout</div>
            </div>
        </div>
    </div>
</div>

</body>
</html>