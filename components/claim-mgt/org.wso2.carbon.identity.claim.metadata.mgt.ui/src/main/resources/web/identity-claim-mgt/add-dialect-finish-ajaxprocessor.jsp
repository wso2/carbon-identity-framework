<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.ui.client.ClaimMetadataAdminClient" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimDialectDTO" %>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext)
            config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String externalClaimDialectURI = request.getParameter("dialect");

    ClaimDialectDTO externalClaimDialect = new ClaimDialectDTO();
    externalClaimDialect.setClaimDialectURI(externalClaimDialectURI);

    String forwardTo = null;
    try {

        ClaimMetadataAdminClient client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);
        client.addClaimDialect(externalClaimDialect);
        forwardTo = "list.jsp?region=region1&item=identity_claim_mgt_menu_list";

    } catch (Exception e) {

        String BUNDLE = "org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        String unformatted = resourceBundle.getString("error.adding.claim.dialect");
        String message = MessageFormat.format(unformatted, new
                Object[]{Encode.forHtmlContent(externalClaimDialectURI)});

        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "list.jsp?region=region1&item=identity_claim_mgt_menu_list";
    }
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }

    forward();
</script>