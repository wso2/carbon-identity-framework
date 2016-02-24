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

var xmlTextArray;
var xmlCDataArray;
var xmlAttrArray;
var xmlPathArray;
var xmlCMENTArray;

// create tree root node
var policyTree;

// Curent node to which the elements are added while parsing
var currentNode;

var textNodeMap = {};

var currentPolicyDoc;

var namespaceMap = {}; // Holds all the namespace URIs mapped to prefixes found on this document

function buildTreeView(policyXML) {

    try {
        namespaceMap = {};
        var domParser = new DOMImplementation();

        // First storing the policyXML globally
        currentPolicyDoc = domParser.loadXML(policyXML);

        var parser = new SAXDriver();

        // pass handlers to the sax2 parser
        var handler = new xmlHandler();
        parser.setDocumentHandler(handler);
        parser.setErrorHandler(handler);
        parser.setLexicalHandler(handler);

        // Creating the tree
        policyTree = new YAHOO.widget.TreeView("divPolicyDocTree");

        // Subscribing to the click event
        policyTree.subscribe("labelClick", function(node) {
            currentTextNode = node;
            editPolicyElement();
        });

        // start parsing
        parser.parse(policyXML);

        // get errors from sax2 parser
        var err = handler.getError();
        if (!err) {
            // stores node element info in arrays
            xmlTextArray = handler.getText_Array();
            xmlCDataArray = handler.getCDATA_Array();
            xmlAttrArray = handler.getAttr_Array();
            xmlPathArray = handler.getPath_Array();
            xmlCMENTArray = handler.getCMENT_Array();
        } else {
            // alert(err);
            CARBON.showErrorDialog(e);
        }
    } catch(e) {
        // alert(domParser.translateErrCode(e.code));          
    }
}

function editPolicyElement() {
    // Extract the element from the document
    var selectedElement = isolateTargetElement(currentPolicyDoc.getElementsByTagName(currentTextNode.label), currentTextNode.xpath);

    // find the template schema element matching this element from elements and attributes arrays
    var elementLocalName = currentTextNode.label;
    if (elementLocalName.indexOf(":") > -1) {
        elementLocalName = elementLocalName.substring(elementLocalName.indexOf(":") + 1);
    }

    var correspondingSchemaElement = getSchemaForElement(elementLocalName);
    if (correspondingSchemaElement == null) {
        // Not supported
        document.getElementById("divPolicyInputGatherer").innerHTML = jsi18n["sorry.editing.not.supported"];        
    } else {

        // Find the namespace uri of the target element
        var elementNamespaceURI = selectedElement.getNamespaceURI();

        // Display the UI to gathe inputs
        generateGathererUI(selectedElement, correspondingSchemaElement, elementNamespaceURI, "edit");
    }
}

function showTagInfo(id) {
    var src = ''

    // get Text, Comment and CDATA information
    if (xmlTextArray[id]) {
        src += 'TEXT: \n' + (xmlTextArray[id] || '') + '\n';
    }

    if (xmlCMENTArray[id]) {
        src += 'COMMENT:\n ' + (xmlCMENTArray[id] || '') + '\n\n';
    }

    if (xmlCDataArray[id]) {
        src += 'CDATA:\n ' + (xmlCDataArray[id] || '') + '\n\n';
    }

    // get attribute info
    if (xmlAttrArray[id]) {
        var arr = xmlAttrArray[id];
        var atts = '';
        for (var i in arr) {
            // name = value
            atts += i + '=' + arr[i] + '\n';
        }
        src += 'ATTRIBUTES:\n ' + atts;
    }

    // display node info
    // alert('NODE: ' + xmlPathArray[id] + '\n\n' + src);
    CARBON.showErrorDialog('NODE: ' + xmlPathArray[id] + '\n\n' + src);

}

var xmlHandler = function() {
    this.m_strError = '';
    this.m_treeNodes = [];	 // stores nodes
    this.m_treePaths = []; // stores path info
    this.m_xPath = [''];	 // stores current path info
    this.m_text = [''];	 // stores node text info
    this.m_cdata = [''];	// stores node cdata info
    this.m_comment = [''];	// stores node comment info
    this.m_attr = [''];	// stores node attribute info
    this.m_pi = [''];		// store pi info - not used
    this.cdata = false;
    this.curpath = '';
    this.cnt = 0;

}

