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

var dummy = {
    attrs: {
        color: ["red", "green", "blue", "purple", "white", "black", "yellow"],
        size: ["large", "medium", "small"],
        description: null
    },
    children: []
};
var tags = {
    "!top": ["top"],
    "!attrs": {
        id: null,
        class: ["A", "B", "C"]
    },
    top: {
        attrs: {
            lang: ["en", "de", "fr", "nl"],
            freeform: null
        },
        children: ["LocalAndOutBoundAuthenticationConfig"]
    },
    LocalAndOutBoundAuthenticationConfig: {
        attrs: {
            name: null,
            isduck: ["yes", "no"]
        },
        children: ["AuthenticationSteps", "AuthenticationStep", "LocalAuthenticatorConfigs", "FederatedIdentityProviders",
            "SubjectStep", "AttributeStep", "StepOrder"]
    },
    AuthenticationSteps: dummy,
    AuthenticationStep: dummy,
    LocalAuthenticatorConfigs: dummy,
    FederatedIdentityProviders: dummy,
    SubjectStep: dummy,
    AttributeStep: dummy,
    StepOrder: dummy
};

function completeAfter(cm, pred) {
    var cur = cm.getCursor();
    if (!pred || pred()) setTimeout(function () {
        if (!cm.state.completionActive)
            cm.showHint({completeSingle: false});
    }, 100);
    return CodeMirror.Pass;
}

function completeIfAfterLt(cm) {
    return completeAfter(cm, function () {
        var cur = cm.getCursor();
        return cm.getRange(CodeMirror.Pos(cur.line, cur.ch - 1), cur) == "<";
    });
}

