function customPopupDialog(message, title, windowHight, okButton, callback, windowWidth) {
    var strDialog = "<div id='dialog' title='" + title + "'><div id='popupDialog'></div>" + message + "</div>";
    var requiredWidth = 750;
    if (windowWidth) {
        requiredWidth = windowWidth;
    }
    var func = function() { 
    jQuery("#dcontainer").html(strDialog);
    if (okButton) {
        jQuery("#dialog").dialog({
            close:function() {
                jQuery(this).dialog('destroy').remove();
                jQuery("#dcontainer").empty();
                return false;
            },
            buttons:{
                "OK":function() {
                    if (callback && typeof callback == "function")
                        callback();
                    jQuery(this).dialog("destroy").remove();
                    jQuery("#dcontainer").empty();
                    return false;
                }
            },
            height:windowHight,
            width:requiredWidth,
            minHeight:windowHight,
            minWidth:requiredWidth,
            modal:true
        });
    } else {
        jQuery("#dialog").dialog({
            close:function() {
                jQuery(this).dialog('destroy').remove();
                jQuery("#dcontainer").empty();
                return false;
            },
            height:windowHight,
            width:requiredWidth,
            minHeight:windowHight,
            minWidth:requiredWidth,
            modal:true
        });
    }
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }
};
