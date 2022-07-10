<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>HomePage</title>

    <link rel="stylesheet" type="text/css" href="style/general.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/eventManager.js"></script>

    <script>
        $(document).ready(function() {
            $.ajax({
                type: "GET",
                url: "${pageContext.request.contextPath}/products-homepage-servlet",
                success: function(data) {

                    let products = JSON.parse(data);
                    for (let i = 0; i < products.length; i++) {
                        $(".slide-show").append(
                            '<div>' + products[i]["name"] + '</div>'
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