function completeIfInTag(cm) {
    return completeAfter(cm, function () {
        var tok = cm.getTokenAt(cm.getCursor());
        if (tok.type == "string" && (!/['"]/.test(tok.string.charAt(tok.string.length - 1)) || tok.string.length == 1)) return false;
        var inner = CodeMirror.innerMode(cm.getMode(), tok.state).state;
        return inner.tagName;
    });
}

var myCodeMirror = CodeMirror.fromTextArea(seqContent, {
    theme: "mdn-like",
    keyMap: "sublime",
    mode: "xml",
    lineNumbers: true,
    lineWrapping: true,
    indentUnit: 4,
    lineWiseCopyCut: true,
    pasteLinesPerSelection: true,
    indentWithTabs: false,
    autoCloseBrackets: true,
    matchBrackets: true,
    gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers", "CodeMirror-foldgutter"],
    foldGutter: true,
    lint: true,
    showCursorWhenSelecting: true,
    styleActiveLine: true,
    extraKeys: {
        "'<'": completeAfter,
        "'/'": completeIfAfterLt,
        "' '": completeIfInTag,
        "'='": completeIfInTag,
        "Ctrl-Space": "autocomplete",
        "F11": function (myCodeMirror) {
            myCodeMirror.setOption("fullScreen", !myCodeMirror.getOption("fullScreen"));
        },
        "Esc": function (myCodeMirror) {
            if (myCodeMirror.getOption("fullScreen")) myCodeMirror.setOption("fullScreen", false);
        },
        "Shift-Ctrl-F": function (myCodeMirror) {
            CodeMirror.commands["selectAll"](myCodeMirror);
            autoFormatSelection(myCodeMirror);
        },
        Tab: function (myCodeMirror) {
            myCodeMirror.execCommand("indentMore");
        },
        "Shift-Tab": function (myCodeMirror) {
            myCodeMirror.execCommand("indentLess");
        }
    },
    hintOptions: {schemaInfo: tags}
});

var doc = myCodeMirror.getDoc();
$('.show_errors_toggle_buttons a').each(function () {
    $(this).click(function () {
        $(this).parent().parent().next().slideToggle();
        $(this).parent().parent().find('a').each(function () {
            $(this).toggle();
        });
    });
});

$(".CodeMirror").append('<div id="toggleEditorSize" class="maximizeIcon" title="Toggle Full Screen"></div>');
$("#toggleEditorSize").click(function () {
    if (myCodeMirror.getOption("fullScreen")) {
        $(this).addClass("maximizeIcon");
        $(this).removeClass("minimizeIcon");
        myCodeMirror.setOption("fullScreen", false);
        $("#codeMirrorTemplate").show();
    } else {
        $(this).addClass("minimizeIcon");
        $(this).removeClass("maximizeIcon");
        myCodeMirror.setOption("fullScreen", true);
        $("#codeMirrorTemplate").hide();
    }
});

function checkEmptyEditorContent() {
    var editorContent = doc.getValue();
    if (editorContent.length === 0) {
        return true;
    }
    return false;
}

function getSelectedRange() {
    return {from: myCodeMirror.getCursor(true), to: myCodeMirror.getCursor(false)};
}

function autoFormatSelection(cm) {
    var range = getSelectedRange();
    cm.autoFormatRange(range.from, range.to);
}

jQuery('body').delegate("h2.trigger", 'click', bindHeadingCollapse);

function bindHeadingCollapse() {
    if (jQuery(this).next().is(":visible")) {
        this.className = "active " + this.className;
    } else {
        this.className = this.className.replace(/\bactive\b/g, "");
    }
    jQuery(this).next().slideToggle("fast");
    var $el = $(this);
    var $container = $el.siblings('ul');
    if ($el === $("#template_list .type > h2")) {
        $container.slideToggle(function () {
            if ($container.css('display') == 'none') {
                $el.addClass('active');
            }
            else {
                $el.removeClass('active');
            }
        });
    }
    return false; //Prevent the browser jump to the link anchor
}

var cursorCoordsBeforeChange, cursorCoordsAfterChange, mark, startLine;
myCodeMirror.on("change", function (instance, ch) {
    cursorCoordsAfterChange = myCodeMirror.coordsChar(myCodeMirror.cursorCoords());
});

myCodeMirror.on("beforeChange", function (instance, changeObj) {
    cursorCoordsBeforeChange = myCodeMirror.coordsChar(myCodeMirror.cursorCoords());
    startLine = cursorCoordsBeforeChange.line;
});

myCodeMirror.on('inputRead', function onChange(editor, input) {
    if (input.text[0] === ';' || input.text[0] === ' ') {
        return;
    }
    CodeMirror.commands.autocomplete(myCodeMirror, null, {completeSingle: false})
});

jQuery(function ($) {
    var _oldShow = $.fn.show;
    $.fn.show = function (speed, oldCallback) {
        return $(this).each(function () {
            var obj = $(this),
                newCallback = function () {
                    if ($.isFunction(oldCallback)) {
                        oldCallback.apply(obj);
                    }
                    obj.trigger('afterShow');
                };
            // you can trigger a before show if you want
            obj.trigger('beforeShow');
            // now use the old function to show the element passing the new callback
            _oldShow.apply(obj, [speed, newCallback]);
        });
    }
});

function checkJSSyntaxError(jsContent, elementWarn, elementErr) {
    var scriptLineNo = "";
    var scriptReg = /<!\[CDATA\[/;
    myCodeMirror.eachLine(function (line) {
        if (line.text.trim().match(scriptReg)) {
            scriptLineNo = myCodeMirror.getLineNumber(line) + 1;
        }
    });
    JSHINT(jsContent);
    for (var i = 0; i < JSHINT.errors.length; ++i) {
        var err = JSHINT.errors[i];
        if (!err) {
            continue;
        } else if (err.code.lastIndexOf("W", 0) === 0) {
            elementWarn.append("<li>" + err.reason
                + "<span>[ Ln: " + (Number(scriptLineNo) + Number(err.line)) + " ch:" + err.character + " ]</span></li>");
        } else {
            elementErr.append("<li>" + err.reason
                + "<span>[ Ln: " + (Number(scriptLineNo) + Number(err.line)) + " ch:" + err.character + " ]</span></li>");
        }
    }
}

function checkJSSyntaxErrorOfFile(jsContent, elementWarn, elementErr) {
    JSHINT(jsContent);
    for (var i = 0; i < JSHINT.errors.length; ++i) {
        var err = JSHINT.errors[i];
        if (!err) {
            continue;
        } else if (err.code.lastIndexOf("W", 0) === 0) {
            elementWarn.append("<li>" + err.reason + "</li>");
        } else {
            elementErr.append("<li>" + err.reason + "</li>");
        }
    }
}

function validateStepConfig(adaptiveScript, configWithoutAdaptiveScript, elementWarn, elementErr) {
    var stepsInUI = getExecuteStepsInConfig(configWithoutAdaptiveScript);
    var stepsInScript = getExecuteStepsInScript(adaptiveScript);
    var stepDifference = diffArray(stepsInUI, stepsInScript);
    if (stepsInUI.length < stepsInScript.length || stepsInUI.length == stepsInScript.length) {
        if (stepDifference.script.length > 0) {
            for (var i = 0; i < stepDifference.script.length; ++i) {

                var lineNo = [];
                var stepReg = new RegExp("executeStep\\(" + stepDifference.script[i] + "+", "g");
                myCodeMirror.eachLine(function (line) {
                    if (line.text.trim().match(stepReg)) {
                        lineNo.push(myCodeMirror.getLineNumber(line) + 1);
                    }
                });

                elementErr.append("<li>Could not find matching Authentication Step for script executeStep <b>"
                    + stepDifference.script[i] + "</b> [Ln: " + lineNo.join() + "].</li>");
            }
        }
    }

    if (stepsInUI.length > stepsInScript.length) {
        for (var i = 0; i < stepDifference.ui.length; ++i) {
            elementWarn.append("<li>Could not find matching 'executeStep' function for" +
                " <span>Step " + stepDifference.ui[i] + ".</span></li>");
        }
    }
}

function validateStepConfigOfFile(adaptiveScript, configWithoutAdaptiveScript, elementWarn, elementErr) {
    var stepsInUI = getExecuteStepsInConfig(configWithoutAdaptiveScript);
    var stepsInScript = getExecuteStepsInScript(adaptiveScript);
    var stepDifference = diffArray(stepsInUI, stepsInScript);
    if (stepsInUI.length < stepsInScript.length || stepsInUI.length == stepsInScript.length) {
        if (stepDifference.script.length > 0) {
            for (var i = 0; i < stepDifference.script.length; ++i) {

                var lineNo = [];
                var stepReg = new RegExp("executeStep\\(" + stepDifference.script[i] + "+", "g");
                elementErr.append("<li>Could not find matching Authentication Step for script executeStep <b>"
                    + stepDifference.script[i] + "</b>.</li>");
            }
        }
    }

    if (stepsInUI.length > stepsInScript.length) {
        for (var i = 0; i < stepDifference.ui.length; ++i) {
            elementWarn.append("<li>Could not find matching 'executeStep' function for" +
                " <span>Step " + stepDifference.ui[i] + ".</span></li>");
        }
    }
}

function getExecuteStepsInConfig(config) {
    var currentConfigSteps = [];
    var result = config.match(/\<StepOrder>[\s\S]*?\<\/StepOrder>/g);
    if (typeof result !== 'undefined' && result !== null) {
        var uniqueStepsInConfig = result.filter(onlyUnique);

        $(uniqueStepsInConfig).each(function (i, e) {
            var currentInt = parseInt(e.replace(/\<StepOrder>/g, "").replace(/\<\/StepOrder>/g, ""));
            currentConfigSteps.push(currentInt);
        });
    }
    return currentConfigSteps.sort(sortNumber);
}

function getExecuteStepsInScript(script) {
    var stepsIntArray = [];
    var currentScriptMinified = script.replace(/(?:\r\n|\r|\n)/g, '').replace(/\s/g, '');
    var result = currentScriptMinified.match(/executeStep\([0-9]+/g);
    if (typeof result !== 'undefined' && result !== null) {
        var uniqueStepsInScript = result.filter(onlyUnique);

        $(uniqueStepsInScript).each(function (i, e) {
            var currentInt = parseInt(e.replace(/executeStep\(/g, ''));
            stepsIntArray.push(currentInt);
        });
    }
    return stepsIntArray.sort(sortNumber);
}

function diffArray(arrUI, arrScript) {
    var difference = {
        ui: [],
        script: [],
    };

    arrUI.map(function (val) {
        arrScript.indexOf(val) < 0 ? difference.ui.push(val) : '';
    });

    arrScript.map(function (val) {
        arrUI.indexOf(val) < 0 ? difference.script.push(val) : '';
    });

    return difference;
}

function updateDefaultSeq() {
    $("#update-default-authSeq-form").submit();
}

function addDefaultSeq() {
    $("#add-default-auth-seq-form").submit();
}

function onlyUnique(value, index, self) {
    return self.indexOf(value) === index;
}

function sortNumber(a, b) {
    return a - b;
}

function removeHtmlContent() {
    $('.editor-error-warn-container li').remove();
    $('.err_warn_text').empty();
    $('.editor-error-content, .editor-warning-content').hide();
}


function extractWithoutAuthScript(seqContent) {
    var withoutScript = seqContent.replace(/\<AuthenticationScript.+>[\s\S]*?\<\/AuthenticationScript>/g, "");
    return withoutScript;
}

function extractAdaptiveAuthScript(seqContent, elementWarn, elementErr) {
    var startOfScriptRegex = /<!\[CDATA\[/;
    var match = startOfScriptRegex.exec(seqContent);
    var startIndexScript;
    if (match) {
        startIndexScript = parseInt(match.index,10) + parseInt(9,10);
    }
    var endOfScript = /\]\]>/;
    var match = endOfScript.exec(seqContent);
    var endIndexScript;
    if (match) {
        endIndexScript = match.index;
    }
    var adaptiveScript;
    if (startIndexScript != null && endIndexScript != null) {
        adaptiveScript = seqContent.substring(startIndexScript, endIndexScript);
        var functionRegex = new RegExp("onLoginRequest\\([a-zA-Z_0-9_$][^)]*\\)", "g");
        if (typeof adaptiveScript !== 'undefined' && adaptiveScript !== null &&
            !adaptiveScript.trim().match(functionRegex)) {
            elementErr.append("<li>Missing required function: <b>onLoginRequest(parameter)</b>.</li>");
        }
    }
    return adaptiveScript;
}

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
        if (okButton || cancelButton) {
            jQuery("#dialog").dialog({
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    return false;
                },
                buttons: {
                    "OK": function () {
                        if (callback && typeof callback == "function")
                            callback();
                        jQuery(this).dialog("destroy").remove();
                        jQuery("#dcontainer").empty();
                        return false;
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
        } else {
            $('.ui-dialog-buttonpane button:contains(OK)').hide();
        }

        if (cancelButton) {
            $('.ui-dialog-buttonpane button:contains(Cancel)').attr("id", "dialog-confirm_cancel-button");
            $('#dialog-confirm_cancel-button').html(cancelButton);
        } else {
            $('.ui-dialog-buttonpane button:contains(Cancel)').hide();
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