/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

//  Example stubs for PolicyEditorService operations.  This function is not intended
//  to be called, but rather as a source for copy-and-paste development.

//  Note that this stub has been generated for use in DOM environments.


function stubs() {
        // formatXML operation
    try {
        /* string */ var formatXMLReturn = PolicyEditorService.formatXML(/* string */ param_xml);
    } catch (e) {
        // fault handling
    }

    // getAvailableSchemas operation
    try {
        /* string */ var getAvailableSchemasReturn = PolicyEditorService.getAvailableSchemas();
    } catch (e) {
        // fault handling
    }

    // getPolicyDoc operation
    try {
        /* string */ var getPolicyDocReturn = PolicyEditorService.getPolicyDoc(/* string */ param_policyURL);
    } catch (e) {
        // fault handling
    }

    // getSchema operation
    try {
        /* string */ var getSchemaReturn = PolicyEditorService.getSchema(/* string */ param_fileName);
    } catch (e) {
        // fault handling
    }

}
stubs.visible = false;

var PolicyEditorService = new WebService("PolicyEditorServiceHttpsSoap12Endpoint");

PolicyEditorService.formatXML =
    function formatXML(/* string */ _xml)
    {
        var isAsync, request, response, resultValue;
        this._options = new Array();
        isAsync = (this.formatXML.callback != null && typeof(this.formatXML.callback) == 'function');
        request = this.formatXML_payload(/* string */ _xml);

        if (isAsync) {
            try {
                this._call(
                    "formatXML",
                    "http://www.w3.org/ns/wsdl/in-out",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.formatXML.callback, this.formatXML.onError)
                );
            } catch (e) {
                var error;
                if (WebServiceError.prototype.isPrototypeOf(e)) {
                    error = e;
                } else if (e.name != null) {
                    // Mozilla
                    error = new WebServiceError(e.name, e.message + " (" + e.fileName + "#" + e.lineNumber + ")");
                } else if (e.description != null) {
                    // IE
                    error = new WebServiceError(e.description, e.number, e.number);
                } else {
                    error = new WebServiceError(e, "Internal Error");
                }
                this.formatXML.onError(error);
            }
        } else {
            try {
                                response = this._call("formatXML", "http://www.w3.org/ns/wsdl/in-out", request);
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                                return resultValue;
            } catch (e) {
                if (typeof(e) == "string") throw(e);
                if (e.message) throw(e.message);
                throw (e.reason);
            }
        }
        return null; // Suppress warnings when there is no return.
    }
PolicyEditorService.formatXML_payload =
    function (/* string */ _xml) {

        return '<p:formatXML xmlns:p="http://org.wso2.wsf/tools">' +
                (_xml == null ? '' : '<xml xmlns="http://org.wso2.wsf/tools">' + this._encodeXML(_xml) + '</xml>') +
            '</p:formatXML>' ;
    }
PolicyEditorService.formatXML_payload.visible = false;
PolicyEditorService.formatXML.callback = null;

PolicyEditorService.getAvailableSchemas =
    function getAvailableSchemas()
    {
        var isAsync, request, response, resultValue;
        this._options = new Array();
        isAsync = (this.getAvailableSchemas.callback != null && typeof(this.getAvailableSchemas.callback) == 'function');
        request = this.getAvailableSchemas_payload();

        if (isAsync) {
            try {
                this._call(
                    "getAvailableSchemas",
                    "http://www.w3.org/ns/wsdl/in-out",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getAvailableSchemas.callback, this.getAvailableSchemas.onError)
                );
            } catch (e) {
                var error;
                if (WebServiceError.prototype.isPrototypeOf(e)) {
                    error = e;
                } else if (e.name != null) {
                    // Mozilla
                    error = new WebServiceError(e.name, e.message + " (" + e.fileName + "#" + e.lineNumber + ")");
                } else if (e.description != null) {
                    // IE
                    error = new WebServiceError(e.description, e.number, e.number);
                } else {
                    error = new WebServiceError(e, "Internal Error");
                }
                this.getAvailableSchemas.onError(error);
            }
        } else {
            try {
                                response = this._call("getAvailableSchemas", "http://www.w3.org/ns/wsdl/in-out", request);
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                                return resultValue;
            } catch (e) {
                if (typeof(e) == "string") throw(e);
                if (e.message) throw(e.message);
                throw (e.reason);
            }
        }
        return null; // Suppress warnings when there is no return.
    }
