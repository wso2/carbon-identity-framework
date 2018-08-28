/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Temporary function that serves as the local popup customization till carbon-kernel release
 * This has also been moved to proper codebase in carbon.ui as this is reusable.
 */
function showPopupConfirm(htmlMessage, title, windowHeight, windowWidth, okButton, cancelButton, callback, closeCallback) {
    if (!isHTML(htmlMessage)) {
        htmlMessage = htmlEncode(htmlMessage);
    }
    var strDialog = "<div id='dialog' title='" + title + "'><div id='popupDialog'></div>" + htmlMessage + "</div>";
    var requiredWidth = 750;
    if (windowWidth) {
        requiredWidth = windowWidth;
    }
    var func = function () {
        jQuery("#dcontainer").html(strDialog);
        if (okButton) {
            jQuery("#dialog").dialog({
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    return false;
                },
                buttons: {
                    "Save": function () {
                        if (callback && typeof callback == "function")
                            var isCallbackerror = callback();
                        if (!isCallbackerror) {
                            jQuery(this).dialog("destroy").remove();
                            jQuery("#dcontainer").empty();
                            return false;
                        }
                    },
                    "Cancel": function () {
                        jQuery(this).dialog('destroy').remove();
                        jQuery("#dcontainer").empty();
                        if (closeCallback && typeof closeCallback == "function") {
                            closeCallback();
                        }
                        return false;
                    },
                },
                height: windowHeight,
                width: requiredWidth,
                minHeight: windowHeight,
                minWidth: requiredWidth,
                modal: true
            });
        } else {
            jQuery("#dialog").dialog({
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    if (closeCallback && typeof closeCallback == "function") {
                        closeCallback();
                    }
                    return false;
                },
                height: windowHeight,
                width: requiredWidth,
                minHeight: windowHeight,
                minWidth: requiredWidth,
                modal: true
            });
        }
        if (okButton) {
            $('.ui-dialog-buttonpane button:contains(OK)').attr("id", "dialog-confirm_ok-button");
            $('#dialog-confirm_ok-button').html(okButton);
        }
        if (cancelButton) {
            $('.ui-dialog-buttonpane button:contains(Cancel)').attr("id", "dialog-confirm_cancel-button");
            $('#dialog-confirm_cancel-button').html(cancelButton);
        }
        jQuery('.ui-dialog-titlebar-close').click(function () {
            jQuery('#dialog').dialog("destroy").remove();
            jQuery("#dcontainer").empty();
            jQuery("#dcontainer").html('');
            if (closeCallback && typeof closeCallback == "function") {
                closeCallback();
            }
        });
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }
    function isHTML(str) {
        var a = document.createElement('div');
        a.innerHTML = str;
        for (var c = a.childNodes, i = c.length; i--;) {
            if (c[i].nodeType == 1) return true;
        }
        return false;
    }
}