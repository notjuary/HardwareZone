const info_string = /^([a-zA-Z\xE0\xE8\xE9\xF9\xF2\xEC\x27]\s?)+$/; /*caratteri, lettere accentate apostrofo e un solo spazio fra le parole*/
const email_string = /^[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/; /*	caratteri e . _ % – + @ + caratteri compreso . + . + min 2, max 4 caratteri*/
const password_String = /^[a-zA-Z0-9\_\*\-\+\!\?\,\:\;\.\xE0\xE8\xE9\xF9\xF2\xEC\x27]{6,12}/; /*min 6, max 12 di caratteri, numeri, _ * – + ! ? , : ; . e lettere accentate*/
const cap_string = /^\d{5}$/; /*5 numeri*/

function showTabPersonalInformation() {
    $(".tab-personal-information").show();
    $(".tab-login-information").hide();
    $(".tab-address-information").hide();
}

function showTabLoginInformation() {
    $(".tab-personal-information").hide();
    $(".tab-login-information").show();
    $(".tab-address-information").hide();
}

function showTabAddressInformation() {
    $(".tab-personal-information").hide();
    $(".tab-login-information").hide();
    $(".tab-address-information").show();
}

function validatePersonalInformation() {

    let nome = document.getElementById("nome").value;
    let cognome = document.getElementById("cognome").value;
    let data_nascita = document.getElementById("data-di-nascita").value;

    if (info_string.test(nome) && info_string.test(cognome) && data_nascita !== "") {
        showTabLoginInformation();
    }
    else {
        alert("Non corretto");
    }
}
function validateLoginInformation() {

    let email = document.getElementById("email").value;
    let password = document.getElementById("password").value;

    if (email_string.test(email) && password_String.test(password)) {
        showTabAddressInformation();
    }
    else {
        alert("Non corretto");
    }
}