PolicyEditorService.getAvailableSchemas_payload =
    function () {

        return null;
    }
PolicyEditorService.getAvailableSchemas_payload.visible = false;
PolicyEditorService.getAvailableSchemas.callback = null;

PolicyEditorService.getPolicyDoc =
    function getPolicyDoc(/* string */ _policyURL)
    {
        var isAsync, request, response, resultValue;
        this._options = new Array();
        isAsync = (this.getPolicyDoc.callback != null && typeof(this.getPolicyDoc.callback) == 'function');
        request = this.getPolicyDoc_payload(/* string */ _policyURL);

        if (isAsync) {
            try {
                this._call(
                    "getPolicyDoc",
                    "http://www.w3.org/ns/wsdl/in-out",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getPolicyDoc.callback, this.getPolicyDoc.onError)
                );
            } catch (e) {
                var error;
                if (WebServiceError.prototype.isPrototypeOf(e)) {
                    error = e;
                } else if (e.name != null) {
                    // Mozilla
                    error = new WebServiceError(e.name, e.message + " (" + e.fileName + "#" + e.lineNumber + ")");
                } else if (e.description != null) {
                    // IE
                    error = new WebServiceError(e.description, e.number, e.number);
                } else {
                    error = new WebServiceError(e, "Internal Error");
                }
                this.getPolicyDoc.onError(error);
            }
        } else {
            try {
                                response = this._call("getPolicyDoc", "http://www.w3.org/ns/wsdl/in-out", request);
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                                return resultValue;
            } catch (e) {
                if (typeof(e) == "string") throw(e);
                if (e.message) throw(e.message);
                throw (e.reason);
            }
        }
        return null; // Suppress warnings when there is no return.
    }
PolicyEditorService.getPolicyDoc_payload =
    function (/* string */ _policyURL) {

        return '<p:getPolicyDoc xmlns:p="http://org.wso2.wsf/tools">' +
                (_policyURL == null ? '' : '<policyURL xmlns="http://org.wso2.wsf/tools">' + this._encodeXML(_policyURL) + '</policyURL>') +
            '</p:getPolicyDoc>' ;
    }
PolicyEditorService.getPolicyDoc_payload.visible = false;
PolicyEditorService.getPolicyDoc.callback = null;

PolicyEditorService.getSchema =
    function getSchema(/* string */ _fileName)
    {
        var isAsync, request, response, resultValue;
        this._options = new Array();
        isAsync = (this.getSchema.callback != null && typeof(this.getSchema.callback) == 'function');
        request = this.getSchema_payload(/* string */ _fileName);

        if (isAsync) {
            try {
                this._call(
                    "getSchema",
                    "http://www.w3.org/ns/wsdl/in-out",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getSchema.callback, this.getSchema.onError)
                );
            } catch (e) {
                var error;
                if (WebServiceError.prototype.isPrototypeOf(e)) {
                    error = e;
                } else if (e.name != null) {
                    // Mozilla
                    error = new WebServiceError(e.name, e.message + " (" + e.fileName + "#" + e.lineNumber + ")");
                } else if (e.description != null) {
                    // IE
                    error = new WebServiceError(e.description, e.number, e.number);
                } else {
                    error = new WebServiceError(e, "Internal Error");
                }
                this.getSchema.onError(error);
            }
        } else {
            try {
                                response = this._call("getSchema", "http://www.w3.org/ns/wsdl/in-out", request);
                                var extractedValue = WSRequest.util._stringValue(response.documentElement);
                                resultValue = /* string */ extractedValue;
                                return resultValue;
            } catch (e) {
                if (typeof(e) == "string") throw(e);
                if (e.message) throw(e.message);
                throw (e.reason);
            }
        }
        return null; // Suppress warnings when there is no return.
    }
