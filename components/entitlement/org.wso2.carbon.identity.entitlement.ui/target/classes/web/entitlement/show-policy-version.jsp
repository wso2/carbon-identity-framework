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
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants"%>

<%@page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>

<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.ResourceBundle" %>
<%
    String policy = "";
    String createdTime = "";
    String createdUser = "";
    String forwardTo = "index.jsp";
    String policyId = request.getParameter("policyId");
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String selectedVersion = request.getParameter("selectedVersion");
    String[] versions = null;
    if(selectedVersion == null || selectedVersion.trim().length() == 0){
        selectedVersion = EntitlementPolicyConstants.ENTITLEMENT_CURRENT_VERSION;
    }
    try {
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(
                cookie, serverURL, configContext);
        versions = client.getPolicyVersions(policyId);
        if(versions == null || versions.length == 0){
            versions = new String[]{EntitlementPolicyConstants.ENTITLEMENT_CURRENT_VERSION};
        } else {
            versions[versions.length-1] = EntitlementPolicyConstants.ENTITLEMENT_CURRENT_VERSION;
        }

        PolicyDTO policyDTO = null;
        if(EntitlementPolicyConstants.ENTITLEMENT_CURRENT_VERSION.equals(selectedVersion)){
            policyDTO =  client.getPolicy(policyId, false);
        } else {
            policyDTO =  client.getPolicyByVersion(policyId, selectedVersion);
        }

        if (policyDTO.getPolicy() != null) {
            policy = policyDTO.getPolicy().trim().replaceAll("><", ">\n<");
        }

        if(policyDTO.getLastModifiedTime() != null){
            try {
                Date date = new Date(Long.parseLong(policyDTO.getLastModifiedTime()));
                createdTime = date.toString();
            } catch (Exception e){
                // ignore
            }
        }

        if(policyDTO.getLastModifiedUser() != null){
            createdUser = policyDTO.getLastModifiedUser();
        }

    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.
                getString("error.while.retreiving.policies"), e.getMessage());
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>',  function () {
            location.href = "index.jsp";
        });
    });
</script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb label="policy.version.manage"
                       resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" />
<div id="middle">
    <h2><fmt:message key='policy.version.manage'/></h2>
    <div id="workArea">
    <form method="post" name="versionForm" id="versionForm" action="show-policy-version.jsp?policyId=<%=Encode.forUriComponent(policyId)%>">
    <table id="mainTable" class="styledLeft noBorders">
        <tr>
            <td><fmt:message key="entitlement.policy.id" /></td>
            <td><%=Encode.forHtml(policyId)%></td>
        </tr>
        <tr>
            <td><fmt:message key="policy.version"/></td>
            <td>
                <select id="selectedVersion" name="selectedVersion" onchange="getVersion();" >
                    <%
                        if (versions != null && versions.length > 0) {
                            for (String version : versions) {
                                if (version.equals(selectedVersion)) {
                    %>
                    <option value="<%=Encode.forHtmlAttribute(version)%>" selected="selected"><%=Encode.forHtmlContent(version)%>
                    </option>
                    <%
                                    } else {
                    %>
                    <option value="<%=Encode.forHtmlAttribute(version)%>"><%=Encode.forHtmlContent(version)%>
                    </option>
                    <%
                                }
                            }
                        }
                    %>
                </select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="policy.version.created.time" /></td>
            <td><%=createdTime%></td>
        </tr>
        <tr>
            <td><fmt:message key="policy.version.created.user" /></td>
            <td><%=createdUser%></td>
        </tr>
    </table>
    </form>

    <table class="styledLeft" style="width:100%">
        <thead>
        <tr>
            <th>
                <fmt:message key='policy.version.view'/>
            </th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="formRow">
                <table class="normal" style="width:100%">
                    <tbody>
                    <tr>
                        <td>
                            <input type="hidden" name="editorConfig" id="editorConfig">
                            <textarea  readonly="true" name="editorConfigText"  id="editorConfigText"  rows="50" cols="50"
                                      style="border: 1px solid rgb(204, 204, 204); width: 90%;">
                                    <%--height: 1000px; margin-top: 5px; display: none;"--%>
                                <%=Encode.forHtml(policy)%>
                            </textarea>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <%
                    if(!EntitlementPolicyConstants.ENTITLEMENT_CURRENT_VERSION.equals(selectedVersion) &&
                            CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/configure/entitlement/policy/manage/rollback")){
                %>
                <button class="button" onclick="submitForm(); return false;"><fmt:message key="rollaback"/></button>
                <%
                    }
                %>
                <button class="button" onclick="cancel(); return false;"><fmt:message key="cancel"/></button>
            </td>
        </tr>
        </tbody>
    </table>
    </div>
</div>
</fmt:bundle>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<script type="text/javascript">

    jQuery(document).ready(function(){
        editAreaLoader.init({
            id : "editorConfigText"		// text area id
            ,syntax: "xml"			// syntax to be uses for highlighting
            ,start_highlight: true  // to display with highlight mode on start-up
            ,is_editable: false
        });
    })

    function submitForm() {
        document.versionForm.action= "rollback-policy.jsp?policyId=<%=Encode.forUriComponent(policyId)%>";
        document.versionForm.submit();
    }

    function cancel() {
        location.href = "index.jsp";
    }

    function getVersion(){
        document.versionForm.submit();
    }
</script>
