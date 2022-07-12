<%@ page import="Model.ProductBean" %>
<%@ page import="java.util.ArrayList" %>
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
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/validateForm.js"></script>

    <%  @SuppressWarnings("unchecked")
        ArrayList<CategoryBean> listCategories = (ArrayList<CategoryBean>) request.getAttribute("categories"); %>

    <script>
        function filterAjax() {

            let form = $("#formFilter");

            $.ajax({
                type: "POST",
                url: "${pageContext.request.contextPath}/filter-product-servlet",
                data: form.serialize(),
                success: function(data) {

                    let products = JSON.parse(data);
                    $("#containerProduct *").remove();

                    for (let i = 0; i < products.length; i++) {


                        let icon = "";
                        if (products[i]["quantity"] > 0)
                            icon = " fa-cart-plus"
                        else
                            icon = " fa-ban"

                        let salesDiv = "";
                        if (products[i]["sales"] === 0)
                            salesDiv = '<div class="price">€' + products[i]["price"].toFixed(2) + '</div>'
                        else {
                            let sale = products[i]["price"] - ((products[i]["price"] * products[i]["sales"] / 100));
                            salesDiv = '<div class="price onSale"><span style="color: black; text-decoration: line-through;">€' + products[i]["price"].toFixed(2) + '</span> €' + sale.toFixed(2) + ' -' + products[i]["sales"] + '%</div>'
                        }


                        $("#containerProduct").append(
                            '<div class="productCard">' +
                            '<div class="image"><img src="' + products[i]["image"] + '" alt="' + products[i]["name"] + '"></div>' +
                            '<div class="name">' + products[i]["name"] + '</div>' +
                            salesDiv +
                            '<div><i class="fa-solid' +  icon + '"></i></div>' +
                            '</div>'
                        );
                    }
                }
            });
        }
    </script>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <div class="bodyCatalog">
        <form id="formFilter">
            <div class="filterBar">
                <div class="filterPrice">
                    <span>Prezzo</span>
                    <div class="range">
                        <label for="min">Prezzo minimo</label><br>
                        <input type="text" pattern="[0-9]+" id="min" name="min" value="1" placeholder="1">
                    </div>
                    <div class="range">
                        <label for="max">Prezzo massimo</label><br>
                        <input type="text" pattern="[0-9]+" id="max" name="max" value="10000" placeholder="10000">
                    </div>
                </div>

                <div class="filterCategory">
                    <span>Categoria</span>
                    <div class="categoryName">
                        <input type="radio" class="categoryName" name="category" id="all" value="all" checked>
                        <label for="all">Mostra tutto</label>
                    </div>
                    <% for(CategoryBean category: listCategories) { %>
                        <div class="categoryName">
                            <input type="radio" class="categoryName" name="category" id="<%= category.getNome() %>" value="<%= category.getNome() %>">
                            <label for="<%= category.getNome() %>"><%= category.getNome() %></label>
                        </div>
                    <% } %>
                </div>

                <div class="filterSearch">
                    <input type="button" onclick="validateFilterPrice()" value="Filtra">
                </div>
            </div>
        </form>

        <div class="containerProduct" id="containerProduct">
            <%  @SuppressWarnings("unchecked")
                ArrayList<ProductBean> productsList = (ArrayList<ProductBean>) request.getAttribute("products"); %>

            <% for (ProductBean product: productsList) { %>
                <div class="productCard">
                    <div class="image"><img src="<%= product.getImage() %>" alt="<%= product.getName() %>"></div>
                    <div class="name"><%= product.getName() %></div>

                    <% if (product.getSales() > 0) {
                        double sale = product.getPrice() - ((product.getPrice() * product.getSales() / 100)); %>
                        <div class="price onSale"><span style="color: black; text-decoration: line-through;">€<%= String.format("%.2f", product.getPrice()) %></span> €<%= String.format("%.2f", sale) %> -<%= product.getSales() %>%</div>
                    <% } else { %>
                        <div class="price">€<%= String.format("%.2f", product.getPrice()) %></div>
                    <% } %>

                    <% if (product.getQuantity() > 0) { %>
                        <div><i class="fa-solid fa-cart-plus"></i></div>
                    <% } else {%>
                        <div><i class="fa-solid fa-ban"></i></div>
                    <% } %>
                </div>
            <% } %>
        </div>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>
