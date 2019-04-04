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
        children: ["LocalAndOutBoundAuthenticationConfig", "ClaimConfig"]
    },
    LocalAndOutBoundAuthenticationConfig: {
        attrs: {
            name: null,
            isduck: ["yes", "no"]
        },
        children: ["AuthenticationSteps", "AuthenticationStep", "LocalAuthenticatorConfigs", "FederatedIdentityProviders",
            "SubjectStep", "AttributeStep", "StepOrder"]
    },
    ClaimConfig: {
        attrs: {name: null},
        children: ["RoleClaimURI", "LocalClaimDialect", "ClaimMappings", "AlwaysSendMappedLocalSubjectId", "IdpClaim"]
    },
    AuthenticationSteps: dummy, AuthenticationStep: dummy, LocalAuthenticatorConfigs: dummy, FederatedIdentityProviders: dummy,
    SubjectStep: dummy, AttributeStep: dummy, StepOrder: dummy, RoleClaimURI: dummy, LocalClaimDialect: dummy,
    ClaimMappings: dummy, AlwaysSendMappedLocalSubjectId: dummy, IdpClaim: dummy
};

function completeAfter(cm, pred) {
    var cur = cm.getCursor();
    if (!pred || pred()) setTimeout(function() {
        if (!cm.state.completionActive)
            cm.showHint({completeSingle: false});
    }, 100);
    return CodeMirror.Pass;
}

function completeIfAfterLt(cm) {
    return completeAfter(cm, function() {
        var cur = cm.getCursor();
        return cm.getRange(CodeMirror.Pos(cur.line, cur.ch - 1), cur) == "<";
    });
}

function completeIfInTag(cm) {
    return completeAfter(cm, function() {
        var tok = cm.getTokenAt(cm.getCursor());
        if (tok.type == "string" && (!/['"]/.test(tok.string.charAt(tok.string.length - 1)) || tok.string.length == 1)) return false;
        var inner = CodeMirror.innerMode(cm.getMode(), tok.state).state;
        return inner.tagName;
    });
}

var myCodeMirror = CodeMirror.fromTextArea(templateContent, {
    theme: "mdn-like",
    keyMap: "sublime",
    mode: "xml",
    lineNumbers: true,
    indentUnit: 4,
    lineWrapping: true,
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