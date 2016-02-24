function showNewRuleBox(link) {
    link.style.display = "none";
    var rowToHide = document.getElementById(link.id + "Row");
    if (rowToHide.style.display == "none") {
        rowToHide.style.display = "";
    } else {
        rowToHide.style.display = "none";
    }
}
function showHideRow(link) {
    var rowToHide = document.getElementById(link.id + "Row");
    if (rowToHide.style.display == "none") {
        rowToHide.style.display = "";
        link.className = "icon-link arrowUp";
    } else {
        rowToHide.style.display = "none";
        link.className = "icon-link arrowDown";
    }
}
function handleFocus(obj, txt) {
    if (obj.value == txt) {
        obj.value = '';
        YAHOO.util.Dom.removeClass(obj, 'defaultText');

    }
}
function handleBlur(obj, txt) {
    if (obj.value == '') {
        obj.value = txt;
        YAHOO.util.Dom.addClass(obj, 'defaultText');
    }
}
YAHOO.util.Event.onDOMReady(
        function() {
            /*if (document.getElementById("resourceNamesTarget").value == "") {
                document.getElementById("resourceNamesTarget").value = "Pick resource name";
            }
            if (document.getElementById("subjectNamesTarget").value == "") {
                document.getElementById("subjectNamesTarget").value = "Pick role name";
            }
            if (document.getElementById("userAttributeValueTarget").value == "") {
                document.getElementById("userAttributeValueTarget").value = "User attribute";
            }
            if (document.getElementById("actionNamesTarget").value == "") {
                document.getElementById("actionNamesTarget").value = "Action";
            }*/
        }
        );