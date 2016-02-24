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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>

<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>

<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="java.text.MessageFormat"%>
<%@ page import="java.util.ResourceBundle" %>
<%
    String policy = "";
    String policyId = request.getParameter("policyId");
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    try {
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(
                cookie, serverURL, configContext);
        PolicyDTO policyDTO = client.getPolicy(policyId, true);
        if (policyDTO != null && policyDTO.getPolicy() != null) {
            policy = policyDTO.getPolicy().trim().replaceAll("><", ">\n<");
        }
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.
                getString("error.while.retreiving.policies"), e.getMessage());
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=message%>',  function () {
            location.href = "my-pdp.jsp";
        });
    });
</script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="policy.viewer"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />
<div id="middle">
    <h2><fmt:message key='policy.viewer'/></h2>
    <div id="workArea">
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
                                <%=Encode.forHtmlContent(policy)%>
                            </textarea>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
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

    function cancel() {
        location.href = "my-pdp.jsp";
    }

</script>