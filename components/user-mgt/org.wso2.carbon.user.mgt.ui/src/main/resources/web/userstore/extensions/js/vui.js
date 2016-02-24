/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/*all validation functions required by the .jsp files*/


function validateEmptyById(fldId) {
    var fld = document.getElementById(fldId);
    var error = "";
    var value = fld.value;
    if (value.length == 0) {
        error = fld.name + " ";
        return error;
    }

    value = value.replace(/^\s+/, "");
    if (value.length == 0) {
        error = fld.name + "(contains only spaces) ";
        return error;
    }

    return error;
}


function isAtleastOneCheckedIfExisting(fldname) {
    var foundOne = false;
    var elems = document.getElementsByName(fldname);

    if (elems.length == 0) {
        foundOne = true;
    } else {
        var counter = 0;
        for (counter = 0; counter < elems.length; counter++) {
            if (elems[counter].checked == true)
                foundOne = true;
        }
    }
    return foundOne;
}

function isAtleastOneChecked(fldname) {
    var foundOne = false;
    var elems = document.getElementsByName(fldname);

    var counter = 0;
    for (counter = 0; counter < elems.length; counter++) {
        if (elems[counter].checked == true)
            foundOne = true;
    }
    return foundOne;
}

function doSelectAll(targetfldname) {
    var elems = document.getElementsByName(targetfldname);
    for (var counter = 0; counter < elems.length; counter++) {
        if(!elems[counter].disabled){
            elems[counter].checked = true;
        }
    }
}

function doUnSelectAll(targetfldname) {
    var elems = document.getElementsByName(targetfldname);
    for (var counter = 0; counter < elems.length; counter++) {
        if(!elems[counter].disabled){
            elems[counter].checked = false;
        }
    }
}

function validateStorePassword(fld1name) {
    var error = "";
    var invalid = "&";
    var pw1 = document.getElementById(fld1name).value;

    // check for spaces
    if (pw1 != null && pw1.length > 0) {
        if (pw1.indexOf(invalid) > -1) {
            error = "Sorry, invalid charactor in password";
            return error;
        }
    }
    return error;
}

function checkStorePasswordRetype(fld1name, fld2name) {
    var error = "";
    var invalid = "&";
    var pw1 = document.getElementById(fld1name).value;
    var pw2 = document.getElementById(fld2name).value;
    // check for a value in both fields. 

    if (pw1 != pw2) {
        error = "Password and Password Repeat do not match. Please re-enter.";
        return error;
    }

    return error;
}
