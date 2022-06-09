const myTimeout = setTimeout(clearDiv, 5000);

function clearDiv() {

    $(".success").fadeOut();
    $(".alert").fadeOut();
}