<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Chi siamo</title>

    <link rel="stylesheet" type="text/css" href="style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/chiSiamo.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="script/eventManager.js"></script>
    <script type="text/javascript" src="script/product.js"></script>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <div class="container-page-chiSiamo">
        <div class="students">
            <div class="single-student">
                <div class="imageStudent">
                    <img src="img/imgStudent.png" alt="">
                </div>
                <div class="name">Michele</div>
            </div>

            <div class="single-student">
                <div class="imageStudent">
                    <img src="img/imgStudent.png" alt="">
                </div>
                <div class="name">Teodoro</div>
            </div>

            <div class="single-student">
                <div class="imageStudent">
                    <img src="img/imgStudent.png" alt="">
                </div>
                <div class="name">Junhuang</div>
            </div>
        </div>
    </div>

    <div class="doveSiamo"><h3>Ci troviamo qui :</h3></div>

    <div class="google-maps">
        <iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3021.5281416761145!2d14.788886415017927!3d40.772401841855775!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x133bc5af135b7ab1%3A0x1ee87d918d403827!2sVia%20Giovanni%20Paolo%20II%2C%20132%2C%2084084%20Fisciano%20SA!5e0!3m2!1sit!2sit!4v1658702983814!5m2!1sit!2sit" width="400" height="250" style="border:0;" allowfullscreen="" loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe>
    </div>

    <%@ include file="/footer.jsp"%>

</body>
</html>