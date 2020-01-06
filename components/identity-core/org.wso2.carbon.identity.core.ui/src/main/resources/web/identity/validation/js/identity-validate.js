/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This file includes javascript functions that can be used for form input validation.
 * This file expects dialog/js/dialog.js to be included while including this script.
 */

/**
 * Returns the regular expression for the provided pattern key.
 * If no regex is defined for the provided input returns the provided input as a regex
 *
 * @param pattern regex pattern key or undefined regex as a string
 */
function getPattern(pattern) {
    var regex;
    switch (pattern) {
        case "digits-only":
            regex = /^[0-9]+$/;
            break;
        case "alphabetic-only":
            regex = /^[a-zA-Z]+$/;
            break;
        case "alphanumerics-only":
            regex = /^[a-zA-Z0-9]+$/;
            break;
        case "url":
            regex = /^(([^:/?#]+):)?(([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;
            break;
        case "email":
            regex = /^(.+)@(.+)$/;
            break;
        case "whitespace-exists":
            regex = /.*\s+.*/;
            break;
        case "uri-reserved-exists":
            regex = /.*[:/\?#\[\]@!\$&'\(\)\*\+,;=]+.*/;
            break;
        case "uri-unsafe-exists":
            regex = /.*[<>%\{\}\|\^~\[\]`]+.*/;
            break;
        case "html-meta-exists":
            regex = /.*[&<>"'/]+.*/;
            break;
        case "xml-meta-exists":
            regex = /.*[&<>"']+.*/;
            break;
        case "http-url":
            regex = /^(http:)([^/?#])?(:)?(([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;
            break;
        case "https-url":
            regex = /^(https:)([^/?#])?(:)?(([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;
            break;
        case "ftp-url":
            regex = /^(ftp:)([^/?#])?(:)?(([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;
            break;
        case "registry-invalid-chars-exists":
            regex = /[~!@#;%^*()+={}|<>\\"'/,]+/;
            break;
        case "fragment-free-url":
            regex = /^(([^:/?#]+):)?(([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?([^#])?/;
            break;
        case "invalid-username-search":
            regex = /.*[&<>"]+.*/;
            break;
        default:
            regex = new RegExp(pattern);
            break;
    }

    return regex;
}

/**
 * Checks if the provided input value is valid against the given white list pattern array
 *
 * @param input input value
 * @param whiteListPatterns white list pattern array
 * @returns {boolean} true if input value is valid
 */
function isWhiteListed(input, whiteListPatterns) {

    var isValid = false;
    var pattern;
    for (var i = 0; i < whiteListPatterns.length; i++) {
        pattern = getPattern(whiteListPatterns[i]);
        isValid = pattern.test(input);
        if (isValid) {
            break;
        }
    }

    return isValid;
}

/**
 * Checks if the provided input value is valid against the given black list pattern array
 *
 * @param input input value
 * @param blackListPatterns black list pattern array
 * @returns {boolean} true if input value is valid
 */
function isNotBlackListed(input, blackListPatterns) {

    var isValid = false;
    var pattern;
    for (var i = 0; i < blackListPatterns.length; i++) {
        pattern = getPattern(blackListPatterns[i]);
        isValid = !pattern.test(input);

        if (!isValid) {
            break;
        }
    }

    return isValid;
}

/**
 * Returns a comma separated string of patterns given by the patterns array
 *
 * @param patterns
 * @returns {string}
 */
function getPatternString(patterns) {

    var patternString = "";
    for (var i = 0; i < patterns.length; i++) {
        patternString += getPattern(patterns[i]).toString();
        if ((patterns.length - 1) != i) {
            patternString += ", ";
        }
    }

    return patternString;
}

/**
 * Extracts the given validator object and show the confirmation dialog.
 * Expects handleYes, handleNo, closeCallback as specified in dialog.js
 * @param validationObj validator object
 * @param msg   custom message
 * @param handleYes function to be called upon user clicking yes
 * @param handleNo  function to be called upon user clicking no
 * @param closeCallback
 * @returns {boolean} true if object contains isValid = true
 */
function isValidConfirmationDialog(validationObj, msg, handleYes, handleNo, closeCallback){
    if (validationObj['isValid'] === true) {
        return true;
    }

    var label = validationObj['label'];
    var whiteListPatterns = validationObj['whiteListPatterns'];
    var blackListPatterns = validationObj['blackListPatterns'];

    var message = msg.replaceAll('{0}', label).replaceAll('{1}', whiteListPatterns === "" ? 'NONE' : whiteListPatterns)
        .replaceAll('{2}', blackListPatterns === "" ? 'NONE' : blackListPatterns);

    CARBON.showConfirmationDialog(message, handleYes, handleNo, closeCallback);
    return false;
}
/**
 * Extracts the given validator object
 *
 * @param validationObj validator object
 * @param msg custom message
 * @returns {boolean} true if object contains isValid = true
 */
function isValid(validationObj, msg) {

    if (validationObj['isValid'] === true) {
        return true;
    }

    var label = validationObj['label'];
    var whiteListPatterns = validationObj['whiteListPatterns'];
    var blackListPatterns = validationObj['blackListPatterns'];

    var message = msg.replaceAll('{0}', label).replaceAll('{1}', whiteListPatterns === "" ? 'NONE' : whiteListPatterns)
        .replaceAll('{2}', blackListPatterns === "" ? 'NONE' : blackListPatterns);

    CARBON.showErrorDialog(message);
    return false;
}

/**
 * Validates the given input element against white list or black list patterns
 *
 * @param inputElement input element to be evaluated
 * @returns {{isValid: boolean, label: string, whiteListPatterns: string, blackListPatterns: string}}
 */
function validateInput(inputElement) {

    var value = inputElement.value;

    var whiteListPatternString = "";
    var blackListPatternString = "";
    var labelString = "";

    if (value != null && value != 'null') {
        var whiteListPatterns = inputElement.getAttribute('white-list-patterns');
        var blackListPatterns = inputElement.getAttribute('black-list-patterns');

        if ((whiteListPatterns === null || whiteListPatterns === "") &&
            (blackListPatterns === null || blackListPatterns === "")) {
            return {
                isValid: true,
                label: labelString,
                whiteListPatterns: whiteListPatternString,
                blackListPatterns: blackListPatternString
            };
        }

        var isValid = false;
        var whiteListed = false;
        var notBlackListed = false;
        var whiteListPatternsProvided = false;
        var blackListPatternsProvided = false;

        if (whiteListPatterns != null && whiteListPatterns != "") {
            whiteListPatternsProvided = true;
            var patternArray = whiteListPatterns.split(' ');
            whiteListed = isWhiteListed(value, patternArray);
            whiteListPatternString = getPatternString(patternArray);
        }

        if (blackListPatterns != null && blackListPatterns != "") {
            blackListPatternsProvided = true;
            var patternArray = blackListPatterns.split(' ');
            notBlackListed = isNotBlackListed(value, patternArray);
            blackListPatternString = getPatternString(patternArray);
        }

        if (whiteListPatternsProvided && blackListPatternsProvided) {
            isValid = whiteListed || notBlackListed;
        } else if (whiteListPatternsProvided) {
            isValid = whiteListed;
        } else if (blackListPatternsProvided) {
            isValid = notBlackListed;
        }

        if (isValid === true) {
            return {
                isValid: true,
                label: labelString,
                whiteListPatterns: whiteListPatternString,
                blackListPatterns: blackListPatternString
            };
        } else {
            labelString = inputElement.getAttribute('label');
            if (labelString == null || labelString == "") {
                labelString = inputElement.getAttribute('name');
            }
            return {
                isValid: false,
                label: labelString,
                whiteListPatterns: whiteListPatternString,
                blackListPatterns: blackListPatternString
            };
        }
    }
}

/**
 * Validates all input elements of the form which defines attributes for white list or black list patterns against them.
 *
 * @param form Form to be validated
 * @returns {{isValid: boolean, label: string, whiteListPatterns: string, blackListPatterns: string}}
 */
function validateForm(form) {

    var allInputs = form.getElementsByTagName('input');
    var len = allInputs.length;

    for (var i = 0; i < len; i++) {
        var validationObj = validateInput(allInputs[i]);

        if (validationObj['isValid'] === true) {
            continue;
        } else {
            return validationObj;
        }
    }

    return {
        isValid: true,
        label: "",
        whiteListPatterns: "",
        blackListPatterns: ""
    };
}

/**
 * Validates the given input element against white list or black list patterns and returns true if input is valid
 * and pops up an confirmation dialog to ask user's confirmation if input is invalid.
 *
 * @param inputElement input element to be evaluated
 * @param msg Message to be popped up if validations fails. If message contains {0}, {1} and {2},
 * they are replaced by the input label, white list patterns and black list patterns respectively.
 * @param handleYes function to be called upon user clicking yes
 * @param handleNo function to be called upon user clicking no
 * @param closeCallback
 * @returns {boolean} true if successfully validated
 */

function doValidateInputToConfirm(inputElement, msg, handleYes, handleNo, closeCallback) {

    return isValidConfirmationDialog(validateInput(inputElement), msg , handleYes, handleNo, closeCallback);
}
/**
 * Validates the given input element against white list or black list patterns and returns true if input is valid
 * and pops up an error message if input is invalid.
 *
 * @param inputElement input element to be evaluated
 * @param msg Message to be popped up if validations fails. If message contains {0}, {1} and {2},
 * they are replaced by the input label, white list patterns and black list patterns respectively.
 * @returns {boolean} true if successfully validated
 */
function doValidateInput(inputElement, msg) {

    return isValid(validateInput(inputElement), msg);
}

/**
 * Validates all input elements of the form which defines attributes for white list or black list patterns against them,
 * and returns true if all inputs are valid and pops up an error message if inputs are invalid.
 *
 * @param form
 * @param msg Message to be popped up if validations fails. If message contains {0}, {1} and {2},
 * they are replaced by the input label, white list patterns and black list patterns respectively.
 * @returns {boolean} true if successfully validated
 */
function doValidateForm(form, msg) {

    return isValid(validateForm(form), msg);
}

/**
 * Util function to escape regex special characters
 * @param str
 * @returns {void|string|XML}
 */
function escapeRegExp(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"); // $& means the whole matched string
}

String.prototype.replaceAll = function (find, replace) {
    var str = this;
    return str.replace(new RegExp(escapeRegExp(find), 'g'), replace);
};

