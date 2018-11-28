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
        mod(require("../../lib/codemirror"));
    else if (typeof define == "function" && define.amd) // AMD
        define(["../../lib/codemirror"], mod);
    else // Plain browser env
        mod(CodeMirror);
})(function (CodeMirror) {
    "use strict";

// Depends on js-yaml.js from https://github.com/nodeca/js-yaml

// declare global: jsyaml

    CodeMirror.registerHelper("lint", "yaml", function (text) {
        var found = [];
        if (!window.jsyaml) {
            if (window.console) {
                window.console.error("Error: window.jsyaml not defined, CodeMirror YAML linting cannot run.");
            }
            return found;
        }
        try {
            jsyaml.load(text);
        } catch (e) {
            var loc = e.mark,
                // js-yaml YAMLException doesn't always provide an accurate lineno
                // e.g., when there are multiple yaml docs
                // ---
                // ---
                // foo:bar
                from = loc ? CodeMirror.Pos(loc.line, loc.column) : CodeMirror.Pos(0, 0),
                to = from;
            found.push({from: from, to: to, message: e.message});
        }
        return found;
    });

});
