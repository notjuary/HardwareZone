<%@ page import="Model.ProductBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.CategoryDAO" %>
<%@ page import="Model.CategoryBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Catalogo</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/catalog.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script src='https://kit.fontawesome.com/c6b30e1924.js' crossorigin='anonymous'></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>

    <script>
        $(".next").click(function(){
            $.ajax({
                url: "show-catalog-servlet?first=1&last=18",
                type: 'GET',

                success: function() {
                    alert("Va")
                    $("#containerProduct").load("#containerProduct");
                }
            });
        });
    </script>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <% ArrayList<ProductBean> productsList = (ArrayList<ProductBean>) request.getAttribute("products"); %>
    <%
        CategoryDAO service = new CategoryDAO();
        ArrayList<CategoryBean> categories = service.doRetrieveAll();
    %>

    <div class="bodyCatalog">
        <div class="filterBar">
            <div class="filterPrice">
                <span>Prezzo</span>
            </div>
            <div class="filterCategory">
                <span>Categoria</span>
                <% for(CategoryBean category: categories) { %>
                <div class="categoryName">
                    <input type="checkbox" id="<%= category.getNome() %>">
                    <label for="<%= category.getNome() %>"><%= category.getNome() %></label>
                </div>
                <% } %>
            </div>
            <div class="filterSearch">
                <input type="button" value="Filtra" onclick="">
            </div>
        </div>

        <div class="containerProduct">
            <% for (ProductBean product: productsList) { %>
                <div class="productCard">
                    <div class="image"><img src="<%= product.getImage() %>" alt="<%= product.getName() %>"></div>
                    <div class="name"><%= product.getName() %></div>
                    <div class="price">€<%= product.getPrice() %></div>
                    <div><i class="fa-solid fa-cart-plus"></i></div>
                </div>
            <% } %>

            <div class="arrow-container">
                <div class="previous"><i class="fa-solid fa-circle-arrow-left"></i></div>
                <div class="next"><i class="fa-solid fa-circle-arrow-right"></i></div>
            </div>
        </div>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
