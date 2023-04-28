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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.EntitledAttributesDTO"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String subjectType = "";
    String action = "";
    String subjectName = "";
    String subjectId = "";
    String resourceName = "";
    String enableChildSearchParameter;
    boolean enableChildSearch;
    String[] subjectTypes = new String[]{"Role","User"};
    EntitledResultSetDTO results = null;
    EntitledAttributesDTO[] entitledAttributes = null;
    String forwardTo;

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    subjectType = (String)request.getParameter("subjectType");
    if("Role".equals(subjectType)) {
        subjectId = EntitlementPolicyConstants.SUBJECT_ID_ROLE;
    } else {
        subjectType = "User";
        subjectId = EntitlementPolicyConstants.SUBJECT_ID_DEFAULT;
    }

    String userSelectedSubjectId = (String)request.getParameter("subjectId");
    if(userSelectedSubjectId != null && !"".equals(userSelectedSubjectId)){
        subjectId = userSelectedSubjectId;
    }
    subjectName = (String)request.getParameter("subjectName");
    resourceName = (String) request.getParameter("resourceName");
    action = (String)request.getParameter("action");
    enableChildSearchParameter = (String)request.getParameter("enableChildSearch");
    if("true".equals(enableChildSearchParameter)){
        enableChildSearch = true;
    } else {
        enableChildSearch =false;
    }

    try {
        if (subjectName != null) {
            EntitlementServiceClient client = new EntitlementServiceClient(cookie,
                    serverURL, configContext);
            results = client.getEntitledAttributes(subjectName, resourceName, subjectId, action,
                                                   enableChildSearch);

            if(EntitlementPolicyConstants.SEARCH_ERROR.equals(results.getMessageType())){
%>
            <script type="text/javascript">
                function showErrorBox(message){
                    CARBON.showErrorDialog(message);
                }
            </script>

            <script type="text/javascript">
                showErrorBox("<%=Encode.forJavaScriptAttribute(Encode.forHtml(results.getMessage()))%>");
            </script>
<%
            } else {
                entitledAttributes = results.getEntitledAttributesDTOs();
            }
        }
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.performing.advance.search");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
		label="advance.search"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
    <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
    <link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function submitForm(){
        if(doValidation()){
            document.requestForm.action = "advance-search.jsp";
            document.requestForm.submit();
        }
    }

    function doCancel(){
        location.href = 'index.jsp?';
    }

    function getSelectedSubjectType() {
        var comboBox = document.getElementById("subjectType");
        var selectedSubjectType = comboBox[comboBox.selectedIndex].value;
        location.href = 'advance-search.jsp?subjectType=' + selectedSubjectType;
    }

    function doValidation() {

        var value = document.getElementsByName("subjectName")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="subject.name.is.required"/>');
            return false;
        }
        return true;
    }

</script>

