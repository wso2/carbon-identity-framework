<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>

<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>

<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String policy = "";
    String policyId = null;
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(
                cookie, serverURL, configContext);
        policyId = request.getParameter("policyid");
        policy = (String) session.getAttribute("policy");
        if (policy == null && policyId != null && policyId.trim().length() > 0) {
            PolicyDTO dto = client.getPolicy(policyId, false);
            if (dto != null) {
                policy = dto.getPolicy();
            }
        }

        if (policy != null) {
            policy = policy.trim().replaceAll("><", ">\n<");
        } else {
            policy = "";
        }
        session.removeAttribute("policy");
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.
                getString("error.while.retreiving.policies"), e.getMessage());
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>', function () {
            location.href = "index.jsp";
        });
    });
</script>
<%
    }
%>

<div style="display: none;">
    <form name="frmPolicyData" action="../policyeditor/index.jsp" method="post">
        <input type="hidden" name="policy" id="policy">
        <input type="hidden" name="visited" id="visited">
        <textarea id="txtPolicy" rows="50" cols="50"><%=Encode.forHtmlContent(policy)%>
        </textarea>
        <input type="hidden" name="policyid" value="<%=Encode.forUriComponent(policyId)%>"/>
        <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>"/>
    </form>
</div>

<script type="text/javascript">
    // Handling the browser back button for Firefox. The IE back button is handled form the policy editor index.jsp page
    if (document.frmPolicyData.visited.value == "") {
        // This is a fresh page load
        document.frmPolicyData.visited.value = "1";

        function submitForm() {
            document.getElementById("policy").value = document.getElementById("txtPolicy").value;
            document.frmPolicyData.submit();
        }

        submitForm();
    }
    else {
        location.href = '<%=Encode.forJavaScriptBlock(request.getHeader("Referer"))%>';
    }
</script>