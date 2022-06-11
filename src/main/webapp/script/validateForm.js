const info_string = /^([a-zA-Z\xE0\xE8\xE9\xF9\xF2\xEC\x27]\s?){2,20}$/ /* caratteri, lettere accentate apostrofo e un solo spazio fra le parole*/
const email_string = /^[a-zA-Z\d._%-]+@[a-zA-Z\d.-]+\.[a-zA-Z]{2,20}$/; /*	caratteri e . _ % – + @ + caratteri compreso . + . + min 2, max 4 caratteri*/
const password_string = /^[a-zA-Z\d\-\xE0\xE8\xE9\xF9\xF2\xEC\x27]{6,16}/; /* minimo sei, max 12 di caratteri, numeri, _ * – + ! ? , : ; . e lettere accentate*/
const phone_string = /^\d{10}$/ /*10 numeri*/
const provincia_string = /^([a-zA-Z]{2})$/;
const cap_string = /^\d{5}$/; /*Cinque numeri*/
const address_string = /^([a-zA-Z\d\xE0\xE8\xE9\xF9\xF2\xEC\x27\x2C]\s?)+$/;

function showTabPersonalInformation() {
    $(".tab-personal-information").show();
    $(".tab-login-information").hide();
    $(".tab-address-information").hide();
    $(".tab-review-information").hide();
}

function showTabLoginInformation() {
    $(".tab-personal-information").hide();
    $(".tab-login-information").show();
    $(".tab-address-information").hide();
    $(".tab-review-information").hide();
}

function showTabAddressInformation() {
    $(".tab-personal-information").hide();
    $(".tab-login-information").hide();
    $(".tab-address-information").show();
    $(".tab-review-information").hide();
}

function showTabReviewInformation() {
    $(".tab-personal-information").hide();
    $(".tab-login-information").hide();
    $(".tab-address-information").hide();
    $(".tab-review-information").show();
}

function validatePersonalInformation() {

    let name = document.getElementById("name").value;
    let surname = document.getElementById("surname").value;
    let birthday = document.getElementById("birthday").value;

    if (info_string.test(name) && info_string.test(surname) && birthday !== "") {
        $("input").css("border-color", "#E5E5E5");
        showTabLoginInformation();
    }

    else {
        if (!(info_string.test(name)))
            $("#name").css("border-color", "#C92403");
        else
            $("#name").css("border-color", "#E5E5E5");

        if (!(info_string.test(surname)))
            $("#surname").css("border-color", "#C92403");
        else
            $("#surname").css("border-color", "#E5E5E5");

        if (birthday === "")
            $("#data-di-nascita").css("border-color", "#C92403");
        else
            $("#data-di-nascita").css("border-color", "#E5E5E5");
    }
}

function validateLoginInformation() {

    let email = document.getElementById("email").value;
    let password = document.getElementById("password").value;
    let phone = document.getElementById("phone").value;

    if (email_string.test(email) && password_string.test(password) && phone_string.test(phone)) {
        $("input").css("border-color", "#E5E5E5");
        showTabAddressInformation();
    }
    else {
        if (!(email_string.test(email)))
            $("#email").css("border-color", "#C92403");
        else
            $("#email").css("border-color", "#E5E5E5");

        if (!(password_string.test(password)))
            $("#password").css("border-color", "#C92403");
        else
            $("#password").css("border-color", "#E5E5E5");

        if (!(phone_string.test(phone)))
            $("#phone").css("border-color", "#C92403");
        else
            $("#phone").css("border-color", "#E5E5E5");
    }
}

function validateAddressInformation() {

    let city = document.getElementById("city").value;
    let province = document.getElementById("province").value;
    let postalCode = document.getElementById("postalCode").value;
    let address = document.getElementById("address").value;

    if (info_string.test(city) && provincia_string.test(province) && cap_string.test(postalCode) && address_string.test(address)) {
        $("input").css("border-color", "#E5E5E5");
        showTabReviewInformation();
    }
    else {
        if (!(info_string.test(city)))
            $("#city").css("border-color", "#C92403");
        else
            $("#city").css("border-color", "#E5E5E5");

        if (!(info_string.test(province)))
            $("#province").css("border-color", "#C92403");
        else
            $("#province").css("border-color", "#E5E5E5");

        if (!(cap_string.test(postalCode)))
            $("#codice-postale").css("border-color", "#C92403");
        else
            $("#codice-postale").css("border-color", "#E5E5E5");

        if (!(address_string.test(address)))
            $("#address").css("border-color", "#C92403");
        else
            $("#address").css("border-color", "#E5E5E5");
    }
}