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

var lastUsedServiceId = "";
var lastUsedServiceVersion = "";

/**
 * Retrieves a Policy using a given URL
 *
 * @param url
 */
function getPolicyDoc(url) {

    var policyDoc = removeCDATA(PolicyEditorService.getPolicyDoc(url));
    syncRawPolicyView(policyDoc);
    buildTreeView(policyDoc);
}

/**
 * Retrieves a list of available Policy Schemas (XSDs) from the backend
 */
function getPolicSchemaDefs() {

    var schemaFilesList = removeCDATA(PolicyEditorService.getAvailableSchemas());

    var domParser = new DOMImplementation();
    var fileListXML = domParser.loadXML(schemaFilesList);

    var fileList = fileListXML.getElementsByTagName("file");
    for (var x = 0; x < fileList.length; x++) {
        // Retrieving policy XSDs
        var schemaDoc = removeCDATA(PolicyEditorService.getSchema(fileList.item(x).firstChild.nodeValue.toString()));
        storeSchema(schemaDoc);

    }

    buildPolicyMenu();
}


/**
 * Utility function to remove CDATA tags from a response
 * 
 * @param candidateString
 */
function removeCDATA(candidateString)
{
    //Verify whether this is a CDATA string
    if (candidateString.substring(0, 9) == "<![CDATA[") {
        //Removing <![CDATA[
        candidateString = candidateString.substring(9, candidateString.length);
        //Removing ]]>
        candidateString = candidateString.substring(0, candidateString.length - 3);
    } else if (candidateString.substring(0, 12) == "&lt;![CDATA[") {
        //Removing &lt;![CDATA[
        candidateString = candidateString.substring(12, candidateString.length);
        //Removing ]]&lt;
        candidateString = candidateString.substring(0, candidateString.length - 6);
    }

    return candidateString;
}

/**
 * Synchronizes the Raw Policy (Text) view using the changes done to the in-memory policy document
 * 
 * @param policyDocument
 */
function syncRawPolicyView(policyDocument) {

    var rawPolicyTextArea = document.getElementById("raw-policy");

    if (policyDocument.indexOf("?>") > -1) {
        policyDocument = policyDocument.substring(policyDocument.indexOf("?>") + 2);
    }

    try {
        var parser = new DOMImplementation();
        currentPolicyDoc = parser.loadXML(policyDocument);

        var browser = WSRequest.util._getBrowser();
        if (browser == "gecko") {
            // Gecko has inbuilt E4X. This formats XML nicely
            editAreaLoader.setValue("raw-policy","" + new XML(policyDocument));
        } else {
            // There's no known way to format in the client side. Sending to the backend
            formatXMLUsingService(policyDocument);
        }

        if (editAreaLoader.getValue("raw-policy") == "") {
            editAreaLoader.setValue("raw-policy",'<wsp:Policy xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy" />');
        }
    } catch(e) {
        //alert("Failed to parse the policy XML. Please check. [" + e.toString() + "]");
        editAreaLoader.setValue("raw-policy","" + policyDocument);
    }
}

/**
 * Formats a given XML using the backend service
 * 
 * @param xml
 */
function formatXMLUsingService(xml) {
    editAreaLoader.setValue("raw-policy",removeCDATA(PolicyEditorService.formatXML(xml)));
}

/**
 * Synchronizes the Policy Tree using the in-memory Policy document
 * 
 */
function syncPolicyTreeView() {
    var rawPolicy = editAreaLoader.getValue("raw-policy");

    if (rawPolicy.indexOf("?>") > -1) {
        rawPolicy = rawPolicy.substring(rawPolicy.indexOf("?>") + 2);
    }

    buildTreeView(rawPolicy);
}

/**
 * Generates HTML UI elements required to gather input from the user in order to add/update
 * an element.
 * 
 * @param targetElement - To which elements the updations should be commited to
 * @param schemaElement - The remplate schema element to use to generate the UI
 * @param namespaceURI - The namespace URI to use for the new element
 * @param mode - Mode can be either 'add' or 'edit'
 */
