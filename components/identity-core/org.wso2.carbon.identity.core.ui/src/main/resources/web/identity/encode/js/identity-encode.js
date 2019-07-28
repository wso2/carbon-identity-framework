/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function encodeForHTML(value) {
    // Create a in-memory div, set it's inner text(which jQuery automatically encodes)
    // then grab the encoded contents back out.  The div never exists on the page.
    var output = $('<div/>').text(value).html();
    output = output.replace(/"/g, "&quot;");
    output = output.replace(/'/g, '&#39;');

    return output;
}

function encodeQuotesForJavascript(value) {
    // Escape only non escaped quotes
    var output = value;
    output = output.replace(/\\([\s\S])|(['"])/ig, "\\$1$2");

    return output;
}
