<%--
  Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

   WSO2 Inc. licenses this file to you under the Apache License,
   Version 2.0 (the "License"); you may not use this file except
   in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
  --%>

<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String oldRoleName = request.getParameter("oldRoleName");
    String newRoleName = request.getParameter("newRoleName");
	String forwardTo = null;

    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
        // if old role name contains a domain, it would be appended to new role nme
        if(oldRoleName.contains(UserAdminUIConstants.DOMAIN_SEPARATOR) &&
        !newRoleName.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)){
            String domain = oldRoleName.substring(0, oldRoleName.indexOf(UserAdminUIConstants.DOMAIN_SEPARATOR));
            newRoleName = domain + UserAdminUIConstants.DOMAIN_SEPARATOR + newRoleName;
        }

        client.updateRoleName(oldRoleName ,newRoleName);
        forwardTo = "role-mgt.jsp?ordinal=1";
        session.removeAttribute("roleBean");
        session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
        session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED);
        String message = MessageFormat.format(resourceBundle.getString("role.update"), oldRoleName);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("role.cannot.add"),
                new Object[] { newRoleName, e.getMessage() });
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "role-mgt.jsp?ordinal=1";
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
