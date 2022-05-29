<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/nav-menu.css">
<script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>

<%
    String utente = (String) session.getAttribute("nome-utente");
    String url;

    if (utente == null) {
        utente = "Accesso";
        url = "login.jsp";
    }
    else {
        url = "profilo-servlet";
    }
%>

<div class="navigation-bar">
    <div class="item-menu"><a href="index.jsp">HomePage</a></div>
    <div class="item-menu"><a href="#">Catalogo</a></div>
    <div class="item-menu"><a href="#">Offerte</a></div>
    <div class="item-menu"><a href="login.jsp">Accesso</a></div>
        <div class="sub-menu">
            <div class="item-submenu"></div>
            <div class="item-submenu"></div>
            <div class="item-submenu"></div>
        </div>
    <div class="item-menu"><a href="#">Carrello</a></div>
    <div class="item-menu"><a href="#">Chi siamo</a></div>
</div>

<!--
<div class="navigation-bar">
    <span class="navigation-bar" onclick="showMenu()"><i class="fa-solid fa-bars"></i></span>
    <ul class="navigation-bar">
        <li class="navigation-bar"><a href="index.jsp" class="navigation-bar"><i class="fa-solid fa-house"></i><br/>HomePage</a></li>
        <li class="navigation-bar"><a href="#" class="navigation-bar"><i class="fa-solid fa-book-open"></i><br/>Catalogo</a></li>
        <li class="navigation-bar"><a href="#" class="navigation-bar"><i class="fa-solid fa-dollar-sign"></i><br/>Offerte</a></li>
        <li class="navigation-bar"><a onclick="showSearch()" class="navigation-bar"><i class="fa-solid fa-magnifying-glass"></i><br/>Ricerca</a></li>
        <li class="navigation-bar"><a href="#" class="navigation-bar"><i class="fa-solid fa-circle-info"></i><br/>Chi siamo</a></li>
        <li class="navigation-bar"><a onclick="showLoginMenu()" class="navigation-bar"><i class="fa-solid fa-user"></i><br/><%= utente%></a></li>
            <div class="sub-nav">
                <ul>
                    <li></li>
                </ul>
            </div>
        <li class="navigation-bar"><a href="#" class="navigation-bar"><i class="fa-solid fa-cart-shopping"></i><br/>Carrello</a></li>
    </ul>

    <div class="search-tab">
        <form action="#" class="search-tab">
            <input type="text" class="search-tab" id="ricerca" name="ricerca" placeholder="Ricerca">
            <button type="submit" class="search-tag"><i class="fa-solid fa-magnifying-glass"></i></button>
            <button type="button" class="search-tag" onclick="showSearch()"><i class="fa-solid fa-xmark"></i></button>
        </form>
    </div>

</div>
-->