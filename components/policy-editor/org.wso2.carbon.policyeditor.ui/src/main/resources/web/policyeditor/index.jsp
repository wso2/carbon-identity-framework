<%--
   ~ Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
   ~
   ~  WSO2 Inc. licenses this file to you under the Apache License,
   ~  Version 2.0 (the "License"); you may not use this file except
   ~  in compliance with the License.
   ~  You may obtain a copy of the License at
   ~
   ~     http://www.apache.org/licenses/LICENSE-2.0
   ~
   ~  Unless required by applicable law or agreed to in writing,
   ~  software distributed under the License is distributed on an
   ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   ~  KIND, either express or implied.  See the License for the
   ~  specific language governing permissions and limitations
   ~  under the License.
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%--<link rel="stylesheet" type="text/css" href="../admin/css/main.css"/>--%>
<link rel="stylesheet" type="text/css" href="css/local-styles.css"/>
<link rel="stylesheet" type="text/css" href="js/yui/menu/assets/skins/sam/menu.css"/>
<link rel="stylesheet" type="text/css" href="js/yui/treeview/assets/treeview.css"/>
<link rel="stylesheet" type="text/css"
      href="js/yui/tabview/assets/skins/policyeditor/tabview.css"/>
<link rel="stylesheet" type="text/css" href="js/yui/button/assets/skins/sam/button.css"/>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../admin/js/dhtmlHistory.js"></script>
<script type="text/javascript" src="../admin/js/WSRequest.js"></script>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>

<script type="text/javascript" src="js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="js/yui/element/element-beta-min.js"></script>
<script type="text/javascript" src="js/yui/container/container_core-min.js"></script>
<script type="text/javascript" src="js/yui/menu/menu-min.js"></script>
<script type="text/javascript" src="js/yui/treeview/treeview-min.js"></script>
<script type="text/javascript" src="js/yui/tabview/tabview-min.js"></script>
<script type="text/javascript" src="js/yui/button/button-beta-min.js"></script>

<script type="text/javascript" src="js/xml-for-script/tinyxmlsax.js"></script>
<script type="text/javascript" src="js/xml-for-script/tinyxmlw3cdom.js"></script>

<script type="text/javascript" src="js/sax-tree.js"></script>
<script type="text/javascript" src="js/sax-policy-menu.js"></script>
<script type="text/javascript" src="js/policy-editor-service-stub.js"></script>
<script type="text/javascript" src="js/policy-editor.js"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<fmt:bundle basename="org.wso2.carbon.policyeditor.ui.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.policyeditor.ui.i18n.JSResources" request="<%=request%>"/>
    <carbon:breadcrumb

            label="Policy"
		resourceBundle="org.wso2.carbon.policyeditor.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
<div id="middle">
<h2><fmt:message key="policy.editor"/></h2>
<br/>
<br/>


<div id="main-container" class="main-container">
<div class="yui-skin-sam">
    <div id="editor-canvas" class="yui-navset">
        <ul class="yui-nav">
            <li class="selected"><a href="#tab1"><em><fmt:message key="source.view"/></em></a></li>
            <li><a href="#tab2" onclick="syncPolicyTreeView();"><em><fmt:message key="design.view"/></em></a></li>
        </ul>
        <div class="yui-content" style="padding-bottom:20px;height:700px;">
            <div id="tab1">
            	<table style="width:100%" class="styledLeft" cellspacing="0" cellpadding="0">
            	<tbody>
            	<tr>
            	
                <td style="width:100%">
                    <textarea id="raw-policy" class="raw-policy-tree"></textarea>
                </td>
                </tr>
                <tr><td height="10px"></td></tr>
                <tr>
		        <td>
                	<input class="button" type="button" name="save-policy" value="<fmt:message key="save.policy"/>"
                                           onclick="savePolicyXML();">
                    <input class="button" type="button" name="go-back" value="<fmt:message key="go.back"/>"
                                           onclick="goBack();">
                </div>
                </td>
                </tr>
                </tbody>
                </table>
            </div>
            <div id="tab2">
            	<table style="width:100%" class="styledLeft" cellspacing="0" cellpadding="0">
            	<tbody>
            	<tr>
            	<td>            
                <div style="padding-bottom:0.5px;">
                  <em><i><fmt:message key="tip.for.more.operations.right.click.the.tree.nodes.in.the.panel.below"/></i></em>
                </div>
                </td>
                </tr>
                <tr>
                <td>
                    <div id="divPolicyDocTree" class="policy-tree">
                        <!-- Will contain a tree representation of the policy xml -->
                    </div>
                    <div class="policy-input-gatherer">
                        <div id="divPolicyInputGatherer">
                            <!-- Will contain a tree representation of the policy xml -->
                        </div>
                    </div>
                    </td>
                    </tr>
                    <tr><td height="40px"></td></tr>
                    <tr>
                    <td>
                        <input class="button" type="button" name="save-policy" value="<fmt:message key="save.policy"/>"
                                           onclick="savePolicyXML();">
                        <input class="button" type="button" name="go-back" value="<fmt:message key="go.back"/>"
                                           onclick="goBack();">
                    </td>
                    </tr>
                    </tbody>
                    </table>
            </div>
        </div>
    </div>

    <!-- This div will contain the hidden form, which will do the POST back when saving a Policy -->
    <div id="post-back-content" style="display: none;">
        <form name="postbackForm" id="post-back-form" action="../entitlement/update-policy-submit.jsp" method="post">
        </form>
    </div>
