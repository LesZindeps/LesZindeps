var zindepCallback = function(data) {
    var lesZindepsIcon = "http://www.leszindeps.fr/public/images/logo_leszindeps.png";
    var lesZindepsText = "Les Zindeps";
    var availableIcon = "http://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Button_Icon_Green.svg/300px-Button_Icon_Green.svg.png";
    var unavailableIcon = "http://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Button_Icon_Red.svg/300px-Button_Icon_Red.svg.png";

    var $zindepWidget = $("#zindepWidget");
    var width = $zindepWidget.data("width");
    var $zindepDispo = data.currentAvailability;
    var isZindepDispo = ($zindepDispo != "NOT_AVAILABLE");
    var zindepDispoIcon = isZindepDispo ? availableIcon : unavailableIcon;
    var zindepDispoText = isZindepDispo ? "Disponible" : "Non disponible";

    var lesZindepsIconWidth = 100 * width / 132; // reverse width dependence to zindepDispoIcon
    var zindepDispoIconWidth = lesZindepsIconWidth / 4;
    var spaceBetweenLogo = (lesZindepsIconWidth / 100) * 7;

    $zindepWidget.append("<div style='display:inline;'><img src='"+lesZindepsIcon+"' alt='"+lesZindepsText+"' title='"+lesZindepsText+"' width='"+lesZindepsIconWidth+"px' style='float:left;' /></div>")
        .append("<div style='display:inline;'><img src='"+zindepDispoIcon+"' alt='"+zindepDispoText+"' title='"+zindepDispoText+"' width='"+zindepDispoIconWidth+"px' style='float:left;margin-left:"+spaceBetweenLogo+"px;' /></div>")
        .css("cursor", "pointer")
        .click(function() { $(location).attr("href", $zindepWidget.data("profil")) });
}

$(document).ready(function() {
    $.jsonp({
        url: $("#zindepWidget").data("profil")+".jsonp",
        callbackParameter: "zindepCallback"
    });
});
