<%--
  Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

   WSO2 Inc. licenses this file to you under the Apache License,
   Version 2.0 (the "License"); you may not use this file except
   in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
  --%>

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.owasp.encoder.Encode"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode" %>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="javax.servlet.jsp.JspWriter"%>
<%@page import="java.io.IOException"%>
<%@page import="java.net.URLDecoder" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.ResourceBundle" %>
<jsp:useBean id="roleBean" type="org.wso2.carbon.user.mgt.ui.RoleBean"
                    class="org.wso2.carbon.user.mgt.ui.RoleBean" scope="session"/>
<jsp:setProperty name="roleBean" property="*" />

<link rel="stylesheet" type="text/css" href="../yui/build/treeview/assets/skins/sam/treeview.css" />
<script type="text/javascript" src="js/yuiloader-min.js"></script>
<script type="text/javascript" src="js/event-min.js"></script>
<script type="text/javascript" src="js/dom-min.js"></script>
<script type="text/javascript" src="js/logger-min.js"></script>
<!--script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/treeview/treeview-min.js"></script> this does not work. Only the debug version works -->
<script type="text/javascript" src="js/treeview-debug.js"></script>
<script type="text/javascript" src="js/element-min.js"></script>
<style type="text/css">
.ygtvcheck0 { background: url(images/check0.gif) 0 0 no-repeat; width:16px; cursor:pointer }
.ygtvcheck1 { background: url(images/check1.gif) 0 0 no-repeat; width:16px; cursor:pointer }
.ygtvcheck2 { background: url(images/check2.gif) 0 0 no-repeat; width:16px; cursor:pointer }
.ygtvlabel{
    color:#477ea5;
}
#expandcontractdiv{
    background-color:#ededed;
    padding:5px;
}
</style>

    <%! 
		private void printNodesOfTree(UIPermissionNode parentNode, String parentNodeName, int count,
                                      JspWriter out, String urlData) throws IOException{

            if(parentNode == null){
                return;
            }
	        try{
			    UIPermissionNode[] children = parentNode.getNodeList();
			    String displayName = parentNode.getDisplayName();
			    String path = parentNode.getResourcePath();
			    String thisNodeName = "tempNode" + count;

			    if(urlData != null && !urlData.equals("")){ // If comming back from step 3
			    	String[] data = URLDecoder.decode(urlData).replaceAll("selectedPermissions=", "").split("&");
			    	ArrayList<String> selectedNodes = new ArrayList(Arrays.asList(data));
			    	boolean wasSelected = selectedNodes.contains(path);
			    	
			    	out.write("var " + thisNodeName + " = new YAHOO.widget.TaskNode({label:\""
					           + displayName + "\",labelData:\"" + path + "\"}, " + parentNodeName + ", true, "
					           + wasSelected + ");");  	
			    	
			    } else {
			    
			    	out.write("var " + thisNodeName + " = new YAHOO.widget.TaskNode({label:\""
			            + displayName + "\",labelData:\"" + path + "\"}, " + parentNodeName + ", true, "
			            + parentNode.getSelected() + ");");
			    }

                if(children != null){
                    for (UIPermissionNode child : children) {
                        count++;
                        printNodesOfTree(child, thisNodeName, count, out, urlData);

                    }
                }
	        } catch(IOException e){
	            throw e;
	        }
		}
    %>

    <%
        String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        String urlData = request.getQueryString(); // to be used when comming back from step3
    	UIPermissionNode rootNode = null;
        try {                                                         
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            //Checking whether the user provided role name exists in the system.
            rootNode = client.getAllUIPermissions();
        } catch (Exception e) {
            String message = MessageFormat.format(resourceBundle.getString("error.while.loading.ui.permission"),
                    e.getMessage());
    %>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>',  function () {
            location.href = "add-step1.jsp";
        });
    });
</script>
<%
    }
%>

    <script type="text/javascript">
	    function doValidation() {
	        var reason = "";
	
	    //    isChecked = isAtleastOneCheckedIfExisting("selectedPermissions");
	    //    if (isChecked != true) {
	    //        CARBON.showWarningDialog("<fmt:message key="please.select.at.least.one.permission"/>");
	    //        return false;
	    //    }
	        return true;
	    }
	
	    function doCancel() {
	        location.href = 'role-mgt.jsp?ordinal=1';
	    }
	
	    function doNext() {
	        document.permissionForm.action="add-step3.jsp";
	        if(doValidation() == true){
	        	setFieldsOnSubmit();
	            document.permissionForm.submit();
	        }
	    }
	    
	    
	    function goBack() {
	        location.href = 'add-step1.jsp?ordinal=2';
	    }
    </script>
    
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="add.system.permissions"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

