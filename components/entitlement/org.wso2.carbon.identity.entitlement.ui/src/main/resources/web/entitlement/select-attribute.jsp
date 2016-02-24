<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.EntitlementTreeNodeDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page
        import="java.io.IOException" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%!
    public void printChildrenTree(EntitlementTreeNodeDTO node, JspWriter out) throws IOException {
        if(node != null){
            EntitlementTreeNodeDTO[] children = node.getChildNodes();
            if(children != null  && children.length > 0){
                out.write("<li><a class='plus' onclick='treeColapse(this)'>&nbsp;</a> " +
                        "<a class='treeNode' onclick='selectMe(this)'>" + node.getName() + "</a>");
                out.write("<ul style='display:none'>");
                for(EntitlementTreeNodeDTO child : children){
                    printChildrenTree(child, out);
                }
                out.write("</ul>");
            } else {
                out.write("<li><a class='minus' onclick='treeColapse(this)'>&nbsp;</a> " +
                        "<a class='treeNode' onclick='selectMe(this)'>" + node.getName() + "</a>");
                out.write("</li>");
            }
        }
    }

    public void printChildren(EntitlementTreeNodeDTO node, String parentNodeName, JspWriter out) throws IOException {
        if(node != null){
            String nodeName;
            if(parentNodeName != null && parentNodeName.trim().length() > 0){
                nodeName = parentNodeName + "/" + node.getName();
            } else {
                nodeName = node.getName();
            }

            out.write("<li><a class='treeNode' onclick='selectMe(this)'>" + nodeName + "</a></li>") ;
            EntitlementTreeNodeDTO[] children = node.getChildNodes();
            if(children != null  && children.length > 0){
                for(EntitlementTreeNodeDTO child : children){
                    printChildren(child, nodeName, out);
                }
            }
        }
    }

%>

<%
    String forwardTo;
    EntitlementFinderDataHolder finderDataHolder = null;
    EntitlementTreeNodeDTO selectedTree = null;
    String selectedFinderModule;
    String category;
    boolean showNoData = false;
    String searchString = request.getParameter("searchString");
    int levels = 0;

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String ruleId = request.getParameter("ruleId");
    String returnPage = request.getParameter("returnPage");
    if(returnPage == null || returnPage.trim().length() == 0){
        returnPage = "policy-editor.jsp";
    }
    selectedFinderModule = request.getParameter("finderModule");
    if(selectedFinderModule == null || selectedFinderModule.trim().length() < 1){
        selectedFinderModule = EntitlementPolicyConstants.DEFAULT_META_DATA_MODULE_NAME;
    }

    category = request.getParameter("category");
    String selectedDataLevelString = request.getParameter("selectedDataLevel");
    int selectedDataLevel = 0;
    try{
        selectedDataLevel = Integer.parseInt(selectedDataLevelString);
    } catch (Exception e){
        //ignore
    }
    String selectedData = request.getParameter("selectedData" + selectedDataLevel);

    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                                getPolicyEditorData(EntitlementConstants.PolicyEditor.STANDARD);
    Set<String> attributeIds = holder.getAttributeIdMap().keySet();
    if(category != null && category.trim().length() > 0){
        attributeIds = holder.getCategoryAttributeIdMap().get(category);
    }
    Set<String> dataTypes = holder.getDataTypeMap().keySet();
    String selectedAttributeDataType = request.getParameter("selectedAttributeDataType");
    String selectedAttributeId = request.getParameter("selectedAttributeId");
    Set<EntitlementFinderDataHolder> holders = entitlementPolicyBean.getEntitlementFinders(category);
    try {
        EntitlementPolicyAdminServiceClient client =
                new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);

        if(selectedFinderModule != null && selectedFinderModule.trim().length() > 0){
            finderDataHolder = entitlementPolicyBean.getEntitlementFinders().get(selectedFinderModule);
            levels = finderDataHolder.getHierarchicalLevels();

            if(searchString != null && searchString.trim().length() > 0){
                selectedTree = client.getEntitlementData(selectedFinderModule,
                        category, searchString, 0, 100);
                if(selectedTree == null){
                    showNoData = true;
                }
            } else if(levels > 0 && selectedData != null && selectedData.trim().length() > 0 &&
                    selectedDataLevel + 1 != levels){
                EntitlementTreeNodeDTO nodeDTO = client.getEntitlementData(selectedFinderModule,
                        category, selectedData, selectedDataLevel + 1, 100);
                if(nodeDTO != null){
                    entitlementPolicyBean.getEntitlementLevelData().put(selectedDataLevel + 1, nodeDTO);
                }
                if(selectedData != null && selectedData.trim().length() > 0 ){
                    entitlementPolicyBean.getSelectedEntitlementData().put(selectedDataLevel, selectedData);
                }
            }
        } else {
            String message = resourceBundle.getString("no.entitlement.data.finder.defined");
%>
<script type="text/javascript">
    CARBON.showWarningDialog('<%=message%>', null, null);
</script>
<%
        }
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.retrieving.attribute.values");
%>
<script type="text/javascript">
    CARBON.showWarningDialog('<%=message%>', null, null);
