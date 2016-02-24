/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var elements = new Array(); // Stores all the document elements

var attributes = new Array(); // Stores all the document attributes

// Stores supported types from most commont simple to complex the UI will only support these types.
// A textbox will appear for unsupported types.
var types = new Array();

var currentTextNode; // Stores the currently selected tree node, who triggered the menu

// Holds the target element of the document with which the UI has to work with
var currentUITargetElement = "";

var schemaCollection = new Array(); // Temporariliy holds schemas

// Global variable which holds the context menu
var policyMenu = null;


function storeSchema(schemaDefXML) {
    schemaCollection[schemaCollection.length] = schemaDefXML;
}

function buildPolicyMenu() {

    if (policyMenu == null) {
        // Clear existing arrays
        elements = new Array();
        attributes = new Array();
        types = new Array();

    // Process all stored schemas
        for (var x = 0; x < schemaCollection.length; x++) {
            var schemaDefXML = schemaCollection[x]

        // Clearing unnecerssary stuff if present at the beginning
            if (schemaDefXML.indexOf("?>") > -1) {
                schemaDefXML = schemaDefXML.substring(schemaDefXML.indexOf("?>") + 2);
            }

        // instantiate the W3C DOM Parser
            var parser = new DOMImplementation();

        // load the XML into the parser and get the DOMDocument
            try {
                var domDoc = parser.loadXML(schemaDefXML);
                var docRoot = domDoc.getDocumentElement();
                var namespaceURI = getSchemaTargetNamespace(docRoot);
                var currentNode = docRoot.firstChild;

                while (currentNode) {
                    var elementLocalName = currentNode.localName;

                    if (elementLocalName != "") {
                        processNode(currentNode, namespaceURI)
                    }

                    currentNode = currentNode.nextSibling;
                }
            } catch(e) {
                // alert(parser.translateErrCode(e.code));                
            }

        }

    // Render the menu with new data
        renderPolicyMenu();
    }
}

function getSchemaTargetNamespace(documentRoot) {
    return documentRoot.getAttributes().getNamedItem("targetNamespace").getNodeValue().toString();
}

function processNode(node, namespaceURI) {
    var nodeName = node.localName;

    var menuElement = {name: nodeName, namespace: namespaceURI, schemaElement: node};

    if (nodeName == "element") {
        elements[elements.length] = menuElement;
    } else if (nodeName == "attribute") {
        attributes[attributes.length] = menuElement;
    } else if (nodeName == "complexType") {
        types[types.length] = menuElement;
    }
}

function renderPolicyMenu() {

    var menuRootItems = new Array();
    var submenuElements = new Array();
    var submenuAttributes = new Array();

    // Process Elements
    for (var x = 0; x < elements.length; x++) {
        // creating the JSON object for this menu item
        var elMenuItem = {
            text: '\"' + elements[x].schemaElement.getAttribute("name") + '\"',
            onclick: { fn: renderInputGatherer, obj: [elements[x].schemaElement, elements[x].namespace] }
        };
        submenuElements[submenuElements.length] = elMenuItem;
    }
    //Creating the Elements sub-menu
    var elSubMenu = {
        text: jsi18n["add.policy.element"],
        url: "#add-element",
        submenu: {
            id: "elements",
            itemdata: submenuElements
        }
    };
    // adding to root menu
    menuRootItems[menuRootItems.length] = elSubMenu;

    // Process Attributes
    for (x = 0; x < attributes.length; x++) {
        // creating the JSON object for this menu item
        var elAttributeItem = {
            text: '\"' + attributes[x].schemaElement.getAttribute("name") + '\"',
            onclick: { fn: renderInputGatherer, obj: [attributes[x].schemaElement, attributes[x].namespace ] }
        }
        submenuAttributes[submenuAttributes.length] = elAttributeItem;
    }
    //Creating the Elements sub-menu
    var atSubMenu = {
        text: jsi18n["add.policy.attribute"],
        url: "#add-attribute",
        submenu: {
            id: "attributes",
            itemdata: submenuAttributes
        }
    };
    // adding to root menu
    menuRootItems[menuRootItems.length] = atSubMenu;

    // Adding the delete menu option
    var deleteNode = {
        text: jsi18n["delete.this.element"],
        onclick: { fn: deletePolicyElement }
    };
    menuRootItems[menuRootItems.length] = deleteNode;

    policyMenu = new YAHOO.widget.ContextMenu("policy-menu", {
        trigger: "divPolicyDocTree",
        container: "tab2",  // This property has to be set to a parent div, other than the treeview container div for IE to render properly
        itemdata: menuRootItems,
        lazyload: true
    });

    /*
      Subscribe to the "contextmenu" event for the element(s)
      specified as the "trigger" for the ContextMenu instance.
    */
    policyMenu.subscribe("triggerContextMenu", onTriggerContextMenu);
}

