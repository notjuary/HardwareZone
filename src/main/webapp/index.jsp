<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>HomePage</title>

    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/homepage.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/eventManager.js"></script>
    <script type="text/javascript" src="script/product.js"></script>

    <script>
        $(document).ready(function() {
            $.ajax({
                type: "GET",
                url: "${pageContext.request.contextPath}/products-homepage-servlet",
                success: function(data) {

                    let products = JSON.parse(data);

                    for (let i = 0; i < products.length; i++) {

                        let icon = "";
                        if (products[i]["quantity"] > 0)
                            icon = '<a class="shop" onclick="addProductCard(' + products[i]["id"] + ', 1)"><i class="fa-solid fa-cart-plus"></i></a>'
                        else
                            icon = '<i class="fa-solid fa-ban"></i>'

                        let salesDiv = "";
                        if (products[i]["sales"] === 0)
                            salesDiv = '<div class="price">€' + products[i]["price"].toFixed(2) + '</div>'
                        else {
                            let sale = products[i]["price"] - ((products[i]["price"] * products[i]["sales"] / 100));
                            salesDiv = '<div class="price onSale"><span style="color: black; text-decoration: line-through;">€' + products[i]["price"].toFixed(2) + '</span> €' + sale.toFixed(2) + ' -' + products[i]["sales"] + '%</div>'
                        }


                        $(".slide-show").append(
                            '<a class="productCard" href="${pageContext.request.contextPath}/show-product-servlet?productId=' + products[i]["id"] + '">' +
                            '<div class="productCard">' +
                            '<div class="image"><img src="' + products[i]["image"] + '" alt="' + products[i]["name"] + '"></div>' +
                            '<div class="name">' + products[i]["name"] + '</div>' +
                            salesDiv +
                            '<div>' + icon + '</div>' +
                            '</div></a>'
                        );
                    }
                }
            });
        });
    </script>

</head>
<body>

    <%@ include file="menu.jsp"%>

        <div class="slide-show">
        </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>