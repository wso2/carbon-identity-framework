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
var orig = CodeMirror.hint.javascript;

CodeMirror.hint.javascript = function (cm) {
    var hintList = orig(cm) || {from: cm.getCursor(), to: cm.getCursor(), list: []};
    conditionalAuthFunctions.forEach(function (value) {
        hintList.list.push(value);
    });
    var hintListArr = [];
    if (hintList.constructor === Array) {
        hintListArr = hintList;
    } else {
        hintListArr = Array.from(hintList.list);
    }
    var cursor = cm.getCursor();
    var currentLine = cm.getLine(cursor.line);
    var start = cursor.ch;
    var end = start;
    while (end < currentLine.length && /[\w$]+/.test(currentLine.charAt(end))) ++end;
    while (start && /[\w$]+/.test(currentLine.charAt(start - 1))) --start;
    var curWord = (start != end) && currentLine.slice(start, end);
    var regex = new RegExp('^' + curWord, 'i');
    var subList = hintListArr.filter(function (item) {
        return item.match(regex);
    }).sort();
    var result = {
        list: !curWord ? hintListArr : subList,
        from: CodeMirror.Pos(cursor.line, start),
        to: CodeMirror.Pos(cursor.line, end)
    };
    return result;
};