/*
    "contextmenu" event handler for the element(s) that
    triggered the display of the ContextMenu instance - used
    to set a reference to the TextNode instance that triggered
    the display of the ContextMenu instance.
*/
function onTriggerContextMenu(p_oEvent) {

    var oTarget = this.contextEventTarget,
            Dom = YAHOO.util.Dom;

    /*
         Get the TextNode instance that that triggered the
         display of the ContextMenu instance.
    */
    var oTextNode = Dom.hasClass(oTarget, "ygtvlabel") ?
                    oTarget : Dom.getAncestorByClassName(oTarget, "ygtvlabel");

    if (oTextNode) {
        currentTextNode = textNodeMap[oTarget.id];
    }
    else {
        // Cancel the display of the ContextMenu instance.
        this.cancel();
    }
}

function deletePolicyElement(p_sType, p_aArgs, p_oValue) {

    //todo add verification before deleting elements

    var docRoot = currentPolicyDoc.getDocumentElement();
    var results = docRoot.getElementsByTagName(currentTextNode.label);

    var targetElement = results.item(0);
    if (results.length > 1) {
        // If there are more than one element, we need to figure out which one of these elements
        // is the correct element using XPath
        targetElement = isolateTargetElement(results, currentTextNode.xpath);
    }

    if (targetElement != null) {
        targetElement.getParentNode().removeChild(targetElement);
        syncRawPolicyView(currentPolicyDoc.toString())
        buildTreeView(currentPolicyDoc.toString());
    }

}

function renderInputGatherer(p_sType, p_aArgs, p_oValue) {
    // Clear existing ui elements
    document.getElementById("divPolicyInputGatherer").innerHTML = "";

    var schemaElement = p_oValue[0];
    var namespaceURI = p_oValue[1];

    var newXMLElement;

    var docRoot = currentPolicyDoc.getDocumentElement();
    var results = docRoot.getElementsByTagName(currentTextNode.label);

    var targetElement = results.item(0);
    if (results.length > 1) {
        // If there are more than one element, we need to figure out which one of these elements
        // is the correct element using XPath
        targetElement = isolateTargetElement(results, currentTextNode.xpath);
    }

    if (targetElement != null) {

        if (schemaElement.getElementsByTagName(schemaElement.prefix + ":attribute").length == 0) {
            // This is just a wrapper element no input from the user is required
            if (namespaceURI != "") {
                var prefix = namespaceMap[namespaceURI];

                if (prefix == undefined) {
                    // We need to define a prefix for this URI
                    prefix = "poled" + Math.floor(Math.random() * 10001);

                    newXMLElement =
                    currentPolicyDoc.createElement(prefix + ":" +
                                                   schemaElement.getAttribute("name"));
                    newXMLElement.setAttribute("xmlns:" + prefix, namespaceURI);

                    // Add the new URI to map
                    namespaceMap[namespaceURI] = prefix;
                } else {
                    newXMLElement =
                    currentPolicyDoc.createElement(prefix + ":" +
                                                   schemaElement.getAttribute("name"));
                }
            } else {
                newXMLElement =
                currentPolicyDoc.createElement(schemaElement.getAttribute("name"));
            }

            targetElement.appendChild(newXMLElement);
            syncRawPolicyView(currentPolicyDoc.toString())
            buildTreeView(currentPolicyDoc.toString());
        } else {
            // This needs a ui to get input. Calling the big guns
            generateGathererUI(targetElement, schemaElement, namespaceURI, "add");
        }
    }
}


function isolateTargetElement(elementsList, targetXPath) {
    for (var x = 0; x < elementsList.length; x++) {
        if (getElementXPath(elementsList.item(x)) == targetXPath) {
            return elementsList.item(x);
        }
    }

    return null;
}

function getElementXPath(elt)
{
    var path = "";
    for (; elt && elt.nodeType == 1; elt = elt.parentNode)
    {
        var idx = getElementIdx(elt);
        var xname = elt.tagName;
        if (idx > 1) xname += "[" + idx + "]";
        path = "/" + xname + path;
    }

    return path;
}

function getElementIdx(elt)
{
    var count = 1;
    for (var sib = elt.previousSibling; sib; sib = sib.previousSibling)
    {
        if (sib.nodeType == 1 && sib.tagName == elt.tagName)     count++
    }

    return count;
}