</script>
<%
    }
%>

<%
    if(holders == null || holders.size() == 0){
        String message = resourceBundle.getString("no.entitlement.data.finder.defined");
%>
<script type="text/javascript">
    CARBON.showWarningDialog('<%=message%>', null, null);
</script>
<%
    }
%>

<%
    if(showNoData){
        String message = resourceBundle.getString("no.entitlement.data.defined");
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=message%>', null, null);
</script>  
<%  
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
        label="select.attribute.values"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>" />

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>


<!--Yahoo includes for dom event handling-->
<script src="http://yui.yahooapis.com/2.8.1/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--Yahoo includes for animations-->
<script src="http://yui.yahooapis.com/2.8.1/build/animation/animation-min.js" type="text/javascript"></script>

<!--Local js includes-->
<script type="text/javascript" src="js/treecontrol.js"></script>
<script type="text/javascript" src="js/popup.js"></script>

<link href="css/tree-styles.css" media="all" rel="stylesheet" />
<link href="css/dsxmleditor.css" media="all" rel="stylesheet" />
<script type="text/javascript">

    function getFinderModule() {
        preSubmit();
        document.attributeValueForm.action = "select-attribute.jsp";
        document.attributeValueForm.submit();
    <%--var comboBox = document.getElementById("finderModule");--%>
    <%--var finderModule = comboBox[comboBox.selectedIndex].value;--%>
    <%--location.href = 'select-attribute-values.jsp?finderModule=' + finderModule--%>
    <%--+ "&category=" + '<%=category%>' + "&ruleId=" + '<%=ruleId%>';--%>
    }

    function getNextData() {
        preSubmit();
        document.attributeValueForm.action = "select-attribute.jsp";
        document.attributeValueForm.submit();
    <%--var comboBox = document.getElementById("finderModule");--%>
    <%--var finderModule = comboBox[comboBox.selectedIndex].value;--%>
    <%--location.href = 'select-attribute-values.jsp?finderModule=' + finderModule--%>
    <%--+ "&category=" + '<%=category%>' + "&ruleId=" + '<%=ruleId%>';--%>
    }

    function doSearch() {
        preSubmit();
        document.attributeValueForm.action = "select-attribute.jsp";
        document.attributeValueForm.submit();
    <%--var comboBox = document.getElementById("finderModule");--%>
    <%--var finderModule = comboBox[comboBox.selectedIndex].value;--%>
    <%--location.href = 'select-attribute-values.jsp?finderModule=' + finderModule--%>
    <%--+ "&category=" + '<%=category%>' + "&ruleId=" + '<%=ruleId%>';--%>
    }

    function createInputs(value){
        var mainTable = document.getElementById('mainTable');
        var newTr = mainTable.insertRow(mainTable.rows.length);
        var cell1 = newTr.insertCell(0);
        cell1.innerHTML = '<input type="hidden" name="attributeValue'+ mainTable.rows.length
                +'" id="attributeValue'+ mainTable.rows.length +'" value="' + value + '"/>';
    }

    function submitForm(fullPathSupported){
        preSubmit();
        for(var i in paths){
            if(fullPathSupported){
                createInputs(paths[i].path);
            } else {
                createInputs(paths[i].name);
            }
        }
        document.attributeValueForm.action = "<%=Encode.forJavaScriptBlock(returnPage)%>.jsp?category="
                + '<%=Encode.forUriComponent(category)%>' +"&ruleId=" + '<%=Encode.forUriComponent(ruleId)%>' ;
        document.attributeValueForm.submit();
    }

    function doCancel(){
        preSubmit();
        document.attributeValueForm.action = "<%=Encode.forJavaScriptBlock(returnPage)%>.jsp?ruleId=" + '<%=Encode.forUriComponent(ruleId)%>';
        document.attributeValueForm.submit();
    }

    function preSubmit(){

        jQuery('#attributeValueTable > tbody:last').append('<tr><td><input type="hidden" name="category" id="category" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(category))%>" /><input type="hidden" name="ruleId" id="ruleId" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(ruleId))%>" /><input type="hidden" name="returnPage" id="returnPage" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(returnPage))%>" /></td></tr>') ;

    }

</script>

<div id="middle">
    <h2><fmt:message key="select.attribute.values"/></h2>
    <div id="workArea">
        <form id="attributeValueForm" name="attributeValueForm" method="post" action="basic-policy-editor.jsp">
            <table width="60%" id="attributeValueTable" class="styledLeft">
            <tbody>
                <tr>
                    <td class="formRaw">
                        <table class="normal" cellpadding="0" cellspacing="0" class="treeTable" style="width:100%">
                            <tr>
                                <td width="20%">
                                    <fmt:message key="select.attribute.id"/>
                                </td>

                                <td width="30%">
                                    <select id="selectedAttributeId" name="selectedAttributeId" class="text-box-big">
                                        <%
                                            for (String attributeId : attributeIds) {
                                                if(selectedAttributeId !=null && selectedAttributeId.equals(attributeId)){
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(attributeId)%>" selected="selected"><%=Encode.forHtmlContent(attributeId)%></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(attributeId)%>"><%=Encode.forHtmlContent(attributeId)%></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="select.attribute.dataType"/>
                                </td>

                                <td>
                                    <select type="hidden" id="selectedAttributeDataType" name="selectedAttributeDataType" class="text-box-big">
                                        <%
                                            for (String attributeDataType : dataTypes) {
                                                if(selectedAttributeDataType != null
                                                        && selectedAttributeDataType.equals(attributeDataType)){
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(attributeDataType)%>" selected="selected"><%=Encode.forHtmlContent(attributeDataType)%></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(attributeDataType)%>"><%=Encode.forHtmlContent(attributeDataType)%></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                            </tr>
            <%
                if(holders != null && holders.size() > 0){
            %>
                            <tr>
                                <td class="leftCel-med">
                                    <fmt:message key="attribute.finder.module"/>
                                </td>
                                <td>
                                    <select onchange="getFinderModule();" id="finderModule" name="finderModule" class="text-box-big">
                                        <%
                                            for (EntitlementFinderDataHolder entry : holders) {
                                                String moduleName = entry.getName();
                                                if(moduleName.equals(selectedFinderModule)){
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(moduleName)%>" selected="selected"><%=Encode.forHtmlContent(moduleName)%></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(moduleName)%>"><%=Encode.forHtmlContent(moduleName)%></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                            </tr>
                            <%
                                int i = 1;
                                while (true) {
                                    EntitlementTreeNodeDTO  nodeDTO =  entitlementPolicyBean.getEntitlementLevelData().get(i);
                                    if(nodeDTO == null){
                                        break;
                                    }
                            %>
                            <tr>
                                <td class="leftCel-med">
                                    <fmt:message key="select.attribute.data"/> <%=i%>
                                </td>
                                <td>
                                    <select onchange="getNextData();" id="selectedData<%=Encode.forHtmlAttribute(i)%>"
                                            name="selectedData<%=Encode.forHtmlAttribute(i)%>" class="text-box-big">
                                        <%

                                            EntitlementTreeNodeDTO[] childNodeDTOs = nodeDTO.getChildNodes();
                                            for (EntitlementTreeNodeDTO childNodeDTO : childNodeDTOs) {
                                                String name = childNodeDTO.getName();
                                                if(name.equals(entitlementPolicyBean.getSelectedEntitlementData().get(i))){
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(name)%>" selected="selected"><%=Encode.forHtmlContent(name)%></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(name)%>"><%=Encode.forHtmlContent(name)%></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                                <td>
                                    <input type="hidden" name="selectedDataLevel"
                                           id="selectedDataLevel" value="<%=i%>" />
                                </td>
                            </tr>
                            <%
                                    i++;
                                }
                            %>
                            <%
                                if(levels == 0 || selectedDataLevel + 1 == levels){
                            %>
                            <tr>
                                <td class="leftCel-med">
                                    <fmt:message key="enter.attribute.search.pattern"/>
                                </td>
                                <td>
                                    <input type="text" name="searchString" id="searchString"
                                           value="<%= searchString != null? Encode.forHtmlAttribute(searchString) :""%>"/>
                                </td>
                                <td style="border:0; !important">
                                    <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                                       onclick="doSearch(); return false;"
                                       alt="<fmt:message key="search"/>"></a>
                                </td>
                            </tr>
                            <%
                                }
                            %>
                            <tr>
                                <td>
                                    <table id="mainTable" class="styledLeft noBorders" style="display:none">
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3">
                                    <table cellpadding="0" cellspacing="0" class="treeTable" style="width:100%">
                                        <thead>
                                        <tr>
                                            <th ><fmt:message key="attribute.values"/></th>
                                            <th  style="background-image:none;border:none"></th>
                                            <th><fmt:message key="selected.attribute.values"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <tr>
                                            <%
                                                if(selectedTree != null){
                                            %>
                                            <td style="width: 500px;border:solid 1px #ccc">
                                                <div class="treeControl">
                                                    <ul>
                                                        <%
                                                            if(finderDataHolder.getHierarchicalTree()){
                                                                EntitlementTreeNodeDTO[] childNodes = selectedTree.getChildNodes();
                                                                if(childNodes != null && childNodes.length > 0){
                                                                    for(EntitlementTreeNodeDTO childNode : childNodes){
                                                                        printChildrenTree(childNode , out);
                                                                    }
                                                                }
                                                            } else {
                                                                EntitlementTreeNodeDTO[] childNodes = selectedTree.getChildNodes();
                                                                if(childNodes != null && childNodes.length > 0){
                                                                    for(EntitlementTreeNodeDTO childNode : childNodes){
                                                                        printChildren(childNode, selectedTree.getName(), out);
                                                                    }
                                                                }
                                                            }
                                                        %>
                                                    </ul>
                                                </div>
                                            </td>
                                            <td style="width:50px;vertical-align: middle;border-bottom:solid 1px #ccc">
                                                <input class="button" value=">>" onclick="pickNames(<%=Encode.forJavaScriptAttribute(finderDataHolder.getFullPathSupported())%>)" style="width:30px;margin:10px;" />
                                            </td>
                                            <td style="border:solid 1px #ccc"><div style="overflow: auto;height:300px" id="listView"></div>
                                            </td>
                                            <%
                                                }
                                            %>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                <%
                    }
                %>
                            <tr>
                                <td class="buttonRow" >
                                    <%--<%--%>
                                        <%--if(selectedTree != null){--%>
                                    <%--%>--%>
                                    <input type="button" onclick="submitForm(<%=Encode.forJavaScriptAttribute(finderDataHolder.getFullPathSupported())%>)" value="<fmt:message key="add"/>"  class="button"/>
                                    <%--<%--%>
                                        <%--}--%>
                                    <%--%>--%>
                                    <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />" class="button"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>