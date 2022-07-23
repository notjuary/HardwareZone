const info_string = /^([a-zA-Z\xE0\xE8\xE9\xF9\xF2\xEC\x27]\s?){2,20}$/;
const email_string = /^[a-zA-Z\d._%-]+@[a-zA-Z\d.-]+\.[a-zA-Z]{2,20}$/;
const password_string = /^[a-zA-Z\d\-\xE0\xE8\xE9\xF9\xF2\xEC\x27]{6,16}/;
const phone_string = /^\d{10}$/;
const province_string = /^([a-zA-Z]{2})$/;
const postalcode_string = /^\d{5}$/;
const address_string = /^([a-zA-Z\d\xE0\xE8\xE9\xF9\xF2\xEC\x27\x2C]\s?){2,20}$/;
const number_string = /^\d+$/;
const double_string = /^(\d+(?:[.,]\d{2})?)$/;
const holder_string = /^([a-zA-Z\xE0\xE8\xE9\xF9\xF2\xEC\x27]\s?){2,255}$/;

$(document).ready(function() {
    showTabPersonalInformation();
});

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

function validateName() {

    let name = document.getElementById("name").value;
    if (info_string.test(name)) {
        $("#name").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#name").css("border-color", "#C92403");
        return false;
    }
}

function validateSurname() {

    let surname = document.getElementById("surname").value;
    if (info_string.test(surname)) {
        $("#surname").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#surname").css("border-color", "#C92403");
        return false;
    }
}

function validateBirthday() {

    let birthday = document.getElementById("birthday").value;
    if (birthday !== "") {
        $("#birthday").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#birthday").css("border-color", "#C92403");
        return false;
    }
}

function validateEmail() {

    let email = document.getElementById("email").value;
    if (email_string.test(email)) {
        $("#email").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#email").css("border-color", "#C92403");
        return false;
    }
}

function validatePassword() {

    let password = document.getElementById("password").value;
    if (password_string.test(password)) {
        $("#password").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#password").css("border-color", "#C92403");
        return false;
    }
}

function validatePhone() {

    let phone = document.getElementById("phone").value;
    if (phone_string.test(phone)) {
        $("#phone").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#phone").css("border-color", "#C92403");
        return false;
    }
}

function validateCity() {

    let city = document.getElementById("city").value;
    if (info_string.test(city)) {
        $("#city").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#city").css("border-color", "#C92403");
        return false;
    }
}

function validateProvince() {

    let province = document.getElementById("province").value;
    if (province_string.test(province)) {
        $("#province").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#province").css("border-color", "#C92403");
        return false;
    }
}

function validatePostalCode() {

    let postalCode = document.getElementById("postalCode").value;
    if (postalcode_string.test(postalCode)) {
        $("#postalCode").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#postalCode").css("border-color", "#C92403");
        return false;
    }
}

function validateAddress() {

    let address = document.getElementById("address").value;
    if (address_string.test(address)) {
        $("#address").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#address").css("border-color", "#C92403");
        return false;
    }
}

function validateNumberCart() {

    let numberCard = document.getElementById("numberCard").value;
    if (number_string.test(numberCard) && numberCard.length === 16) {
        $("#numberCard").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#numberCard").css("border-color", "#C92403");
        return false;
    }
}

function validateCVV() {

    let cvv = document.getElementById("CVV").value;
    if (number_string.test(cvv) && cvv.length === 3) {
        $("#CVV").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#CVV").css("border-color", "#C92403");
        return false;
    }
}

function validateDeadline() {

    let deadline = new Date(document.getElementById("deadline").value);
    let today = new Date();
    if (deadline > today) {
        $("#deadline").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#deadline").css("border-color", "#C92403");
        return false;
    }
}

function validateHolder() {

    let holder = document.getElementById("holder").value;
    if (holder_string.test(holder)) {
        $("#holder").css("border-color", "#E5E5E5");
        return true;
    }
    else {
        $("#holder").css("border-color", "#C92403");
        return false;
    }
}

function validatePersonalInformation() {

    if (validateName() & validateSurname() & validateBirthday())
        showTabLoginInformation();
}

function validateLoginInformation() {

    if (validateEmail() & validatePassword() & validatePhone())
        showTabAddressInformation();
}

function validateAddressInformation() {

    if (validateCity() & validateProvince() & validatePostalCode() & validateAddress())
        showTabReviewInformation();
}

function validateLogin() {

    if (validateEmail() & validatePassword())
        document.getElementById("loginForm").submit();
}

function validateFilterPrice() {
    let price1 = document.getElementById("min").value;
    let price2 = document.getElementById("max").value;

    if (number_string.test(price1) && number_string.test(price2)) {
        $("input").css("border-color", "#0f1a20");
        filterAjax();
    }

    else {
        if (!(number_string.test(price1)))
            $("#min").css("border-color", "#C92403");
        if (!(number_string.test(price2)))
            $("#max").css("border-color", "#C92403");
    }
}

function validateEditProfile() {

    if (validateName() & validateSurname() & validateBirthday() &
        validateEmail() & validatePassword() & validatePhone() &
        validateCity() & validateProvince() & validatePostalCode() & validateAddress()) {

        document.getElementById("edit-profile").submit();
    }
}

function validateAddProduct() {

    let price = document.getElementById("price").value;
    let quantity = document.getElementById("quantity").value;

    if (double_string.test(price) && number_string.test(quantity)) {

        document.getElementById("add-product-form").submit();
    }
}

function validatePayment() {

    if (validateNumberCart() & validateCVV() & validateDeadline() & validateHolder())
        document.getElementById("pay-form").submit();
}