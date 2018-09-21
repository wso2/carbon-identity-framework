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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.SpTemplate" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ClientResponse" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp" />

<%
    String templateName = request.getParameter("templateName");
    String templateDesc = request.getParameter("templateDesc");
    String oldSPName = request.getParameter("oldSPName");

    String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, oldSPName);
        appBean.update(request);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie,
                backendServerURL, configContext);
        SpTemplate spTemplate = new SpTemplate();
        spTemplate.setName(templateName);
        spTemplate.setDescription(templateDesc);
        ServiceProvider sp = appBean.getServiceProvider();
        ClientResponse clientResponse = serviceClient.createApplicationTemplateFromSP(sp, spTemplate);
        if (clientResponse.getResponseCode() != HttpStatus.SC_CREATED) {
            String[] errors = clientResponse.getErrors();
            session.setAttribute("clientError", errors);
%>
            <script>
                location.href = 'add-service-provider.jsp?clientError=true';
            </script>
<%
        }
    } catch (Exception e) {
        String message = resourceBundle.getString("alert.error.add.sp.as.template");
        out.print(message);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
    }
%>