function generateGathererUI(targetElement, schemaElement, namespaceURI, mode) {
    // Store the target element globally
    currentUITargetElement = targetElement;

    var actionName = "";
    if (mode == "add") {
        actionName = jsi18n["adding.new.element"] + " ";
    } else if (mode == "edit") {
        actionName = jsi18n["editing.element"] + " ";
    }

    var prefix = schemaElement.prefix;

    // Start processing the attributes
    var elementAttributes = schemaElement.getElementsByTagName(prefix + ":attribute");

    if (elementAttributes.length > 0) {

        var uiHTML = "<div id='element-attribs'><table><tr><th>" + actionName +
                     schemaElement.getAttribute("name") +
                     "</th></tr>";

        for (var x = 0; x < elementAttributes.length; x++) {

            var attrbuteName = elementAttributes.item(x).getAttribute("name");
            if (attrbuteName != undefined) {
                uiHTML = uiHTML + "<tr><td>" + attrbuteName + "</td>";

                var attributeType = elementAttributes.item(x).getAttribute("type");
                if ((attributeType == prefix + ":anyURI") || (attributeType == prefix + ":float") ||
                    (attributeType == prefix + ":decimal") ||
                    (attributeType == prefix + ":double") ||
                    (attributeType == prefix + ":QName") ||
                    (attributeType == prefix + ":base64Binary") ||
                    (attributeType == prefix + ":integer")) {

                    // decide what to put as the default value
                    var defaultVal = "";
                    if (mode == "add") {
                        defaultVal = elementAttributes.item(x).getAttribute("default")
                        if (defaultVal == undefined) {
                            defaultVal = attributeType;
                        }
                    } else if (mode == "edit") {
                        // In this case, the default value should be whatever is already there
                        try {
                            defaultVal =
                            targetElement.getAttributes().getNamedItem(attrbuteName).getNodeValue();
                        } catch(ex) {
                        }
                    }
                    // Display a Text Box to collect data
                    uiHTML =
                    uiHTML + "<td><input id = '" + attrbuteName + "' type='text' value='" +
                    defaultVal +
                    "'/></td></tr>";
                } else if (attributeType == prefix + ":date") {
                    // This is a date type. The XML date format is "YYYY-MM-DD"
                    //todo: Implement with a date-picker component
                }
            }
        }

        // Add the button panel
        uiHTML = uiHTML +
                 "</table></div><div id='button-panel'>";

        if (mode == "add") {
            uiHTML = uiHTML +
                     "<input id='cmdAddElement' type='button' value='" +
                     jsi18n["add.element.to.document"] + "' onclick='createElementFromUIData(\"" +
                     schemaElement.getAttribute("name") + "\",\"" +
                     namespaceURI + "\");'/>";
        } else if (mode == "edit") {
            uiHTML = uiHTML +
                     "<input id='cmdEditElement' type='button' value='" + jsi18n["update.element"] +
                     "' onclick='updateElementFromUIdata();'/>";
        }

        uiHTML = uiHTML + "</div>";

        document.getElementById("divPolicyInputGatherer").innerHTML = uiHTML;

    } else {
        document.getElementById("divPolicyInputGatherer").innerHTML =
        jsi18n["the.element"] + " '" + schemaElement.getAttribute("name") +
        "' " + jsi18n["does.not.have.editable.attributes"];
    }
}

/**
 * Updates the current UI target element with inputs from the UI 
 */
function updateElementFromUIdata() {
    // Collect inputs from UI
    var inputTags = document.getElementById("element-attribs").getElementsByTagName("input");

    // Update the target element
    for (var x = 0; x < inputTags.length; x++) {
        var attributeName = inputTags[x].id;
        var attributeValue = inputTags[x].value;
        // Store the attribute in element
        currentUITargetElement.setAttribute(attributeName, attributeValue);
    }

    // Refresh and sync
    syncRawPolicyView(currentPolicyDoc.toString());
    buildTreeView(currentPolicyDoc.toString());

    CARBON.showInfoDialog(jsi18n["element.updated"]);
}

/**
 * Creates a document element from the data available in the UI
 *
 * @param elementName - The tag name of the new element
 * @param namespaceURI - Namespace URI to use
 */
