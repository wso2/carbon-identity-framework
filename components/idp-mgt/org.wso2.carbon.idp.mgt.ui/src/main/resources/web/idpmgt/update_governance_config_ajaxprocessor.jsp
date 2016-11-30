<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.wso2.carbon.identity.governance.stub.bean.Property" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityGovernanceAdminClient" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        Enumeration<String> paramNames = request.getParameterNames();
        List<String> paramNamesList = new ArrayList<String>();
        while (paramNames.hasMoreElements()) {
            paramNamesList.add(paramNames.nextElement());
        }
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        Property[] properties = new Property[paramNamesList.size()];
        for (int i = 0; i < paramNamesList.size(); i++) {
            Property prop = new Property();
            prop.setName(paramNamesList.get(i));
            prop.setValue(request.getParameter(paramNamesList.get(i)));
            properties[i] = prop;
        }
        IdentityGovernanceAdminClient client = new IdentityGovernanceAdminClient(cookie, backendServerURL,
                configContext);
        client.updateConfigurations(properties);
        String message = MessageFormat.format(resourceBundle.getString("success.adding.config"), null);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.adding.config"),
                new Object[]{e.getMessage()});
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }
%>

<script type="text/javascript">
    location.href = "idp-mgt-list.jsp";
</script>