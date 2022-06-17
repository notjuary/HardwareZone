$("div.success div.alert").ready(function() {
    setTimeout(clearDiv, 5000);
});

function clearDiv() {
    $("div.success").fadeOut();
    $("div.alert").fadeOut();
    $("div.warning").fadeOut();
}