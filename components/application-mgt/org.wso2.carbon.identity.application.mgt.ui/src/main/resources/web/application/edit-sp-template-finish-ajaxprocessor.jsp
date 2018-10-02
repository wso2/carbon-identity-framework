<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.SpTemplate" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementClientException" %>
<%@ page
        import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String content = request.getParameter("templateContent") != null ? request.getParameter("templateContent").trim() : "";
    String templateName = request.getParameter("sp-template-name") != null ? request.getParameter("sp-template-name").trim() : "";
    String modifiedTemplateName = request.getParameter("template-name") != null ? request.getParameter("template-name").trim() : "";
    String modifiedTemplateDesc = request.getParameter("template-description") != null ? request.getParameter("template-description").trim() : "";
    if (StringUtils.isNotEmpty(content)) {
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(
                    cookie, backendServerURL, configContext);
            SpTemplate spTemplate = new SpTemplate();
            spTemplate.setName(modifiedTemplateName);
            spTemplate.setDescription(modifiedTemplateDesc);
            spTemplate.setContent(content);
            serviceClient.updateApplicationTemplate(templateName, spTemplate);

            String message = resourceBundle.getString("alert.success.update.sp.template");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
%>
    <script>
        location.href = 'list-sp-templates.jsp';
    </script>

<%
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementClientException e) {
            String message = resourceBundle.getString("alert.error.update.sp.template");
            if (e.getFaultMessage() != null && e.getFaultMessage().getIdentityApplicationManagementClientException() != null
                    && e.getFaultMessage().getIdentityApplicationManagementClientException().getMessages() != null) {
                String[] errorMessages = e.getFaultMessage().getIdentityApplicationManagementClientException().getMessages();
                session.setAttribute("updateTemplateError", errorMessages);

%>
<script>
    location.href = 'edit-sp-template.jsp?templateName=<%=Encode.forUriComponent(templateName)%>&updateTemplateError=true';
</script>
<%
} else {
    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);%>

<script>
    location.href = 'edit-sp-template.jsp?templateName=<%=Encode.forUriComponent(templateName)%>';
</script>

<%
            }
        }
    } else {
        String message = resourceBundle.getString("alert.error.sp.template.content");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
%>
    <script>
        location.href = 'list-sp-templates.jsp';
    </script>
<%
    }
%>

