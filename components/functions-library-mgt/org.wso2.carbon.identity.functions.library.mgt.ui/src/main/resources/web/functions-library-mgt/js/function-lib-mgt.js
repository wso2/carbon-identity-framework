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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var scriptStringHeader = "";
var scriptStringContent = [];
var scriptStringFooter = "";

var addFunctionLib = $("#addFunctionlib");

var myCodeMirror = CodeMirror.fromTextArea(scriptTextArea, {
    theme: "mdn-like",
    keyMap: "sublime",
    mode: "javascript",
    lineNumbers: true,
    indentUnit: 4,
    lineWrapping: true,
    lineWiseCopyCut: true,
    pasteLinesPerSelection: true,
    extraKeys: {
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
    indentWithTabs: false,
    autoCloseBrackets: true,
    matchBrackets: true,
    gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers", "CodeMirror-foldgutter"],
    foldGutter: true,
    lint: true,
    showCursorWhenSelecting: true,
    styleActiveLine: true,
});

var doc = myCodeMirror.getDoc();


function autoFormatSelection(cm) {
    var range = getSelectedRange();
    cm.autoFormatRange(range.from, range.to);
}

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

function checkEmptyEditorContent() {
    document.getElementById('scriptTextArea').value = doc.getValue();
}

function encodeFunctionLibScript() {
    doc.setValue(btoa(doc.getValue()));
}
