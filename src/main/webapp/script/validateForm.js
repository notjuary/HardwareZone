const info_string = /^([a-zA-Z\xE0\xE8\xE9\xF9\xF2\xEC\x27]\s?){2,30}$/ /* caratteri, lettere accentate apostrofo e un solo spazio fra le parole*/
const email_string = /^[a-zA-Z\d._%-]+@[a-zA-Z\d.-]+\.[a-zA-Z]{2,30}$/; /*	caratteri e . _ % – + @ + caratteri compreso . + . + min 2, max 4 caratteri*/
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

    let nome = document.getElementById("nome").value;
    let cognome = document.getElementById("cognome").value;
    let data_nascita = document.getElementById("data-di-nascita").value;

    if (info_string.test(nome) && info_string.test(cognome) && data_nascita !== "") {
        $("input").css("border-color", "#E5E5E5");
        showTabLoginInformation();
    }

    else {
        if (!(info_string.test(nome)))
            $("#nome").css("border-color", "#C92403");
        else
            $("#nome").css("border-color", "#E5E5E5");

        if (!(info_string.test(cognome)))
            $("#cognome").css("border-color", "#C92403");
        else
            $("#cognome").css("border-color", "#E5E5E5");

        if (data_nascita === "")
            $("#data-di-nascita").css("border-color", "#C92403");
        else
            $("#data-di-nascita").css("border-color", "#E5E5E5");
    }
}

function validateLoginInformation() {

    let email = document.getElementById("email").value;
    let password = document.getElementById("password").value;
    let telefono = document.getElementById("telefono").value;

    if (email_string.test(email) && password_string.test(password) && phone_string.test(telefono)) {
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

        if (!(phone_string.test(telefono)))
            $("#telefono").css("border-color", "#C92403");
        else
            $("#telefono").css("border-color", "#E5E5E5");
    }
}

function validateAddressInformation() {

    let citta = document.getElementById("citta").value;
    let provincia = document.getElementById("provincia").value;
    let codice_postale = document.getElementById("codice-postale").value;
    let indirizzo = document.getElementById("indirizzo").value;

    if (info_string.test(citta) && provincia_string.test(provincia) && cap_string.test(codice_postale) && address_string.test(indirizzo)) {
        $("input").css("border-color", "#E5E5E5");
        showTabReviewInformation();
    }
    else {
        if (!(info_string.test(citta)))
            $("#citta").css("border-color", "#C92403");
        else
            $("#citta").css("border-color", "#E5E5E5");

        if (!(info_string.test(provincia)))
            $("#provincia").css("border-color", "#C92403");
        else
            $("#provincia").css("border-color", "#E5E5E5");

        if (!(cap_string.test(codice_postale)))
            $("#codice-postale").css("border-color", "#C92403");
        else
            $("#codice-postale").css("border-color", "#E5E5E5");

        if (!(address_string.test(indirizzo)))
            $("#indirizzo").css("border-color", "#C92403");
        else
            $("#indirizzo").css("border-color", "#E5E5E5");
    }
}