PolicyEditorService.getSchema_payload =
    function (/* string */ _fileName) {

        return '<p:getSchema xmlns:p="http://org.wso2.wsf/tools">' +
                (_fileName == null ? '' : '<fileName xmlns="http://org.wso2.wsf/tools">' + this._encodeXML(_fileName) + '</fileName>') +
            '</p:getSchema>' ;
    }
PolicyEditorService.getSchema_payload.visible = false;
PolicyEditorService.getSchema.callback = null;



// WebService object.
function WebService(endpointName)
{
    this.readyState = 0;
    this.onreadystatechange = null;
    this.scriptInjectionCallback = null;
    this.proxyAddress = null;

    //public accessors for manually intervening in setting the address (e.g. supporting tcpmon)
    this.getAddress = function (endpointName)
    {
        return this._endpointDetails[endpointName].address;
    }

    this.setAddress = function (endpointName, address)
    {
        this._endpointDetails[endpointName].address = address;
    }

    // private helper functions
    this._getWSRequest = function()
    {
        var wsrequest;
        try {
            wsrequest = new WSRequest();
            // try to set the proxyAddress based on the context of the stub - browser or Mashup Server
            try {
                wsrequest.proxyEngagedCallback = this.scriptInjectionCallback;
                wsrequest.proxyAddress = this.proxyAddress;
            } catch (e) {
                try {
                    wsrequest.proxyEngagedCallback = this.scriptInjectionCallback;
                    wsrequest.proxyAddress = this.proxyAddress;
                } catch (e) { }
            }
        } catch(e) {
            try {
                wsrequest = new ActiveXObject("WSRequest");
            } catch(e) {
                try {
                    wsrequest = new SOAPHttpRequest();

                } catch (e) {
                    throw new WebServiceError("WSRequest object not defined.", "WebService._getWSRequest() cannot instantiate WSRequest object.");
                }
            }
        }
        return wsrequest;
    }

    this._endpointDetails =
        {
            "PolicyEditorServiceHttpSoap12Endpoint": {
                "type" : "SOAP12",
                "address" : "http://10.210.226.196:9763/services/PolicyEditorService.PolicyEditorServiceHttpSoap12Endpoint/",
                "action" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                },
                "soapaction" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                }
            },
            "PolicyEditorServiceHttpsSoap12Endpoint": {
                "type" : "SOAP12",
                "address" : "https://10.210.226.196:9443/services/PolicyEditorService.PolicyEditorServiceHttpsSoap12Endpoint/",
                "action" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                },
                "soapaction" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                }
            },
            "PolicyEditorServiceHttpSoap11Endpoint": {
                "type" : "SOAP11",
                "address" : "http://10.210.226.196:9763/services/PolicyEditorService.PolicyEditorServiceHttpSoap11Endpoint/",
                "action" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                },
                "soapaction" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                }
            },
            "PolicyEditorServiceHttpsSoap11Endpoint": {
                "type" : "SOAP11",
                "address" : "https://10.210.226.196:9443/services/PolicyEditorService.PolicyEditorServiceHttpsSoap11Endpoint/",
                "action" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                },
                "soapaction" : {
                    "getAvailableSchemas" : "urn:getAvailableSchemas",
                    "getPolicyDoc" : "urn:getPolicyDoc",
                    "getSchema" : "urn:getSchema",
                    "formatXML" : "urn:formatXML"
                }
            },
            "PolicyEditorServiceHttpEndpoint": {
                "type" : "HTTP",
                "address" : "http://10.210.226.196:9763/services/PolicyEditorService.PolicyEditorServiceHttpEndpoint/",
                "httplocation" : {
                    "getAvailableSchemas" : "getAvailableSchemas",
                    "getPolicyDoc" : "getPolicyDoc",
                    "getSchema" : "getSchema",
                    "formatXML" : "formatXML"
                },
                "httpmethod" : {
                        "getAvailableSchemas" : "POST",
                        "getPolicyDoc" : "POST",
                        "getSchema" : "POST",
                        "formatXML" : "POST"
                },
                "fitsInURLParams" : {
                        "getAvailableSchemas" : true,
                        "getPolicyDoc" : true,
                        "getSchema" : true,
                        "formatXML" : true
                }
            },
            "PolicyEditorServiceHttpsEndpoint": {
                "type" : "HTTP",
                "address" : "https://10.210.226.196:9443/services/PolicyEditorService.PolicyEditorServiceHttpsEndpoint/",
                "httplocation" : {
                    "getAvailableSchemas" : "getAvailableSchemas",
                    "getPolicyDoc" : "getPolicyDoc",
                    "getSchema" : "getSchema",
                    "formatXML" : "formatXML"
                },
                "httpmethod" : {
                        "getAvailableSchemas" : "POST",
                        "getPolicyDoc" : "POST",
                        "getSchema" : "POST",
                        "formatXML" : "POST"
                },
                "fitsInURLParams" : {
                        "getAvailableSchemas" : true,
                        "getPolicyDoc" : true,
                        "getSchema" : true,
                        "formatXML" : true
                }
            }
    };
    this.endpoint = endpointName;

    this.username = null;
    this.password = null;

    this._encodeXML = function (value) {
        var str = value.toString();
        str = str.replace(/&/g, "&amp;");
        str = str.replace(/</g, "&lt;");
        return(str);
    };

    this._setOptions = function (details, opName) {
        var options = new Array();

        if (details.type == 'SOAP12') options.useSOAP = 1.2;
        else if (details.type == 'SOAP11') options.useSOAP = 1.1;
        else if (details.type == 'HTTP') options.useSOAP = false;

        if (options.useSOAP != false) {
            if (details.action != null) {
                options.useWSA = true;
                options.action = details.action[opName];
            } else if (details.soapaction != null) {
                options.useWSA = false;
                options.action = details.soapaction[opName];
            } else {
                options.useWSA = false;
                options.action = undefined;
            }
        }

        if (details["httpmethod"] != null) {
            options.HTTPMethod = details.httpmethod[opName];
        } else {
            options.HTTPMethod = null;
        }

        if (details["httpinputSerialization"] != null) {
            options.HTTPInputSerialization = details.httpinputSerialization[opName];
        } else {
            options.HTTPInputSerialization= null;
        }

        if (details["httplocation"] != null) {
            options.HTTPLocation = details.httplocation[opName];
        } else {
            options.HTTPLocation = null;
        }

        if (details["httpignoreUncited"] != null) {
            options.HTTPLocationIgnoreUncited = details.httpignoreUncited[opName];
        } else {
            options.HTTPLocationIgnoreUncited = null;
        }

        if (details["httpqueryParameterSeparator"] != null) {
            options.HTTPQueryParameterSeparator = details.httpqueryParameterSeparator[opName];
        } else {
            options.HTTPQueryParameterSeparator = null;
        }

        if (details["policies"]) {
            var policies = details["policies"][opName];
            for(i=0; i<policies.length; i++) {
                if(policies[i] == "UTOverTransport") {
                    options.useWSS = true;
                    break;
                }
            }
        }

        return options;
    };

    this._call = function (opName, pattern, reqContent, callback, userdata)
    {
        var details = this._endpointDetails[this.endpoint];
        this._options = this._setOptions(details, opName);

        var isAsync = (typeof(callback) == 'function');

        var thisRequest = this._getWSRequest();
        thisRequest.pattern = pattern;
        if (isAsync) {
            thisRequest._userdata = userdata;
            thisRequest.onreadystatechange =
                function() {
                    if (thisRequest.readyState == 4) {
                        callback(thisRequest, userdata);
                    }
                }
        }

        if (this.username == null)
            thisRequest.open(this._options, details.address, isAsync);
        else
            thisRequest.open(this._options, details.address, isAsync, this.username, this.password);

        thisRequest.send(reqContent);
        if (isAsync) {
            return "";
        } else {
            try {
                var resultContent = thisRequest.responseText;
                if (resultContent == "") {
                    throw new WebServiceError("No response", "WebService._call() did not recieve a response to a synchronous request.");
                }
                var resultXML = thisRequest.responseXML;
            } catch (e) {
                throw new WebServiceError(e);
            }
            return resultXML;
        }
    };
}
WebService.visible = false;