<div id="middle">
    <%if(UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleBean.getRoleType())){%>
        <h2><fmt:message key="add.internal.user.role"/></h2>
    <%} else { %>
        <h2><fmt:message key="add.user.role"/></h2>
    <%} %>

    <div id="workArea">
        <h3><fmt:message key="step.2.role"/></h3>

        <div class="yui-skin-sam">

            <!-- markup for expand/contract links -->
            <div id="expandcontractdiv" style="margin-bottom:10px;">
                    <a id="expand" href="#"><img src="images/expandall.gif" align="top" /> Expand all</a>
                    <a id="collapse" href="#" style="margin-left:20px"><img src="images/contractall.gif" align="top" /> Collapse all</a>
                </div>

            <div id="treeDiv1"></div>
        </div>
        <form name="permissionForm" method="post" action="add-finish.jsp" onsubmit="setFieldsOnSubmit()">
            <span id="permissions"></span>
            <table>
                <tr>
                    <td class="buttonRow">
                            <%--<input type="button" class="button" value="< <fmt:message key="back"/>" onclick="goBack();"/>--%>
                            <input type="button" class="button" value="<fmt:message key="next"/> >" onclick="doNext();"/>
                            <input type="submit" class="button" value="<fmt:message key="finish"/>" />
                            <input type="button" class="button" value="<fmt:message key="cancel"/>" onclick="doCancel();"/>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>


<script type="text/javascript" src="js/TaskNode.js"></script>


<script type="text/javascript">

	var tree;
	var nodes = [];
	var nodeIndex;
	
	function treeInit() {
		buildPermissionTaskNodeTree();
	}
	
	//handler for expanding all nodes
	YAHOO.util.Event.on("expand", "click", function(e) {
		tree.expandAll();
		YAHOO.util.Event.preventDefault(e);
	});
	
	//handler for collapsing all nodes
	YAHOO.util.Event.on("collapse", "click", function(e) {
		tree.collapseAll();
		YAHOO.util.Event.preventDefault(e);
	});

	//handler for checking all nodes
	YAHOO.util.Event.on("check", "click", function(e) {
		checkAll();
		YAHOO.util.Event.preventDefault(e);
	});
	
	//handler for unchecking all nodes
	YAHOO.util.Event.on("uncheck", "click", function(e) {
		uncheckAll();
		YAHOO.util.Event.preventDefault(e);
	});


	YAHOO.util.Event.on("getchecked", "click", function(e) {           
		YAHOO.util.Event.preventDefault(e);
	});


     function buildPermissionTaskNodeTree() {
	
		//instantiate the tree:
        tree = new YAHOO.widget.TreeView("treeDiv1");
	    tree.checked = true;


      <% printNodesOfTree(rootNode, "tree.getRoot()", 0, out, urlData); %>
	    
	   // Expand and collapse happen prior to the actual expand/collapse,
       // and can be used to cancel the operation
       tree.subscribe("expand", function(node) {// return false to cancel the expand
           });

       tree.subscribe("collapse", function(node) { });

       // Trees with TextNodes will fire an event for when the label is clicked:
       tree.subscribe("labelClick", function(node) { });

       // Trees with TaskNodes will fire an event for when a check box is clicked
       tree.subscribe("checkClick", function(node) { });

       tree.subscribe("clickEvent", function(node) { });

		//The tree is not created in the DOM until this method is called:
        tree.draw();
    }

    function checkAll() {
        var topNodes = tree.getRoot().children;
        for(var i=0; i<topNodes.length; ++i) {
            topNodes[i].check();
        }
    }

    function uncheckAll() {
        var topNodes = tree.getRoot().children;
        for(var i=0; i<topNodes.length; ++i) {
            topNodes[i].uncheck();
        }
    }

   // Gets the labels of all of the fully checked nodes
   // Could be updated to only return checked leaf nodes by evaluating
   // the children collection first.
    function getCheckedNodes(nodes) {
        nodes = nodes || tree.getRoot().children;
        checkedNodes = [];
        for(var i=0, l=nodes.length; i<l; i=i+1) {
            var n = nodes[i];
            //if (n.checkState > 0) { // if we were interested in the nodes that have some but not all children checked
            if (n.checkState === 2) {
                checkedNodes.push(n.data.labelData); // just using label for simplicity
            }

            if (n.hasChildren()) {
                checkedNodes = checkedNodes.concat(getCheckedNodes(n.children));
            }
        }

        return checkedNodes;
    }


	YAHOO.util.Event.onDOMReady(treeInit);

function setFieldsOnSubmit(){
    var checkedNodes = getCheckedNodes();
    for(var i=0, l=checkedNodes.length; i<l; i=i+1) {
        var n = checkedNodes[i];
        var element = document.createElement("input");
        element.setAttribute("type", "hidden");  
        element.setAttribute("name", "selectedPermissions");  
        element.setAttribute("value", n);
        var span = document.getElementById("permissions");
      	span.appendChild(element);
    }
    return true;
}

</script>
</fmt:bundle>

