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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<jsp:useBean id="userBean" type="org.wso2.carbon.user.mgt.ui.UserBean"
             class="org.wso2.carbon.user.mgt.ui.UserBean" scope="session"/>
<jsp:setProperty name="userBean" property="*" />
<%
    String username = null;
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String forwardTo = null;
    try{
        username = userBean.getUsername();
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);

        ClaimValue[] claims = null;
        String userPassword = (userBean.getPassword());
        if(userBean.getEmail().trim().length() > 0 ){
            ClaimValue emailClaim = new ClaimValue();
            emailClaim.setClaimURI(UserAdminUIConstants.EMAIL_CLAIM_URI);
            emailClaim.setValue(userBean.getEmail());
            claims = new ClaimValue[]{emailClaim};
            userPassword = null;
        }
        userBean.addUserRoles((Map<String,Boolean>)session.getAttribute("checkedRolesMap"));
        client.addUser(username, userPassword, userBean.getUserRoles(), claims, null);


        String message = MessageFormat.format(resourceBundle.getString("user.add"), username);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        forwardTo = "user-mgt.jsp?ordinal=1";
    } catch(InstantiationException e){
        CarbonUIMessage.sendCarbonUIMessage("Your session has timed out. Please try again.", CarbonUIMessage.ERROR, request);
        forwardTo = "user-mgt.jsp?ordinal=1";
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("user.cannot.add"), new Object[]{username, e.getMessage()});
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "user-mgt.jsp?ordinal=1";
    }
    finally {
        session.removeAttribute(UserAdminUIConstants.USER_LIST_CACHE);
        session.removeAttribute(UserAdminUIConstants.USER_LIST_CACHE_EXCEEDED);
        session.removeAttribute("userBean");
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }

    forward();
</script>
