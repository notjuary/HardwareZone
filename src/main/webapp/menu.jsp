<%@ page import="Model.UserBean" %>
<%@ page import="Model.CartBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <title>Menu</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/nav-menu.css">
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>
    <!-- Per jQuery 3.6.0 -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"
            integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4="
            crossorigin="anonymous"></script>
    <script type="text/javascript" src="script/product.js"></script>

    <script>
        <%
            UserBean myProfile = (UserBean) request.getSession().getAttribute("user");

            String name;

            if (myProfile == null) {
                name = "Accesso";
            } else {
                name = myProfile.getName();
            }

            int numberObject;
            if ((request.getSession().getAttribute("cart")) != null)
                numberObject = ((CartBean) request.getSession().getAttribute("cart")).getNumberObject();
            else
                numberObject = 0;
        %>

        window.addEventListener("resize", browserSize);
        function browserSize() {
            if (window.outerWidth <= 1160) {
                $("div.menu").hide();
                $("div.sub-menu").hide();
            }
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
            $(".item-menu").each(function() {
                if (currentPage === (this.getAttribute("data-name"))) {
                    $(this).closest("div").addClass("active");
                }
            });
        }
    </script>

    <style>
        /* FIX per far sembrare i bottoni e i link dei normali div */
        button.show-menu, button.item-menu, a.item-menu, a.item-submenu {
            background: none;
            border: none;
            padding: 0;
            margin: 0;
            font: inherit;
            color: inherit;
            cursor: pointer;
            text-align: inherit;
            text-decoration: none;
            display: block;
        }
    </style>

</head>
<body>

<div class="navigation-bar">
    <!-- FIX: Usato <button> nativo -->
    <button type="button" class="show-menu" onclick="showMenu()"><i class="fa-solid fa-bars"></i></button>

    <div class="menu">
        <a href="${pageContext.request.contextPath}/index.jsp" class="item-menu item-logo"><img src="img/Logo_NoScritta.png" class="logo" alt="logo"></a>
        <a href="${pageContext.request.contextPath}/index.jsp" class="item-menu" data-name="HomePage"><i class="fa-solid fa-house iconMenu"></i>HomePage</a>
        <a href="${pageContext.request.contextPath}/show-catalog-servlet" class="item-menu" data-name="Catalogo"><i class="fa-solid fa-book-open iconMenu"></i>Catalogo</a>
        <a href="${pageContext.request.contextPath}/show-sales-servlet" class="item-menu" data-name="Offerte"><i class="fa-solid fa-dollar-sign iconMenu"></i>Offerte</a>
        <a href="${pageContext.request.contextPath}/Cart.jsp" class="item-menu" data-name="Carrello"><i class="fa-solid fa-cart-shopping iconMenu"></i>Carrello <span id="count"><%= numberObject %></span></a>
        <a href="${pageContext.request.contextPath}/chiSiamo.jsp" class="item-menu" data-name="Chi siamo"><i class="fa-solid fa-circle-info iconMenu"></i>Chi siamo</a>

        <% if (myProfile == null) { %>
        <a href="${pageContext.request.contextPath}/login.jsp" class="item-menu" data-name="Login"><i class="fa-solid fa-user iconMenu"></i><%= name %></a>
        <% } else { %>
        <button type="button" class="item-menu" onclick="showSubMenu(this)" style="position: relative" data-name="Login"><i class="fa-solid fa-user iconMenu"></i><%= name %>
            <div class="sub-menu" data-name="Login">
                <a href="${pageContext.request.contextPath}/user-profile-servlet" class="item-submenu">Profilo</a>
                <a href="${pageContext.request.contextPath}/show-order-user-servlet" class="item-submenu">Ordini</a>
                <a href="${pageContext.request.contextPath}/logout-servlet" class="item-submenu">Logout</a>
            </div>
        </button>
        <% } %>
    </div>
</div>

</body>
</html>