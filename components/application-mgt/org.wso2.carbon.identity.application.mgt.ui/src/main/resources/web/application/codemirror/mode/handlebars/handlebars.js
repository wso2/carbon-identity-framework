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

(function (mod) {
    if (typeof exports == "object" && typeof module == "object") // CommonJS
        mod(require("../../lib/codemirror"), require("../../addon/mode/simple"), require("../../addon/mode/multiplex"));
    else if (typeof define == "function" && define.amd) // AMD
        define(["../../lib/codemirror", "../../addon/mode/simple", "../../addon/mode/multiplex"], mod);
    else // Plain browser env
        mod(CodeMirror);
})(function (CodeMirror) {
    "use strict";

    CodeMirror.defineSimpleMode("handlebars-tags", {
        start: [
            {regex: /\{\{!--/, push: "dash_comment", token: "comment"},
            {regex: /\{\{!/, push: "comment", token: "comment"},
            {regex: /\{\{/, push: "handlebars", token: "tag"}
        ],
        handlebars: [
            {regex: /\}\}/, pop: true, token: "tag"},

            // Double and single quotes
            {regex: /"(?:[^\\"]|\\.)*"?/, token: "string"},
            {regex: /'(?:[^\\']|\\.)*'?/, token: "string"},

            // Handlebars keywords
            {regex: />|[#\/]([A-Za-z_]\w*)/, token: "keyword"},
            {regex: /(?:else|this)\b/, token: "keyword"},

            // Numeral
            {regex: /\d+/i, token: "number"},

            // Atoms like = and .
            {regex: /=|~|@|true|false/, token: "atom"},

            // Paths
            {regex: /(?:\.\.\/)*(?:[A-Za-z_][\w\.]*)+/, token: "variable-2"}
        ],
        dash_comment: [
            {regex: /--\}\}/, pop: true, token: "comment"},

            // Commented code
            {regex: /./, token: "comment"}
        ],
        comment: [
            {regex: /\}\}/, pop: true, token: "comment"},
            {regex: /./, token: "comment"}
        ],
        meta: {
            blockCommentStart: "{{--",
            blockCommentEnd: "--}}"
        }
    });

    CodeMirror.defineMode("handlebars", function (config, parserConfig) {
        var handlebars = CodeMirror.getMode(config, "handlebars-tags");
        if (!parserConfig || !parserConfig.base) return handlebars;
        return CodeMirror.multiplexingMode(
            CodeMirror.getMode(config, parserConfig.base),
            {open: "{{", close: "}}", mode: handlebars, parseDelimiters: true}
        );
    });

    CodeMirror.defineMIME("text/x-handlebars-template", "handlebars");
});
