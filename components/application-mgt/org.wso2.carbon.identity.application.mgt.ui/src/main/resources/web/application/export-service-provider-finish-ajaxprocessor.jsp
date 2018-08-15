<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  --%>

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%! public static final String BYTES = "bytes";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream;";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String XML = ".xml";
    private static final String ATTACHMENT_FILENAME = "attachment;filename=\"";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
%>
<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    
    String spName = request.getParameter("spName");
    String secrets = request.getParameter("exportSecrets");
    boolean exportSecrets = "on".equals(secrets);
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    
    if (StringUtils.isNotEmpty(spName)) {
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            
            ApplicationManagementServiceClient serviceClient =
                    new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
            String appData = serviceClient.exportApplication(spName, exportSecrets);
            out.clearBuffer();
            response.setHeader(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + spName + XML + "\"");
            response.setHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM);
            response.setHeader(ACCEPT_RANGES, BYTES);
            response.setHeader(CONTENT_LENGTH, String.valueOf(appData.length()));
            out.write(appData);
        } catch (Exception e) {
            String message = resourceBundle.getString("application.list.error.while.exporting.app") + " : " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
        }
    }

%>
<script>
    location.href = 'list-service-providers.jsp';
</script>
