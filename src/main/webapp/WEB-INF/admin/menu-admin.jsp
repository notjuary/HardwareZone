<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Model.UserBean" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <title>Admin Menu</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/nav-menu.css">
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>

    <script>
        <%
            UserBean myProfile = (UserBean) session.getAttribute("user");

            String name, url;

            if (myProfile == null) {
                name = "Accesso";
                url = "login.jsp";
            } else {
                name = myProfile.getName();
                url = "#"; // Il click è gestito da JS
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
    </script>

    <style>
        /* FIX per far sembrare i bottoni e i link dei normali div */
        button.show-menu, button.admin-item-menu, a.admin-item-menu, a.item-submenu {
            background: none;
            border: none;
            padding: 0;
            margin: 0;
            font: inherit;
            color: inherit;
            cursor: pointer;
            text-align: inherit;
            text-decoration: none;
            display: block; /* o flex a seconda del CSS esistente */
        }
    </style>

</head>
<body>

<div class="navigation-bar">
    <!-- FIX: Usato <button> nativo per un'azione JS -->
    <button type="button" class="show-menu" onclick="showMenu()"><i class="fa-solid fa-bars"></i></button>

    <div class="menu">
        <!-- FIX: Usato <a href> nativo per la navigazione -->
        <a href="${pageContext.request.contextPath}/users-servlet" class="admin-item-menu item-logo"><img src="${pageContext.request.contextPath}/img/Logo_NoScritta.png" class="logo" alt="logo"></a>

        <a href="${pageContext.request.contextPath}/users-servlet" class="admin-item-menu" data-name="HomePage"><i class="fas fa-users iconMenu"></i>Utenti</a>

        <a href="${pageContext.request.contextPath}/show-order-user-servlet" class="admin-item-menu" data-name="Ordini"><i class="fa-solid fa-dollar-sign iconMenu"></i>Ordini</a>

        <!-- FIX: Usato <button> per aprire il sottomenu -->
        <button type="button" class="admin-item-menu" onclick="showSubMenu(this)" style="position: relative" data-name="Prodotti"><i class="fa-solid fa-book-open iconMenu"></i>Prodotti
            <div class="sub-menu" data-name="Prodotti">
                <a href="${pageContext.request.contextPath}/add-product-servlet" class="item-submenu" data-name="Prodotti">Aggiungi</a>
                <a href="${pageContext.request.contextPath}/products-servlet" class="item-submenu" data-name="Prodotti">Gestione</a>
            </div>
        </button>

        <% if (myProfile == null) { %>
        <a href="${pageContext.request.contextPath}/login.jsp" class="admin-item-menu" data-name="Login"><i class="fa-solid fa-user iconMenu"></i><%= name %></a>
        <% } else { %>
        <button type="button" class="admin-item-menu" onclick="showSubMenu(this)" style="position: relative" data-name="Login"><i class="fa-solid fa-user iconMenu"></i><%= name %>
            <div class="sub-menu" data-name="Login">
                <a href="${pageContext.request.contextPath}/user-profile-servlet" class="item-submenu" data-name="Login">Profilo</a>
                <a href="${pageContext.request.contextPath}/logout-servlet" class="item-submenu">Logout</a>
            </div>
        </button>
        <% } %>
    </div>
</div>

</body>
</html>