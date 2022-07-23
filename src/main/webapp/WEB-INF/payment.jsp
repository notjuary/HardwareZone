<%--@elvariable id="total" type="String"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it-IT">
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <title>Pagamento</title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/general.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/payment.css">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/eventManager.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/script/validateForm.js"></script>

</head>
<body>

    <%@ include file="/menu.jsp"%>

    <form action="${pageContext.request.contextPath}/payment-servlet" method="post" id="pay-form">
        <div class="paymentCard">
            <div class="creditCardNumber">
                <label for="numberCard">Numero Carta</label>
                <input type="text" name="numero-carta" id="numberCard" placeholder="Numero Carta">
            </div>

            <div class="cvv">
                <label for="CVV">CVV</label>
                <input type="text" name="CVV" id="CVV" placeholder="CVV">
            </div>

            <div class="deadline">
                <label for="deadline">Scadenza</label>
                <input type="date" name="scadenza" id="deadline">
            </div>

            <div class="holder">
                <label for="holder">Titolare</label>
                <textarea name="titolare" id="holder"></textarea>
            </div>

            <div class="total">
                <p style="text-align: center; color: #EEE5E9">Totale: ${total}</p>
            </div>

            <div class="paymentButton">
                <input type="button" value="Conferma Pagamento" onclick="validatePayment()">
            </div>
        </div>
    </form>

</body>
</html>