WebService.utils = {
    toXSdate : function (thisDate) {
        var year = thisDate.getUTCFullYear();
        var month = thisDate.getUTCMonth() + 1;
        var day = thisDate.getUTCDate();

        return year + "-" +
            (month < 10 ? "0" : "") + month + "-" +
            (day < 10 ? "0" : "") + day + "Z";
    },

    toXStime : function (thisDate) {
        var hours = thisDate.getUTCHours();
        var minutes = thisDate.getUTCMinutes();
        var seconds = thisDate.getUTCSeconds();
        var milliseconds = thisDate.getUTCMilliseconds();

        return (hours < 10 ? "0" : "") + hours + ":" +
            (minutes < 10 ? "0" : "") + minutes + ":" +
            (seconds < 10 ? "0" : "") + seconds +
            (milliseconds == 0 ? "" : (milliseconds/1000).toString().substring(1)) + "Z";
    },

    toXSdateTime : function (thisDate) {
        var year = thisDate.getUTCFullYear();
        var month = thisDate.getUTCMonth() + 1;
        var day = thisDate.getUTCDate();
        var hours = thisDate.getUTCHours();
        var minutes = thisDate.getUTCMinutes();
        var seconds = thisDate.getUTCSeconds();
        var milliseconds = thisDate.getUTCMilliseconds();

        return year + "-" +
            (month < 10 ? "0" : "") + month + "-" +
            (day < 10 ? "0" : "") + day + "T" +
            (hours < 10 ? "0" : "") + hours + ":" +
            (minutes < 10 ? "0" : "") + minutes + ":" +
            (seconds < 10 ? "0" : "") + seconds +
            (milliseconds == 0 ? "" : (milliseconds/1000).toString().substring(1)) + "Z";
    },

    parseXSdateTime : function (dateTime) {
        var buffer = dateTime.toString();
        var p = 0; // pointer to current parse location in buffer.

        var era, year, month, day, hour, minute, second, millisecond;

        // parse date, if there is one.
        if (buffer.substr(p,1) == '-')
        {
            era = -1;
            p++;
        } else {
            era = 1;
        }

        if (buffer.charAt(p+2) != ':')
        {
            year = era * buffer.substr(p,4);
            p += 5;
            month = buffer.substr(p,2);
            p += 3;
            day = buffer.substr(p,2);
            p += 3;
        } else {
            year = 1970;
            month = 1;
            day = 1;
        }

        // parse time, if there is one
        if (buffer.charAt(p) != '+' && buffer.charAt(p) != '-')
        {
            hour = buffer.substr(p,2);
            p += 3;
            minute = buffer.substr(p,2);
            p += 3;
            second = buffer.substr(p,2);
            p += 2;
            if (buffer.charAt(p) == '.')
            {
                millisecond = parseFloat(buffer.substring(p))*1000;
                // Note that JS fractional seconds are significant to 3 places - xs:time is significant to more -
                // though implementations are only required to carry 3 places.
                p++;
                while (buffer.charCodeAt(p) >= 48 && buffer.charCodeAt(p) <= 57) p++;
            } else {
                millisecond = 0;
            }
        } else {
            hour = 0;
            minute = 0;
            second = 0;
            millisecond = 0;
        }

        var tzhour = 0;
        var tzminute = 0;
        // parse time zone
        if (buffer.charAt(p) != 'Z' && buffer.charAt(p) != '') {
            var sign = (buffer.charAt(p) == '-' ? -1 : +1);
            p++;
            tzhour = sign * buffer.substr(p,2);
            p += 3;
            tzminute = sign * buffer.substr(p,2);
        }

        var thisDate = new Date();
        thisDate.setUTCFullYear(year);
        thisDate.setUTCMonth(month-1);
        thisDate.setUTCDate(day);
        thisDate.setUTCHours(hour);
        thisDate.setUTCMinutes(minute);
        thisDate.setUTCSeconds(second);
        thisDate.setUTCMilliseconds(millisecond);
        thisDate.setUTCHours(thisDate.getUTCHours() - tzhour);
        thisDate.setUTCMinutes(thisDate.getUTCMinutes() - tzminute);
        return thisDate;
    },

    _nextPrefixNumber : 0,

    _QNameNamespaceDecl : function (qn) {
        if (qn.uri == null) return "";
        var prefix = qn.localName.substring(0, qn.localName.indexOf(":"));
        if (prefix == "") {
            prefix = "n" + ++this._nextPrefixNumber;
        }
        return ' xmlns:' + prefix + '="' + qn.uri + '"';
    },

    _QNameValue : function(qn) {
        if (qn.uri == null) return qn.localName;
        var prefix, localName;
        if (qn.localName.indexOf(":") >= 0) {
            prefix = qn.localName.substring(0, qn.localName.indexOf(":"));
            localName = qn.localName.substring(qn.localName.indexOf(":")+1);
        } else {
            prefix = "n" + this._nextPrefixNumber;
            localName = qn.localName;
        }
        return prefix + ":" + localName;
    },

    scheme : function (url) {
        var s = url.substring(0, url.indexOf(':'));
        return s;
    },

    domain : function (url) {
        var d = url.substring(url.indexOf('://') + 3, url.indexOf('/',url.indexOf('://')+3));
        return d;
    },

    domainPort : function (url) {
        var d = this.domain(url);
        if (d.indexOf(":") >= 0)
        d = d.substring(d.indexOf(':') +1);
        return d;
    },

    domainNoPort : function (url) {
        var d = this.domain(url);
        if (d.indexOf(":") >= 0)
        d = d.substring(0, d.indexOf(':'));
        return d;
    },

    _serializeAnytype : function (name, value, namespace, optional) {
        // dynamically serialize an anyType value in xml, including setting xsi:type.
        if (optional && value == null) return "";
        var type = "xs:string";
        if (value == null) {
            value = "";
        } else if (typeof(value) == "number") {
            type = "xs:double";
        } else if (typeof(value) == "object" && value.nodeType != undefined) {
            type = "xs:anyType";
            value = WebService.utils._serializeXML(value);
        } else if (typeof(value) == "boolean") {
            type = "xs:boolean";
        } else if (typeof(value) == "object" && Date.prototype.isPrototypeOf(value)) {
            type = "xs:dateTime";
            value = WebService.utils.toXSdateTime(value);
        } else if (value.match(/^\s*true\s*$/g) != null) {
            type = "xs:boolean";
        } else if (value.match(/^\s*false\s*$/g) != null) {
            type = "xs:boolean";
        } else if (!isNaN(Date.parse(value))) {
            type = "xs:dateTime";
            value = WebService.utils.toXSdateTime(new Date(Date.parse(value)));
        } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\dZ?\s*$/g) != null) {
            type = "xs:date";
        } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\d[\+\-]\d\d:\d\d\s*$/g) != null) {
            type = "xs:date";
        } else if (value.match(/^\s*\d\d:\d\d:\d\d\.?\d*Z?\s*$/g) != null) {
            type = "xs:time";
        } else if (value.match(/^\s*\d\d:\d\d:\d\d\.?\d*[\+\-]\d\d:\d\d\s*$/g) != null) {
            type = "xs:time";
        } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\dT\d\d:\d\d:\d\d\.?\d*Z?\s*$/g) != null) {
            type = "xs:dateTime";
        } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\dT\d\d:\d\d:\d\d\.?\d*[\+\-]\d\d:\d\d\s*$/g) != null) {
            type = "xs:dateTime";
        } else if (value.match(/^\s*\d\d*\.?\d*\s*$/g) != null) {
            type = "xs:double";
        } else if (value.match(/^\s*\d*\.?\d\d*\s*$/g) != null) {
            type = "xs:double";
        } else if (value.match(/^\s*\</g) != null) {

            var browser = WSRequest.util._getBrowser();
            var parseTest;
            if (browser == "ie" || browser == "ie7") {
                parseTest = new ActiveXObject("Microsoft.XMLDOM");
                parseTest.loadXML(value);
                if (parseTest.parseError == 0)
                    type = "xs:anyType";
            } else {
                var parser = new DOMParser();
                parseTest = parser.parseFromString(value,"text/xml");
                if (parseTest.documentElement.nodeName != "parsererror" || parseTest.documentElement.namespaceURI != "http://www.mozilla.org/newlayout/xml/parsererror.xml")
                    type = "xs:anyType";
            }

        }
        if (type == "xs:string") {
            value = PolicyEditorService._encodeXML(value);
        }
        var starttag =   "<" + name +
                     (namespace == "" ? "" : " xmlns='" + namespace + "'") +
                     " xsi:type='" + type + "'" +
                     " xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
                     ">";
        var endtag = "</" + name + ">";
        return starttag + value + endtag;
    },

    _serializeXML : function(payload) {
        var browser = WSRequest.util._getBrowser();
        switch (browser) {
            case "gecko":
            case "safari":
                var serializer = new XMLSerializer();
                return serializer.serializeToString(payload);
                break;
            case "ie":
            case "ie7":
                return payload.xml;
                break;
            case "opera":
                var xmlSerializer = document.implementation.createLSSerializer();
                return xmlSerializer.writeToString(payload);
                break;
            case "undefined":
                throw new WebServiceError("Unknown browser", "WSRequest.util._serializeToString doesn't recognize the browser, to invoke browser-specific serialization code.");
        }
    },

    // library function for dynamically converting an element with js:type annotation to a Javascript type.
    _convertJSType : function (element, isWrapped) {
        if (element == null) return "";
        var extractedValue = WSRequest.util._stringValue(element);
        var resultValue, i;
        var type = element.getAttribute("js:type");
        if (type == null) {
            type = "#raw";
        } else {
            type = type.toString();
        }
        switch (type) {
            case "string":
                return extractedValue;
                break;
            case "number":
                return parseFloat(extractedValue);
                break;
            case "boolean":
                return extractedValue == "true" || extractedValue == "1";
                break;
            case "date":
                return WebService.utils.parseXSdateTime(extractedValue);
                break;
            case "array":
                resultValue = new Array();
                for (i=0; i<element.childNodes.length; i++) {
                    resultValue = resultValue.concat(WebService.utils._convertJSType(element.childNodes[i]));
                }
                return(resultValue);
                break;
            case "object":
                resultValue = new Object();
                for (i=0; i<element.childNodes.length; i++) {
                    resultValue[element.childNodes[i].tagName] = WebService.utils._convertJSType(element.childNodes[i]);
                }
                return(resultValue);
                break;
            case "xmlList":
                return element.childNodes;
                break;
            case "xml":
                return element.firstChild;
                break;
            case "#raw":
            default:
                if (isWrapped == true)
                    return element.firstChild;
                else return element;
                break;
        }
    }

};