</div>

<%
    String policyURL = request.getParameter("url");

    String policyText = "";
    String policyId = "";

    if (policyURL == null) {
        if (request.getParameter("policy") != null) {
            policyText = request.getParameter("policy").replaceAll("\r\n", "")
                    .replaceAll("\n", "");
            policyText=policyText.replace("'", "\"");
            policyId = request.getParameter("policyid");
        }
    }
%>

<script type="text/javascript">
    wso2.wsf.Util.initURLs();
    wso2.wsf.XSLTHelper.init();
    window.dhtmlHistory.initialize();
    
    PolicyEditorService.setAddress("PolicyEditorServiceHttpsSoap12Endpoint",
            serverURL + "/PolicyEditorService.PolicyEditorServiceHttpsSoap12Endpoint/");

    // store the policy metadata to be used by javascript code

    // If the policy is to be loaded from a URL
    var currentPolicyURL = '<%=Encode.forJavaScriptBlock(policyURL)%>';

    // If the policy is posted to the editor with additional meta-data
    var policyText = '<%=Encode.forJavaScriptBlock(policyText)%>';
    var policyId = '<%=Encode.forJavaScriptBlock(policyId)%>';

    // Create design and source view tabs
    var tabView = new YAHOO.widget.TabView('editor-canvas');

    var serviceBaseURL = '../../services/';

    // Once the DOM has loaded, we can go ahead and set up the policy tree
    function beginPolicyRetrieval() {
        // Disabling the final breadcrumb link
        disableLastBreadcrumbLink();

        if (currentPolicyURL != "null") {
            getPolicyDoc('<%=Encode.forJavaScript(policyURL)%>');
        } else if (policyText != "") {
            // loading the Policy document to Raw View
            syncRawPolicyView(policyText);

            // Loading the Policy document to Tree View
            buildTreeView(policyText);
        }
    }

    // This is a workaround for IE to ensure 'back' button works as expected
    function handleUnload()
    {
        goBack();
    }
    window.onunload = handleUnload;

    // Trigger policy retrieval once the DOM is ready
    YAHOO.util.Event.onDOMReady(beginPolicyRetrieval);
</script>
</div>
</div>
<script>

var policyText = '<%=Encode.forJavaScriptBlock(policyText)%>';

jQuery.ajax(
    {
        data:"xmlString="+policyText,
        url:"prettyPrinter_ajaxprocessor.jsp",
        type: "POST",
        success:function(data){
        
            jQuery('#raw-policy').text(data);
            editAreaLoader.setValue('raw-policy',data);
            editAreaLoader.init({
                id : "raw-policy"		// textarea id
                ,syntax: "xml"			// syntax to be uses for highgliting
                ,start_highlight: true		// to display with highlight mode on start-up
            });
            editAreaLoader.setValue('raw-policy',data);
        }
    });
    

</script>
</fmt:bundle>