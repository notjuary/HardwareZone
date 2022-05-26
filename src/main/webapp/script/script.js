const info_string = /^([a-zA-Z\xE0\xE8\xE9\xF9\xF2\xEC\x27]\s?)+$/; /*caratteri, lettere accentate apostrofo e un solo spazio fra le parole*/
const email_string = /^[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/; /*	caratteri e . _ % – + @ + caratteri compreso . + . + min 2, max 4 caratteri*/
const password_string = /^[a-zA-Z0-9\_\*\-\+\!\?\,\:\;\.\xE0\xE8\xE9\xF9\xF2\xEC\x27]{6,12}/; /*min 6, max 12 di caratteri, numeri, _ * – + ! ? , : ; . e lettere accentate*/
const phone_string = /^\d{10}$/ /*10 numeri*/
const provincia_string = /^([a-zA-Z]{2})$/;
const cap_string = /^\d{5}$/; /*5 numeri*/

function showMenu() {

}

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
        showTabLoginInformation();
    }
    else {
        if (!(info_string.test(nome)))
            alert("Nome non valido");
        if (!(info_string.test(cognome)))
            alert("Cognome non valido");
        if (data_nascita === "")
            alert("Data non valida");
    }
}

function validateLoginInformation() {

    let email = document.getElementById("email").value;
    let password = document.getElementById("password").value;
    let telefono = document.getElementById("telefono").value;

    if (email_string.test(email) && password_string.test(password) && phone_string.test(telefono)) {
        showTabAddressInformation();
    }
    else {
        if (!(email_string.test(email)))
            alert("Email non valida");
        if (!(password_string.test(password)))
            alert("Password non valida");
        if (!(phone_string.test(telefono)))
            alert("Telefono non valido");
    }
}

function validateAddressInformation() {

    let citta = document.getElementById("citta").value;
    let provincia = document.getElementById("provincia").value;
    let codice_postale = document.getElementById("codice-postale").value;
    let indirizzo = document.getElementById("indirizzo").value;

    if (info_string.test(citta) && provincia_string.test(provincia) && cap_string.test(codice_postale) && info_string.test(indirizzo)) {
        showTabReviewInformation();
    }
    else {
        if (!(info_string.test(citta)))
            alert("Città non valida");
        if (!(info_string.test(provincia)))
            alert("Provincia non valida");
        if (!(cap_string.test(codice_postale)))
            alert("CAP non valido");
        if (!(info_string.test(indirizzo)))
            alert("Indirizzo non valido");

        return false;
    }
}