function createElementFromUIData(elementName, namespaceURI) {
    var newXMLElement;

    var inputTags = document.getElementById("element-attribs").getElementsByTagName("input");

    // Create  the element
    if (namespaceURI != "") {
        var prefix = namespaceMap[namespaceURI];

        if (prefix == undefined) {
            // We need to define a prefix for this URI
            prefix = "poled" + Math.floor(Math.random() * 10001);

            newXMLElement =
            currentPolicyDoc.createElement(prefix + ":" + elementName);
            newXMLElement.setAttribute("xmlns:" + prefix, namespaceURI);

                    // Add the new URI to map
            namespaceMap[namespaceURI] = prefix;
        } else {
            newXMLElement =
            currentPolicyDoc.createElement(prefix + ":" + elementName);
        }
    } else {
        newXMLElement =
        currentPolicyDoc.createElement(elementName);
    }

    for (var x = 0; x < inputTags.length; x++) {
        var attributeName = inputTags[x].id;
        var attributeValue = inputTags[x].value;
        // Store the attribute in element
        newXMLElement.setAttribute(attributeName, attributeValue);
    }

    // Append the new element to the document
    currentUITargetElement.appendChild(newXMLElement);

    // Refresh and sync
    syncRawPolicyView(currentPolicyDoc.toString());
    buildTreeView(currentPolicyDoc.toString());

    // Clear UI components
    document.getElementById("divPolicyInputGatherer").innerHTML = "";
}


/**
 * Loads the schema fragment for a named element
 * 
 * @param elementName
 */
function getSchemaForElement(elementName) {
    // searching the element array
    for (var x = 0; x < elements.length; x++) {
        if (elements[x].schemaElement.getAttribute("name") == elementName) {
            return elements[x].schemaElement;
        }
    }

    // searching the attributes array
    for (x = 0; x < attributes.length; x++) {
        if (attributes[x].schemaElement.getAttribute("name") == elementName) {
            return attributes[x].schemaElement;
        }
    }

    return null;
}

/**
 * Saves the modified Policy Document using the backend service.
 *  
 */
var xt="",h3OK=1;
function checkErrorXML(x) {
    xt = ""
    h3OK = 1
    checkXML(x)
}

function checkXML(n)
{
    var l,i,nam
    nam = n.nodeName
    if (nam == "h3")
    {
        if (h3OK == 0)
        {
            return;
        }
        h3OK = 0
    }
    if (nam == "#text")
    {
        xt = xt + n.nodeValue + "\n"
    }
    l = n.childNodes.length
    for (i = 0; i < l; i++)
    {
        checkXML(n.childNodes[i])
    }
}
function validateXML(txt)
{
    // code for IE
    var error = "";
    if (window.ActiveXObject)
    {
        var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = "false";
        xmlDoc.loadXML(txt);

        if (xmlDoc.parseError.errorCode != 0)
        {
            txt = "Error Code: " + xmlDoc.parseError.errorCode + "\n";
            txt = txt + "Error Reason: " + xmlDoc.parseError.reason;
            txt = txt + "Error Line: " + xmlDoc.parseError.line;
            error = txt;
        }
    }
    // code for Mozilla, Firefox, Opera, etc.
    else if (document.implementation.createDocument)
    {
        var parser = new DOMParser();
        var text = txt;
        var xmlDoc = parser.parseFromString(text, "text/xml");

        if (xmlDoc.getElementsByTagName("parsererror").length > 0)
        {
            checkErrorXML(xmlDoc.getElementsByTagName("parsererror")[0]);
            error = xt;
        }

    }
    return error;

}
function savePolicyXML() {
    // Ensure the in memory policy is in sync with the UI
    var rawPolicy = editAreaLoader.getValue("raw-policy");
    if (rawPolicy.indexOf("?>") > -1) {
        rawPolicy = rawPolicy.substring(rawPolicy.indexOf("?>") + 2);
    }
    var error = validateXML(rawPolicy);
    if(error!=""){
        CARBON.showErrorDialog(error);
        return;
    }
    
    var domParser = new DOMImplementation();
    currentPolicyDoc = domParser.loadXML(rawPolicy);
    

    // check whether the root policy was removed
    if (currentPolicyDoc.getXML() == "") {
        CARBON.showErrorDialog(jsi18n["policy.content.blank"]);
    } else {

        if (currentPolicyURL != "null") {
            var body_xml = '<ns1:savePolicyXMLRequest xmlns:ns1="http://org.wso2.wsf/tools">' +
                           '<ns1:url>' + currentPolicyURL + '</ns1:url>' +
                           '<ns1:policy>' + currentPolicyDoc.toString() + '</ns1:policy>' +
                           '</ns1:savePolicyXMLRequest>';

            var callURL = serviceBaseURL + "PolicyEditorService";

            new wso2.wsf.WSRequest(callURL, "savePolicyXML", body_xml, savePolicyXMLCallback);
        } else {
            postbackUpdatedPolicy();
        }
    }
}

