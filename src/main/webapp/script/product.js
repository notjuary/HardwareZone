function addProductCard(ID, quantity) {

    $.ajax({
        type: "GET",
        url: "add-to-cart-servlet",
        data: {"productId": ID, "quantity": quantity},
        success: function() {
            // FIX: Number.parseInt invece di parseInt
            let newQuantity = Number.parseInt($("#count").text()) + Number.parseInt(quantity);
            $("#count").text(newQuantity);
        },

        error: function () {
            window.location.reload();
        }
    });
}