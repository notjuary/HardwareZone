window.addEventListener("resize", browserSize);

function browserSize() {

    if (window.outerWidth > 1000)
        $("ul.navigation-bar").css({"visibility": "visible", "display": "flex"});

    else {
        $("ul.navigation-bar").css({"visibility": "hidden", "display": "none"});

    }
}

function showMenu() {
    let nav_bar = $("ul.navigation-bar").css("visibility");

    if (nav_bar === "hidden") {
        $("ul.navigation-bar").css({"visibility": "visible", "display": "block"});
    } else
        $("ul.navigation-bar").css({"visibility": "hidden", "display": "none"});
}

function showSearch() {
    let search_menu = $("div.search-tab").css("visibility");

    if (search_menu === "hidden" && window.outerWidth > 1000) {
        $("div.search-tab").css({"visibility": "visible", "display": "inline-block"});
    } else if (search_menu === "hidden" && window.outerWidth < 1000) {
        $("div.search-tab").css({"visibility": "visible", "display": "inline-block"});
        $("ul.navigation-bar").css({"visibility": "hidden", "display": "none"});
    } else {
        $("div.search-tab").css({"visibility": "hidden", "display": "none"});
    }
}