function savePolicyXMLCallback() {

}

/**
 * POSTs an updated Policy document to a given URL via a dynamically generated Form.
 *  
 */
function postbackUpdatedPolicy() {
    var formEl = document.getElementById("post-back-form");
    var formContentHTML = formEl.innerHTML;

    formEl.innerHTML =
        formContentHTML + '<input type="hidden" name="policy" id="policy-content"/>'
        + '<input type="hidden" name="policyid" value="' + policyId + '"/>';

    YAHOO.util.Event.onDOMReady(function() {
        document.getElementById("policy-content").value = btoa(currentPolicyDoc.toString());
        document.postbackForm.submit();
    });

}

/**
 * Allows the user to return to the page prior to the Policy Editor page.
 *  
 */
function goBack() {
    var redirectURL = document.getElementById("post-back-form").getAttribute("action");

    cleanBreadCrumb();
    
    // Redirecting to the url
    location.href = redirectURL;
}

/**
 * Utility function to create a Cookie
 * 
 * @param name
 * @param value
 * @param days - How many days before the cookie expires
 */
function createCookie(name, value, days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    }
    else var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
}

/**
 * Utility function to read a cookie value
 * 
 * @param name
 */
function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

/**
 * The breadcome behaves weirdly by showing the proxy page. This function removes that by
 * manipulating the breadcrumb cookie.
 *  
 */
function cleanBreadCrumb() {
    // Read the existing breadcrumb value
    var breadCrumb = readCookie("current-breadcrumb");

    // Set the new value after removing policy pages
    var newBreadCrumb = breadCrumb.split("*")[0];

    // Workaround for module management page
    var parts = newBreadCrumb.split("%23");
    if (parts.length > 1) {
        if (parts[1].indexOf("policy_editor_proxy") > -1) {
            newBreadCrumb = parts[0] + "%23";
        }
    }

    createCookie("current-breadcrumb", newBreadCrumb);
}

/**
 * Sometimes the ending breadcrumb link is activated, which is unorthodox.
 * Killing the last link to prevent it.
 * 
 */
function disableLastBreadcrumbLink() {

    var breadCrumbLinks = document.getElementById("breadcrumb-div").getElementsByTagName("a");
    for (var x = 0; x < breadCrumbLinks.length; x++) {
        var currentLinkText = breadCrumbLinks[x].firstChild.nodeValue;
        if (currentLinkText == "Policy") {
            breadCrumbLinks[x].href = "#";
        }
    }
}
/* javascript prety printing */
function format_xml(str)
{
	var xml = '';

	// add newlines
	str = str.replace(/(>)(<)(\/*)/g,"$1\r$2$3");

	// add indents
	var pad = 0;
	var indent;
	var node;

	// split the string
	var strArr = str.split("\r");

	// check the various tag states
	for (var i = 0; i < strArr.length; i++) {
		indent = 0;
		node = strArr[i];

		if(node.match(/.+<\/\w[^>]*>$/)){ //open and closing in the same line
			indent = 0;
		} else if(node.match(/^<\/\w/)){ // closing tag
			if (pad > 0){pad -= 1;}
		} else if (node.match(/^<\w[^>]*[^\/]>.*$/)){ //opening tag
			indent = 1;
		} else
			indent = 0;
		//}

		xml += spaces(pad) + node + "\r";
		pad += indent;
	}
    xml = xml.replace(/(&gt;)/g, ">");
    xml = xml.replace(/(&lt;)/g, "<");
	return xml;
}