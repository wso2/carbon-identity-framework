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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.context.PrivilegedCarbonContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.core.util.UserCoreUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    String forwardTo = null;
    String username = request.getParameter("username");
    String newPassword = request.getParameter("newPassword");
    String isUserChange = request.getParameter("isUserChange");
    String returnPath = request.getParameter("returnPath");
    String currentPassword = request.getParameter("currentPassword");

    String trustedReturnPath = "../userstore/index.jsp";
    if ("user-mgt.jsp".equals(returnPath)) {
        trustedReturnPath = "user-mgt.jsp";
    }

    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
        if (isUserChange != null) {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            client.changePasswordByUser(UserCoreUtil.addTenantDomainToEntry(username, tenantDomain),
                                        currentPassword, newPassword);
            forwardTo = trustedReturnPath;
            session.removeAttribute(ServerConstants.PASSWORD_EXPIRATION);
        } else {
            client.changePassword(username, newPassword);
            forwardTo = "user-mgt.jsp?ordinal=1";
        }

        String message = MessageFormat.format(resourceBundle.getString("password.change.successful"),
                                              new Object[] { username });
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);

    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("password.change.error"),
                                              username, e.getMessage());
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        if (isUserChange != null) {
            forwardTo = "change-passwd.jsp?ordinal=2&returnPath=" + trustedReturnPath + "&isUserChange=true";
        } else {
            forwardTo = "change-passwd.jsp?username=" + Encode.forUriComponent(username) + "&ordinal=2";
        }
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>