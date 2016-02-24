<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
    prefix="carbon"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileCient"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>

<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="java.util.ResourceBundle"%>
<%@ page import="org.owasp.encoder.Encode" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />

<%
    String profile = request.getParameter("profile");
    String username = request.getParameter("username");
    String forwardTo = "";
    String BUNDLE = "org.wso2.carbon.identity.user.profile.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
	String fromUserMgt = (String) request.getParameter("fromUserMgt");
	
    if (fromUserMgt==null) fromUserMgt = "false";

    
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserProfileCient client = new UserProfileCient(cookie, backendServerURL, configContext);
        client.deleteUserProfile(username, profile);
        String message = resourceBundle.getString("user.profile.deleted.successfully");
        CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.INFO, request);
        if ("true".equals(fromUserMgt)) {
           forwardTo = "index.jsp?username="+ Encode.forUriComponent(username);
        }else{
        	forwardTo = "index.jsp";
        }
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.updating.profile.user");
        CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.ERROR, request,e);
        forwardTo = "../admin/error.jsp";
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