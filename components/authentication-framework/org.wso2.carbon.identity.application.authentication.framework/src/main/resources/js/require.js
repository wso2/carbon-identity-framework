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

var internalRequire = (function () {

    var _require = function (libname) {
        var moduleInfo,
            head = '(function(exports,module,require){ ',
            code = '',
            tail = '})',

            code = loadLocalLibrary(libname);


        moduleInfo = {
            exports: {},
            require: _requireWrapper()
        };

        code = head + code + tail;

        var compiledWrapper = null;
        try {
            compiledWrapper = eval(code);
        } catch (e) {
            throw new Error("Error evaluating module " + libname + " line #" + e.lineNumber + ": " + e.message);
        }

        var args = [
            moduleInfo.exports, /* exports */
            moduleInfo, /* module */
            moduleInfo.require, /* require */
        ];
        try {
            compiledWrapper.apply(null, args);
        } catch (e) {
            throw new Error("Error executing module " + libname + " line #" + e.lineNumber + " : " + e.message);
        }
        return moduleInfo;
    };
    var _requireWrapper = function () {
        return function (libname) {
            var module = _require(libname);
            return module.exports;
        };
    };
    return _requireWrapper();
})

var require = internalRequire();