<div id="middle">
    <h2><fmt:message key="entitled.data.search"/></h2>
    <%
        if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/entitlement/pdp")) {
    %>
    <div id="workArea">
        <form id="requestForm" name="requestForm" method="post" action="advance-search.jsp">
        <table class="styledLeft noBorders">

        <tr>
            <td class="leftCol-small"><fmt:message key='select.subject.type'/><span class="required">*</span></td>
            <td colspan="2">
            <select  onchange="getSelectedSubjectType();" id="subjectType" name="subjectType">
                    <%
                        for (String type : subjectTypes) {
                            if(type.equals(subjectType)) {
                    %>
                        <option value="<%=subjectType%>" selected="selected"><%=subjectType%></option>

                    <%
                            } else {
                    %>
                        <option value="<%=type%>"><%=type%></option>

                    <%
                            }
                        }
                    %>
            </select>
            </td>
        </tr>

        <tr>
            <td class="leftCol-small"><fmt:message key='user.role'/><span class="required">*</span></td>
            <td colspan="2">
            <%
                if (subjectName != null && !subjectName.equals("")) {
            %>
            <input type="text" name="subjectName" id="subjectName"
                       value="<%=Encode.forHtmlAttribute(subjectName)%>" class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="subjectName" id="subjectName" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>


        <tr>
            <td class="leftCol-small"><fmt:message key='subject.id'/><span class="required">*</span></td>
            <td colspan="2">
            <input type="text" name="subjectId" id="subjectId"
                       value="<%=Encode.forHtmlAttribute(subjectId)%>" class="text-box-big"/>
            </td>
        </tr>

        <tr>
            <td class="leftCol-small"><fmt:message key='action'/></td>
            <td colspan="2">
            <%
                if (action != null && !action.equals("")) {
            %>
            <input type="text" name="action" id="action" value="<%=Encode.forHtmlAttribute(action)%>"
                       class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="action" id="action" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>            

        <tr>
            <td class="leftCol-small"><fmt:message key='resource.name'/></td>
            <td colspan="2">
            <%
                if (resourceName != null && !resourceName.equals("")) {
            %>
            <input type="text" name="resourceName" id="resourceName" value="<%=Encode.forHtmlAttribute(resourceName)%>"
                       class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="resourceName" id="resourceName" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>

        <tr>
            <td></td>
            <td>
               <input <%if(enableChildSearch){%> checked="checked" <%}%> type="checkbox" name="enableChildSearch" value="true" /><fmt:message key="enable.child.search"/>
            </td>
        </tr>

        <tr>
            <td colspan="2" class="buttonRow">
                <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                   onclick="submitForm(); return false;"><fmt:message key="search"/></a>
            </td>
        </tr>
        </table>
        </form>

        <table  class="styledLeft"  style="width: 100%;margin-top:10px;">
            <thead>
                <tr>
                    <%
                        if(action == null || action.trim().length() < 1){
                    %>
                        <th><fmt:message key='resource'/></th>
                        <th><fmt:message key='action'/></th>
                    <%
                        }  else {
                    %>
                        <th colspan="2"><fmt:message key='resource'/></th>
                    <%
                        }
                    %>
                </tr>
            </thead>
            <tbody>

                <%
                    if(entitledAttributes != null && entitledAttributes.length > 0) {
                        Set<String> resourceSet = new HashSet <String>();
                        for(EntitledAttributesDTO result : entitledAttributes){
                            if(result.getAllResources()){
                                resourceSet.add("ANY");            
                            } else {
                                resourceSet.add(result.getResourceName());
                            }
                        }
                        for(String resource : resourceSet){
                %>
                <tr>
                        <td><%=Encode.forHtmlContent(resource)%></td>
                        <%

                            if(action == null || action.trim().length() < 1){
                                Set<String> actionSet = new HashSet<String>();
                                String actionNames = "";
                                for(EntitledAttributesDTO result : entitledAttributes){
                                    if(result.getAllResources()){
                                        if(result.getAllActions()){
                                            actionSet.add("ANY");
                                        } else {
                                            actionSet.add(result.getAction());
                                        }
                                    } else if(resource.equals(result.getResourceName())){
                                        if(result.getAllActions()){
                                            actionSet.add("ANY");
                                        } else {
                                            actionSet.add(result.getAction());
                                        }       
                                    }
                                }

                                for(String actionName : actionSet){
                                    if("".equals(actionNames)){
                                        actionNames = actionName;
                                    } else {
                                        actionNames = actionNames + " , " + actionName;
                                    }
                                }
                        %>
                                 <td><%=Encode.forHtmlContent(actionNames)%></td>
                        <%
                            }
                        %>
                </tr>
                <%
                        }
                    } else {
                %>
                    <tr>
                        <td colspan="2">No Result is found</td>
                    </tr>

                <%
                    }
                %>
            </tbody>
        </table>
    </div>
    <%
        }
    %>
</div>
</fmt:bundle>
