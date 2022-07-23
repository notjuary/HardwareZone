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

        let slideIndex = 0;
        function showSlides() {
            let i;
            let slides = document.getElementsByClassName("mySlides");
            let dots = document.getElementsByClassName("dot");
            for (i = 0; i < slides.length; i++) {
                slides[i].style.display = "none";
            }
            slideIndex++;
            if (slideIndex > slides.length) {slideIndex = 1}
            for (i = 0; i < dots.length; i++) {
                dots[i].className = dots[i].className.replace(" active", "");
            }
            slides[slideIndex-1].style.display = "block";
            dots[slideIndex-1].className += " active";
            setTimeout(showSlides, 5000);
        }

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

                        $(".product-show").append(
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
<body onload="showSlides()">

    <%@ include file="/menu.jsp"%>

    <div class="slideshow-container">
        <div class="mySlides fade">
            <img src="https://www.maxit.my/wp-content/uploads/2016/05/msivrg1.jpg">
        </div>

        <div class="mySlides fade">
            <img src="https://www.evetech.co.za/repository/ProductImages/CoolerMaster-Slider-Banner-980x380px-v1.jpg">
        </div>

        <div class="mySlides fade">
            <img src="https://notebookspec.com/web/wp-content/uploads/2021/01/ROG-2021-Family-website-banner_1920-x-740_Fn.jpg">
        </div>
    </div>

    <div style="text-align:center">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
    </div>

    <div class="product-show">
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>