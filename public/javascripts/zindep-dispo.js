var zindepCallback = function(data) {
    var lesZindepsIcon = "http://www.leszindeps.fr/public/images/logo_leszindeps.png";
    var lesZindepsText = "Les Zindeps";
    var availableIcon = "http://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Button_Icon_Green.svg/300px-Button_Icon_Green.svg.png";
    var unavailableIcon = "http://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Button_Icon_Red.svg/300px-Button_Icon_Red.svg.png";

    var $zindepWidget = $("#zindepWidget");
    var zindepWidth = $zindepWidget.data("width");
    var $zindepDispo = data.currentAvailability;
    var isZindepDispo = ($zindepDispo != "NOT_AVAILABLE");
    var zindepDispoIcon = isZindepDispo ? availableIcon : unavailableIcon;
    var zindepDispoText = isZindepDispo ? "Disponible" : "Non disponible";

    var zindepPadding = zindepWidth / 50;
    var zindepHeight = zindepWidth * (9 / 40);
    var lesZindepsIconWidth = 100 * zindepWidth / 132; // reverse width dependence to zindepDispoIcon
    var zindepDispoIconWidth = lesZindepsIconWidth / 4;
    var zindepDispoIconMarginTop = zindepPadding;
    var spaceBetweenLogo = (lesZindepsIconWidth / 100) * 7;

    $zindepWidget.append("<div style='display:inline;'><img src='"+lesZindepsIcon+"' alt='"+lesZindepsText+"' title='"+lesZindepsText+"' width='"+lesZindepsIconWidth+"px' style='float:left;' /></div>")
        .append("<div style='display:inline;'><img src='"+zindepDispoIcon+"' alt='"+zindepDispoText+"' title='"+zindepDispoText+"' width='"+zindepDispoIconWidth+"px' style='float:left; margin-left:"+spaceBetweenLogo+"px; margin-top:"+zindepDispoIconMarginTop+"px;' /></div>")
        .css({"cursor": "pointer", "background-color": "white"})
        .css({"padding": zindepPadding+"px", "width": zindepWidth+"px", "height": zindepHeight+"px"})
        .click(function() { $(location).attr("href", $zindepWidget.data("profil")) });
}

$(document).ready(function() {
    $.jsonp({
        url: $("#zindepWidget").data("profil")+".jsonp",
        callbackParameter: "zindepCallback"
    });
});
