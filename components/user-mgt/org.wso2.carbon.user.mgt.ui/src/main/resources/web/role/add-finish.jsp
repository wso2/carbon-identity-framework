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
<%@page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.core.UserCoreConstants" %>
<%@ page import="org.wso2.carbon.user.core.util.UserCoreUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>

<jsp:useBean id="roleBean" type="org.wso2.carbon.user.mgt.ui.RoleBean"
             class="org.wso2.carbon.user.mgt.ui.RoleBean" scope="session"/>
<jsp:setProperty name="roleBean" property="*" />
<%
    String forwardTo = null;
    String roleName = null;
    String roleType = null;
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        roleName = roleBean.getRoleName();
        roleType = roleBean.getRoleType();
        if ((roleType == null || "null".equals(roleType)) &&
                UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(IdentityUtil.extractDomainFromName(roleName))) {
            roleType = UserCoreConstants.INTERNAL_DOMAIN;
        } else if ((roleType == null || "null".equals(roleType)) &&
                UserAdminUIConstants.APPLICATION_DOMAIN.equalsIgnoreCase(IdentityUtil.extractDomainFromName(roleName))){
            roleType = UserAdminUIConstants.APPLICATION_DOMAIN;
        }
        boolean isSharedRole = roleBean.getSharedRole() != null && !roleBean.getSharedRole().isEmpty(); 
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                                                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);

        roleBean.addRoleUsers((Map<String, Boolean>) session.getAttribute("checkedUsersMap"));

        if(UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType)){
            client.addInternalRole(UserCoreUtil.removeDomainFromName(roleName), roleBean.getRoleUsers(),
                                                                roleBean.getSelectedPermissions());
        } else if(UserAdminUIConstants.APPLICATION_DOMAIN.equalsIgnoreCase(roleType)) {
            client.addInternalRole(roleName, roleBean.getRoleUsers(), roleBean.getSelectedPermissions());
        } else {
            client.addRole(roleName, roleBean.getRoleUsers(), roleBean.getSelectedPermissions(), isSharedRole);
        }

        session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
        session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED);
        
        String message = MessageFormat.format(resourceBundle.getString("role.add"), roleName);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        forwardTo = "role-mgt.jsp?ordinal=1";
    } catch (InstantiationException e) {
        CarbonUIMessage.sendCarbonUIMessage("Your session has timed out. Please try again.",
                CarbonUIMessage.ERROR, request);
    }catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("role.cannot.add"),
                new Object[] { roleName, e.getMessage() });
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        if(UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType)){
            forwardTo = "add-step1.jsp?roleType=" + UserAdminUIConstants.INTERNAL_ROLE ; 
        } else {
            forwardTo = "add-step1.jsp";
        }
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
    forward();
</script>
