<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Carrello</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/cart.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>

    <script>

        function loadProductAjax() {

            $.ajax({
                type: "GET",
                url: "${pageContext.request.contextPath}/show-cart-servlet",
                success: function(data) {

                    let products = JSON.parse(data);

                    if (products.length > 0) {
                        let total =0;

                        $(".section-cart-item *").remove();
                        for (let i = 0; i < products.length; i++) {

                            let salesDiv = "";
                            if (products[i]["sales"] === 0) {
                                salesDiv = '<div class="price">€' + products[i]["price"].toFixed(2) + '</div>'
                                total = total + (products[i]["price"].toFixed(2) * products[i]["quantity"]);
                            }
                            else {
                                let sale = products[i]["price"] - ((products[i]["price"] * products[i]["sales"] / 100));
                                total = total + (sale * products[i]["quantity"]);
                                salesDiv = '<div class="price onSale"><span style="color: black; text-decoration: line-through;">€' + products[i]["price"].toFixed(2) + '</span> €' + sale.toFixed(2) + ' -' + products[i]["sales"] + '%</div>'
                            }

                            $(".section-cart-item").append(
                                '<div class="productCard">' +
                                '<div class="imageCart"><img alt="image" src="' + products[i]["image"] + '"></div>' +
                                '<div class="infoProductCart">' +
                                '<div class="nameCart">' + products[i]["name"] + '</div>' +
                                '<div class="quantityCart">Quantità: ' + products[i]["quantity"] + '</div>'
                                + salesDiv +
                                '</div>' +
                                '<div class="remove"><a href="remove-from-cart-servlet?productId=' + products[i]["id"] + '"><i class="fa-solid fa-trash"></i></a></div>' +
                                '</div>'
                            )
                        }

                        $(".section-cart-item").append(
                            '<p style="text-align: center"> Totale: €' + total.toFixed(2) + '</p>' +
                            '<div class="buttonCart"><a class="button" href="payment-servlet">Procedi</a></div>'
                        )
                    }
                }
            });
        }
    </script>

</head>
<body onload="loadProductAjax()">

    <%@ include file="/menu.jsp"%>

    <div class="section-cart-item">
        <p class="noProduct">Non ci sono prodotti da visualizzare</p>
        <p class="noProduct">Vai al <a href="show-catalog-servlet" style="color: #c92403">catalogo</a></p>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>