xmlHandler.prototype.characters = function(data, start, length) {
    // capture characters from CDATA and Text entities
    var text = data.substr(start, length);
    if (text == '\n') {
        return null // get ride of blank text lines
    }

    if (this.m_treeNodes[this.m_xPath.join('/')]) {
        if (!this.cdata) {
            if (!this.m_text[this.cnt]) {
                this.m_text[this.cnt] = '';
            }
            this.m_text[this.cnt] += text;
        }
        else {
            if (!this.m_cdata[this.cnt]) {
                this.m_cdata[this.cnt] = '';
            }
            this.m_cdata[this.cnt] += text;
        }
    }
}

xmlHandler.prototype.comment = function(data, start, length) {
    this.m_comment[this.cnt] = data.substr(start, length);
}


xmlHandler.prototype.endCDATA = function() {
    // end of CDATA entity
    this.cdata = false;

}

xmlHandler.prototype.endDocument = function() {
    // Draw the tree to canvas
    policyTree.draw();

    // Retrieve schemas from the back-end and build the policy menu
    getPolicSchemaDefs();

    // Expand the tree
    policyTree.expandAll();
}


xmlHandler.prototype.endElement = function(name) {
    this.m_xPath = this.m_xPath.slice(0, -1);
}

xmlHandler.prototype.error = function(exception) {
    this.m_strError += 'Error:' + exception.getMessage() + '\n';
}

xmlHandler.prototype.fatalError = function(exception) {
    this.m_strError += 'fata error:' + exception.getMessage() + '\n';
}

xmlHandler.prototype.getAttr_Array = function() {
    return this.m_attr;
}


xmlHandler.prototype.getCDATA_Array = function() {
    return this.m_cdata;
}


xmlHandler.prototype.getCMENT_Array = function() {
    return this.m_comment;
}

xmlHandler.prototype.getError = function() {
    return this.m_strError;
}

xmlHandler.prototype.getPath_Array = function() {
    return this.m_treePaths;
}


xmlHandler.prototype.getText_Array = function() {
    return this.m_text;
}

xmlHandler.prototype.processingInstruction = function(target, data) {

}


xmlHandler.prototype.setDocumentLocator = function(locator) {
    this.m_locator = locator;
}


xmlHandler.prototype.startCDATA = function() {
    this.cdata = true;
}

xmlHandler.prototype.startDocument = function() {

}

xmlHandler.prototype.startElement = function(name, atts) {
    // Note: the following code is used to store info about the node
    // into arrays for use in the tree node layout

    var ppath;
    var att_count = atts.getLength();
    var pnode;
    var node;

    // get previous path
    ppath = this.m_xPath.join('/');
    if (!ppath) ppath = "/";
    // get current path
    this.m_xPath[this.m_xPath.length] = name;
    this.curpath = this.m_xPath.join('/');

    this.cnt++;
    this.m_treePaths[this.cnt] = this.curpath;

    pnode = this.m_treeNodes[ppath];
    if (!pnode) {
        var root = policyTree.getRoot();
        var nodeObj = { label: name, href:"#" };
        pnode = new YAHOO.widget.TextNode(nodeObj, root, false);
        pnode.xpath = ppath;

        this.m_treeNodes[this.curpath] = pnode;

        // Adding this node to the map
        textNodeMap[pnode.labelElId] = pnode;

        if ((currentTextNode != undefined) && (pnode.xpath == currentTextNode.xpath)) {
            currentTextNode = node;
        }
    }
    else {
        nodeObj = { label: name, href:"#" };
        node = new YAHOO.widget.TextNode(nodeObj, pnode, false);
        node.xpath = this.curpath;

        this.m_treeNodes[this.curpath] = node;

        // Adding this node to the map
        textNodeMap[node.labelElId] = node;

        if ((currentTextNode != undefined) && (node.xpath == currentTextNode.xpath)) {
            currentTextNode = node;
        }
    }

    // get attributes
    if (att_count) {
        var attr = [];
        for (var i = 0; i < att_count; i++) {
            attr[atts.getName(i)] = atts.getValue(i);
            // Check attributes for namespaces
            processAttribute(atts.getName(i), atts.getValue(i));
        }
        this.m_attr[this.cnt] = attr;
    }


}

xmlHandler.prototype.warning = function(exception) {
    this.m_strError += 'Warning:' + exception.getMessage() + '\n';
}

function processAttribute(attName, value) {
    var idx = attName.indexOf("xmlns:");
    if (idx > -1) {
        // This is an xml namespace
        var prefix = attName.split(":")[1];
        namespaceMap[value.toString()] = prefix;
    }
}
