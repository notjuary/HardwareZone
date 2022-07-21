function addProductCard(ID, quantity) {

    $.ajax({
        type: "GET",
        url: "add-to-cart-servlet",
        data: {"productId": ID, "quantity": quantity},
        success: function() {

            let newQuantity = parseInt($("#count").text()) + parseInt(quantity);
            $("#count").text(newQuantity);
        },

        error: function () {

            window.location.reload();
        }
    });
}